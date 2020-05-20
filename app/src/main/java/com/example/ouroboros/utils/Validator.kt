package com.example.ouroboros.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import com.example.ouroboros.model.TableCodes
import java.util.regex.Matcher
import java.util.regex.Pattern

class Validator{
    fun isEmailValid(email: String?): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher : Matcher = pattern.matcher(email)
        return matcher.matches()
    }

    fun isLocationValid(latitude : String?,longitude : String?): Boolean {
        val latitude_expression = "^(\\+|-)?(?:90(?:(?:\\.0{1,})?)|(?:[0-9]|[1-8][0-9])(?:(?:\\.[0-9]{1,})?))$"
        val latitude_pattern: Pattern = Pattern.compile(latitude_expression, Pattern.CASE_INSENSITIVE)
        val latitude_matcher : Matcher = latitude_pattern.matcher(latitude)
        if (latitude_matcher.matches()){
            val longitude_expression = "^(\\+|-)?(?:180(?:(?:\\.0{1,})?)|(?:[0-9]|[1-9][0-9]|1[0-7][0-9])(?:(?:\\.[0-9]{1,})?))$"
            val longitude_pattern: Pattern = Pattern.compile(longitude_expression, Pattern.CASE_INSENSITIVE)
            val longitude_matcher : Matcher = longitude_pattern.matcher(longitude)
            return longitude_matcher.matches()
        }else{
            return false
        }
    }

    fun isNetworkOn (context : Context?) : Boolean {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isNetworkOn: Boolean = activeNetwork?.isConnectedOrConnecting == true
        return isNetworkOn
    }

    fun isInternetOn(url : String): Boolean {
        try {
            val p = java.lang.Runtime.getRuntime().exec("ping -c 1 -w 1 " + url)
            val value = p.waitFor()
            Log.d("TAG:Validator:value",value.toString())
            return value == 0

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("TAG:Validator:Exception",e.toString())
        }
        return false
    }

    fun isConnected (context : Context?) : Boolean {
        val isNetworkConnected = isNetworkOn(context)
        //It's important to create a internet connectivity check function more faster than isInternetOn() which uses a slowly ping.
        /*if(isNetworkConnected){
            val isInternetConnected = isInternetOn(OUROBOROS_DATABASE_IP)
            return isInternetConnected
        }*/
        return isNetworkConnected
    }


    fun invert(anotherRole : Int) : Int {
        return when(anotherRole){
            TableCodes.RoleTypeCodes.HELPER -> {
                TableCodes.RoleTypeCodes.APPLICANT
            }
            TableCodes.RoleTypeCodes.APPLICANT -> {
                TableCodes.RoleTypeCodes.HELPER
            }
            else -> {
                TableCodes.RoleTypeCodes.UNKNOWN_ROLE
            }
        }
    }

}

/*Handler().postDelayed(Runnable {
    if (!isSessionChecked){
        Toast.makeText(activity!!, getString(R.string.msg_error_session_access), Toast.LENGTH_SHORT).show()
    }
}, REAUTHENTICATE_DELAY)*/

/*//PREFERENCES INTO AN ACTIVITY

//Preferences Write Start (OnPause())
var saveID : Int = ManagerSessions.session.ID
if (saveID < 0){
    saveID = DONT_LOGGED_USER_CODE
}
val sharedPref = this?.getPreferences(Context.MODE_PRIVATE) ?: return
with (sharedPref.edit()) {
    putInt("saveID", saveID)
    commit()
}
Log.d("saveID: Write", saveID.toString())
//Preferences Write End

//(0)Preferences Read Start (OnResume()
val sharedPref = this?.getPreferences(Context.MODE_PRIVATE) ?: return
val defaultValue = DONT_LOGGED_USER_CODE
val saveID = sharedPref.getInt("saveID", defaultValue)
Log.d("saveID: Read", saveID.toString())
if (saveID >= 0){
    codeRequest = LOGIN_CODE
    codeResult = LOGIN_CODE_OK
    ManagerSessions.session(saveID)
}
//Preferences Read End

 */
