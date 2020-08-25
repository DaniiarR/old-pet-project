package com.york.exordi.recycler;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.york.exordi.R;
import com.york.exordi.adapters.PostViewHolder;
import com.york.exordi.models.Result;
import com.york.exordi.shared.OnFullscreenButtonClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class VideoPlayerRecyclerView extends RecyclerView {

    private static final String TAG = "VideoPlayerRecyclerView";

    private enum VolumeState {ON, OFF};

    // ui
    private ImageView thumbnail, volumeControl;
    private ProgressBar progressBar;
    private View viewHolderParent;
    private FrameLayout frameLayout;
    private PlayerView videoSurfaceView;
    private SimpleExoPlayer videoPlayer;
    private ImageButton fullscreenButton;

    // vars
    private List<Result> resultArrayList = new ArrayList<>();
    private int videoSurfaceDefaultHeight = 0;
    private int screenDefaultHeight = 0;
    private Context context;
    private int playPosition = -1;
    private boolean isVideoViewAdded;
    private RequestManager requestManager;

    // controlling playback state
    private VolumeState volumeState;

    private OnFullscreenButtonClickListener listener;

    public VideoPlayerRecyclerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public VideoPlayerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context){
        this.context = context.getApplicationContext();
        Display display = context.getDisplay();
        Point point = new Point();
        display.getRealSize(point);
        videoSurfaceDefaultHeight = point.x;
        screenDefaultHeight = point.y;

        videoSurfaceView = new PlayerView(this.context);
        videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        fullscreenButton = videoSurfaceView.findViewById(R.id.exo_fullscreen);
        videoSurfaceView.findViewById(R.id.exoTimelineControls).setVisibility(View.GONE);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
//        TrackSelection.Factory videoTrackSelectionFactory =
//                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        // 2. Create the player
        videoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        // Bind the player to the view.
        videoSurfaceView.setUseController(false);
        videoSurfaceView.setPlayer(videoPlayer);
        setVolumeControl(VolumeState.ON);

        final boolean[] isScrolled = {false};
        addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d(TAG, "onScrollStateChanged: called.");
//                    if(thumbnail != null){ // show the old thumbnail
//                        thumbnail.setVisibility(VISIBLE);
//                    }

                    // There's a special case when the end of the list has been reached.
                    // Need to handle that with this bit of logic
                    if(!recyclerView.canScrollHorizontally(1)){
                        playVideo(true);
                    }
                    else{
                        playVideo(false);
                    }
                }
            }


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // this bit of logic is used when the first item is video
                // since @onScrollStateChanged won't get called to play it
                if (!isScrolled[0]) {
                    if(!recyclerView.canScrollHorizontally(1)){
                        playVideo(true);
                    }
                    else{
                        playVideo(false);
                    }
                    isScrolled[0] = true;
                }
            }
        });

        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                Log.e(TAG, "onChildViewAttachedToWindow: " );
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                if (viewHolderParent != null && viewHolderParent.equals(view)) {
                    resetVideoView();
                }

            }
        });

        videoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {

                    case Player.STATE_BUFFERING:
                        Log.e(TAG, "onPlayerStateChanged: Buffering video.");
                        if (progressBar != null) {
                            progressBar.setVisibility(VISIBLE);
                        }

                        break;
                    case Player.STATE_ENDED:
                        Log.d(TAG, "onPlayerStateChanged: Video ended.");
                        videoPlayer.seekTo(0);
                        break;
                    case Player.STATE_IDLE:

                        break;
                    case Player.STATE_READY:
                        Log.e(TAG, "onPlayerStateChanged: Ready to play.");
                        if (progressBar != null) {
                            progressBar.setVisibility(GONE);
                        }
                        if(!isVideoViewAdded){
                            addVideoView();
                        }
                        break;
                    default:
                        break;
                }
            }
        });

    }


    public void playVideo(boolean isEndOfList) {

        int targetPosition;

        if(!isEndOfList){
            int startPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
            int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();

            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1;
            }

            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return;
            }

            // if there is more than 1 list-item on the screen
//            if (startPosition != endPosition) {
//                int startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition);
//                int endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition);
//
//                targetPosition = startPositionVideoHeight > endPositionVideoHeight ? startPosition : endPosition;
//            } else {
//                targetPosition = startPosition;
//            }
            targetPosition = startPosition;
        } else {
            targetPosition = resultArrayList.size() - 1;
        }

        Log.d(TAG, "playVideo: target position: " + targetPosition);

        // video is already playing so return
        if (targetPosition == playPosition) {
            return;
        }

        // set the position of the list-item that is to be played
        playPosition = targetPosition;
        if (videoSurfaceView == null) {
            return;
        }

        // remove any old surface views from previously playing videos
        videoSurfaceView.setVisibility(INVISIBLE);
        removeVideoView(videoSurfaceView);

        int currentPosition = targetPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();

        View child = getChildAt(currentPosition);
        if (child == null) {
            return;
        }

        PostViewHolder holder = (PostViewHolder) child.getTag();
        if (holder == null) {
            playPosition = -1;
            return;
        }
//        thumbnail = holder.thumbnail;
        progressBar = holder.progressBar;
//        volumeControl = holder.volumeControl;
        viewHolderParent = holder.itemView;
        requestManager = holder.requestManager;
        frameLayout = holder.itemView.findViewById(R.id.feedListItemFrameLayout);

        videoSurfaceView.setPlayer(videoPlayer);

        viewHolderParent.setOnClickListener(videoViewClickListener);

//        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
//                context, Util.getUserAgent(context, context.getString(R.string.app_name)));
        DataSource.Factory dataSourceFactory =
                new DefaultHttpDataSourceFactory(Util.getUserAgent(context, context.getString(R.string.app_name)), new DefaultBandwidthMeter(), DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS, DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, true);
        if (resultArrayList.get(targetPosition).getFiles().get(0).getType().equals("video")) {
            String mediaUrl = resultArrayList.get(targetPosition).getFiles().get(0).getFile();
//            String mediaUrl = "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.mp4";
            fullscreenButton.setOnClickListener(v -> {
                listener.onButtonClick(mediaUrl, videoPlayer.getCurrentPosition());
                videoPlayer.setPlayWhenReady(false);
            });
            if (mediaUrl != null) {
                HlsMediaSource videoSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(mediaUrl));
                videoPlayer.prepare(videoSource);
                videoSurfaceView.setUseController(true);
//                videoPlayer.setPlayWhenReady(true);
            }
        } else {
            removeVideoView(videoSurfaceView);
        }
    }

    private OnClickListener videoViewClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleVolume();
        }
    };

    public void setOnFullscreenButtonClickListener(OnFullscreenButtonClickListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the visible region of the video surface on the screen.
     * if some is cut off, it will return less than the @videoSurfaceDefaultHeight
     * @param playPosition
     * @return
     */
    private int getVisibleVideoSurfaceHeight(int playPosition) {
        int at = playPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        Log.d(TAG, "getVisibleVideoSurfaceHeight: at: " + at);

        View child = getChildAt(at);
        if (child == null) {
            return 0;
        }

        int[] location = new int[2];
        child.getLocationInWindow(location);

        if (location[1] < 0) {
            return location[1] + videoSurfaceDefaultHeight;
        } else {
            return screenDefaultHeight - location[1];
        }
    }


    // Remove the old player
    private void removeVideoView(PlayerView videoView) {
        videoPlayer.setPlayWhenReady(false);
        ViewGroup parent = (ViewGroup) videoView.getParent();
        if (parent == null) {
            return;
        }

        int index = parent.indexOfChild(videoView);
        int childAmount = parent.getChildCount();
        if (index >= 0) {
            parent.removeViewAt(index);
            int childAmount2 = parent.getChildCount();
            isVideoViewAdded = false;
            viewHolderParent.setOnClickListener(null);
        }

    }

    private void addVideoView(){
        frameLayout.addView(videoSurfaceView);
        isVideoViewAdded = true;
        videoSurfaceView.requestFocus();
        videoSurfaceView.setVisibility(VISIBLE);
        videoSurfaceView.setAlpha(1);
//        thumbnail.setVisibility(GONE);
    }

    private void resetVideoView(){
        if(isVideoViewAdded){
            removeVideoView(videoSurfaceView);
            playPosition = -1;
            videoSurfaceView.setVisibility(INVISIBLE);
//            thumbnail.setVisibility(VISIBLE);
        }
    }

    public void releasePlayer() {

        if (videoPlayer != null) {
            videoPlayer.release();
            videoPlayer = null;
        }

        viewHolderParent = null;
    }

    private void toggleVolume() {
        if (videoPlayer != null) {
            if (volumeState == VolumeState.OFF) {
                Log.d(TAG, "togglePlaybackState: enabling volume.");
                setVolumeControl(VolumeState.ON);

            } else if(volumeState == VolumeState.ON) {
                Log.d(TAG, "togglePlaybackState: disabling volume.");
                setVolumeControl(VolumeState.OFF);

            }
        }
    }

    private void setVolumeControl(VolumeState state){
        volumeState = state;
        if(state == VolumeState.OFF){
            videoPlayer.setVolume(0f);
//            animateVolumeControl();
        }
        else if(state == VolumeState.ON){
            videoPlayer.setVolume(1f);
//            animateVolumeControl();
        }
    }

//    private void animateVolumeControl(){
//        if(volumeControl != null){
//            volumeControl.bringToFront();
//            if(volumeState == VolumeState.OFF){
//                requestManager.load(R.drawable.ic_volume_off_grey_24dp)
//                        .into(volumeControl);
//            }
//            else if(volumeState == VolumeState.ON){
//                requestManager.load(R.drawable.ic_volume_up_grey_24dp)
//                        .into(volumeControl);
//            }
//            volumeControl.animate().cancel();
//
//            volumeControl.setAlpha(1f);
//
//            volumeControl.animate()
//                    .alpha(0f)
//                    .setDuration(600).setStartDelay(1000);
//        }
//    }

    public void setResultArrayList(List<Result> resultArrayList){
        this.resultArrayList = resultArrayList;
    }

    public void setPlaybackPosition(long playbackPosition) {
        if (videoPlayer != null) {
            videoPlayer.seekTo(playbackPosition);
            videoPlayer.setPlayWhenReady(true);
        }
    }

}
