package com.york.exordi.recycler;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
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
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.bumptech.glide.RequestManager;
import com.github.vkay94.dtpv.DoubleTapPlayerView;
import com.github.vkay94.dtpv.youtube.YouTubeOverlay;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.york.exordi.R;
import com.york.exordi.adapters.PostViewHolder;
import com.york.exordi.models.PostId;
import com.york.exordi.models.Result;
import com.york.exordi.shared.OnFullscreenButtonClickListener;
import com.york.exordi.shared.OnPlayerReadyListener;
import com.york.exordi.shared.OnVideoWatchedListener;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayerRecyclerView extends RecyclerView {

    private static final String TAG = "VideoPlayerRecyclerView";

    private enum VolumeState {ON, OFF};

    private SnapHelper snapHelper;
    // ui
    private ImageView thumbnail, volumeControl;
    private ProgressBar progressBar;
    private View viewHolderParent;
    private FrameLayout frameLayout;
    public PlayerView videoSurfaceView;
    private SimpleExoPlayer videoPlayer;
    private ImageButton fullscreenButton;

    // vars
    private List<Result> resultArrayList = new ArrayList<>();
    private int videoSurfaceDefaultHeight = 0;
    private int screenDefaultHeight = 0;
    private Context context;
    private int playPosition = -1;
    private boolean isVideoViewAdded;
    private long videoDuration;
    private long seconds = 0;
    private RequestManager requestManager;
    private final boolean[] isScrolled = {false};

    // controlling playback state
    private VolumeState volumeState;
    private OnPlayerReadyListener onPlayerReadyListener;

    private OnFullscreenButtonClickListener listener;
    private OnVideoWatchedListener onVideoWatchedListener;



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
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(this);
        Display display = context.getDisplay();
        Point point = new Point();
        display.getRealSize(point);
        videoSurfaceDefaultHeight = point.x;
        screenDefaultHeight = point.y;

        videoSurfaceView = new PlayerView(this.context);
        videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
//        videoSurfaceView.setControllerAutoShow(false);
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
//        videoSurfaceView.setUseController(false);
        videoSurfaceView.setPlayer(videoPlayer);
        setVolumeControl(VolumeState.ON);
        addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d(TAG, "onScrollStateChanged: called.");
                    if(thumbnail != null){ // show the old thumbnail
                        thumbnail.setVisibility(VISIBLE);
                    }

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
                    if (!resultArrayList.isEmpty()) {
                        if(!recyclerView.canScrollHorizontally(1)){
                            playVideo(true);
                        }
                        else {
                            playVideo(false);
                        }
                        isScrolled[0] = true;
                    }
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

        Handler handler = new Handler(Looper.getMainLooper());
        videoPlayer.addListener(new Player.EventListener() {

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "onIsPlayingChanged: " + "handler started:" + seconds);
                            if (++seconds >= videoDuration / 1000 / 2) {
                                View visibleView = snapHelper.findSnapView(getLayoutManager());
                                int position = getLayoutManager().getPosition(visibleView);
                                onVideoWatchedListener.onVideoWatched(new PostId(resultArrayList.get(position).getId()));
                            } else {
                                handler.postDelayed(this, 1000);
                            }
                        }
                    }
                    );
                } else {
                    Log.e(TAG, "onIsPlayingChanged: " + "handler stopped" );
                    handler.removeCallbacksAndMessages(null);                }
            }

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
                        seconds = 0;
                        break;
                    case Player.STATE_IDLE:

                        break;
                    case Player.STATE_READY:
                        Log.e(TAG, "onPlayerStateChanged: Ready to play.");
                        // this logic is required to check if the current visible post item is video
                        // because there was a bug when a video from previous post starts playing in the current post

                        boolean isVideo = false;
                        if (resultArrayList != null && !resultArrayList.isEmpty()) {
                            View visibleView = snapHelper.findSnapView(getLayoutManager());
                            int position = getLayoutManager().getPosition(visibleView);
                            if (resultArrayList.get(position).getFiles().get(0).getType().equals("video")) {
                                isVideo = true;
                            }
                        }

                        if (progressBar != null) {
                            progressBar.setVisibility(GONE);
                        }
                        if(!isVideoViewAdded && isVideo){
                            videoDuration = videoPlayer.getDuration();
                            thumbnail.setVisibility(View.INVISIBLE);
                            seconds = 0;
                            addVideoView();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void setOnPlayerReadyListener(OnPlayerReadyListener listener) {
        onPlayerReadyListener = listener;
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

//             if there is more than 1 list-item on the screen
            if (startPosition != endPosition) {
                int startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition);
                int endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition);

                targetPosition = startPositionVideoHeight > endPositionVideoHeight ? startPosition : endPosition;
            } else {
                targetPosition = startPosition;
            }
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
        thumbnail = holder.postImageView;
        frameLayout = holder.itemView.findViewById(R.id.feedListItemFrameLayout);

        videoSurfaceView.setPlayer(videoPlayer);


        viewHolderParent.setOnClickListener(videoViewClickListener);

//        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
//                context, Util.getUserAgent(context, context.getString(R.string.app_name)));
        DataSource.Factory dataSourceFactory =
                new DefaultHttpDataSourceFactory(Util.getUserAgent(context, context.getString(R.string.app_name)), new DefaultBandwidthMeter(), DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS, DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, true);

        if (!resultArrayList.isEmpty() && resultArrayList.get(targetPosition).getFiles().get(0).getType().equals("video")) {
            final String postId = resultArrayList.get(targetPosition).getId();
            String mediaUrl = resultArrayList.get(targetPosition).getFiles().get(0).getFile();
//            String mediaUrl = "https://s3.ca-central-1.amazonaws.com/codingwithmitch/media/VideoPlayerRecyclerView/Sending+Data+to+a+New+Activity+with+Intent+Extras.mp4";
            fullscreenButton.setOnClickListener(v -> {
                listener.onButtonClick(mediaUrl, videoPlayer.getCurrentPosition(), seconds, postId);
                videoPlayer.setPlayWhenReady(false);
            });
            if (mediaUrl != null) {
                HlsMediaSource videoSource = new HlsMediaSource.Factory(dataSourceFactory)
                        .setAllowChunklessPreparation(true)
                        .createMediaSource(Uri.parse(mediaUrl));
                videoPlayer.prepare(videoSource);
                videoSurfaceView.setUseController(true);
//                videoPlayer.setPlayWhenReady(true);
            }
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
    public void removeVideoView(PlayerView videoView) {
        videoPlayer.setPlayWhenReady(false);
        ViewGroup parent = (ViewGroup) videoView.getParent();
        if (parent == null) {
            return;
        }

        int index = parent.indexOfChild(videoView);
        if (index >= 0) {
            parent.removeViewAt(index);
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
//        postPhoto.setVisibility(View.INVISIBLE);
        thumbnail.setVisibility(INVISIBLE);
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

    public void pauseVideo() {
        videoPlayer.setPlayWhenReady(false);
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
        isScrolled[0] = false;
        smoothScrollBy(1, 0);
    }

    public void setPlaybackPosition(long playbackPosition) {
        if (videoPlayer != null) {
            videoPlayer.seekTo(playbackPosition);
            videoPlayer.setPlayWhenReady(true);
        }
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public void setOnVideoWatchedListener(OnVideoWatchedListener onVideoWatchedListener) {
        this.onVideoWatchedListener = onVideoWatchedListener;
    }

}
