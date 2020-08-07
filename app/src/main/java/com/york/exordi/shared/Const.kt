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

        /** Authentication token */
        const val PREF_AUTH_TOKEN = "authToken"
        const val PREF_REFRESH_TOKEN = "refreshToken"
        const val AUTH = "Authorization"
    }
}