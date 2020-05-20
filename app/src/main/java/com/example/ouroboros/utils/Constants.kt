package com.example.ouroboros.utils

class Constants{
    class DatePatterns{
        companion object {
            const val SESSION_DATE_PATTERN = "HH:mm:ss MM.dd.yyyy"
        }
    }

    class RegistryCodes{
        companion object{
            const val MAX_LENGTH_PASSWORD = 6
        }
    }

    class ActivityCodes{
        companion object {
            const val INIT_CODE = 0

            const val LOGIN_CODE = 10
            const val LOGIN_CODE_BACK = 11

            const val REGISTRY_CODE = 20
            const val REGISTRY_CODE_BACK = 21

            const val MAIN_CODE = 30

            const val EDIT_TOPIC_CODE = 40

            const val MAPS_CODE = 50
        }
    }

    class ConstantsStrings{
        companion object{
            const val EMPTY = ""
            const val SPACE = " "
        }
    }

    class sharedPreferenceVariables{
        companion object{
            const val LATITUDE = "LATITUDE"
            const val LONGITUDE = "LONGITUDE"
            const val PRESSED = "PRESSED"

            const val MY_ID_TOPIC = "MY_ID_TOPIC"
            const val SAVED = "SAVED"
        }
    }

    class sharedPreferenceKeys{
        companion object{
            const val MAPS_ACTIVITY_KEY = "MAPS_ACTIVITY"

            const val REQUEST_TOPIC_ACTIVITY_KEY = "REQUEST_TOPIC_ACTIVITY"
        }
    }
}
