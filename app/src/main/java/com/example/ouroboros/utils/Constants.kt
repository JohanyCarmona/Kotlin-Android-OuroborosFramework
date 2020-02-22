package com.example.ouroboros.utils

class ExpressionConstants{
    companion object {
        const val EMPTY = ""
        const val SPACE = " "
        const val ENTER = "\n"
        const val COLON = ":"
        const val COMMA = ","
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
        const val LOGIN_CODE_toMain = 1
        const val LOGIN_CODE_toMAIN_ASK = 2

        const val LOGIN_CODE = 10
        const val LOGIN_CODE_INIT = 11
        const val LOGIN_CODE_OK = 12
        const val LOGIN_CODE_NOT = 13
        const val LOGIN_CODE_BACK = 14
        const val LOGIN_CODE_toREGISTRY = 15

        const val REGISTRY_CODE = 20
        const val REGISTRY_CODE_INIT = 21
        const val REGISTRY_CODE_toMAIN_ASK = 22
        const val REGISTRY_CODE_OK = 23
        const val REGISTRY_CODE_NOT = 24
        const val REGISTRY_CODE_BACK = 25
    }
}