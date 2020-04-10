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
            const val LOGIN_CODE_BACK = 13

            const val REGISTRY_CODE = 20
            const val REGISTRY_CODE_BACK = 21

            const val MAIN_CODE = 30

            const val EDIT_TOPIC_CODE = 50
        }
    }
}
