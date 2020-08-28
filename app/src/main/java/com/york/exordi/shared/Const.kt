package com.york.exordi.shared

class Const {

    companion object {

        /** Names of the fragments that are used as bottom navigation tabs */
        const val FRAGMENT_FEED = "feedFragment"

        const val FRAGMENT_ADD_POST = "profileFragment"
        const val FRAGMENT_EXPLORE = "exploreFragment"

        /** Values that are passed as extras between activities */
        const val EXTRA_BIRTHDATE = "birthDate"
        const val EXTRA_USERNAME = "username"
        const val EXTRA_COUNTRY = "country"
        const val EXTRA_PROFILE = "profile"
        const val EXTRA_FILE_PATH = "filePath"
        const val EXTRA_CATEGORY = "category"
        const val EXTRA_FILE_TYPE = "fileType"
        const val EXTRA_FILE_TYPE_PHOTO = "image"
        const val EXTRA_FILE_TYPE_VIDEO = "video"
        const val EXTRA_VIDEO_URL = "videoUrl"
        const val EXTRA_PLAYBACK_POSITION = "playbackPosition"
        const val EXTRA_POST = "post"
        const val EXTRA_POST_ID = "postId"
        const val EXTRA_RATING = "rating"
        const val EXTRA_NUMBER_OF_FOLLOWERS = "numberOfFollowers"
        const val EXTRA_FOLLOWERS_CHANGE = "followersChange"
        const val EXTRA_UPVOTES_CHANGE = "upvotesChange"
        const val EXTRA_RATING_CHANGE = "ratingChange"
        const val EXTRA_ACTIVITY_MODE = "mode"
        const val EXTRA_MODE_FOLLOWERS = "Followers"
        const val EXTRA_MODE_FOLLOWINGS = "Followings"

        /** Authentication token */
        const val PREF_AUTH_TOKEN = "authToken"
        const val PREF_REFRESH_TOKEN = "refreshToken"
        const val AUTH = "Authorization"
        const val PREF_USERNAME = "username"

        /** View tags */
        const val TAG_PROFILE = "profile"
        const val TAG_UPVOTE = "upvote"
        const val TAG_COMMENTS = "comments"
        const val TAG_COMMENT_DETAILS = "commentDetails"
        const val TAG_FOLLOW = "follow"
        const val TAG_UNFOLLOW = "unfollow"
        const val TAG_WHOLE_VIEW = "whole"
        const val TAG_DELETE_POST = "deletePost"
    }
}