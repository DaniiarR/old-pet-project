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
        const val EXTRA_IMAGE_PATH = "imagePath"
        const val EXTRA_CATEGORY = "category"

        /** Authentication token */
        const val PREF_AUTH_TOKEN = "authToken"
        const val AUTH = "Authorization"
    }
}