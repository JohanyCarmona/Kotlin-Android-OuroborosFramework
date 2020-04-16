package com.example.ouroboros.utils

import android.util.Log
import com.example.ouroboros.utils.Constants.DatePatterns.Companion.SESSION_DATE_PATTERN
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUserMetadata
import java.text.SimpleDateFormat
import java.util.*

class LocalTime {

    fun isValidLocalTime() : Boolean {
        val myMetaData : FirebaseUserMetadata = FirebaseAuth.getInstance().currentUser!!.metadata!!
        val lastSignInTimestamp : Long = convertDefaultTimeToUTCTime(myMetaData.lastSignInTimestamp)
        val nowTimestamp : Long = currentTimeToUTC()
        return (lastSignInTimestamp < nowTimestamp)
    }

    //-5 UTCToTime
    fun convertUTCTimeToDefaultDate(UTCTime : Long): String {
        val UTCDate = Date(UTCTime)
        val formatter = SimpleDateFormat(SESSION_DATE_PATTERN, Locale.UK)
        formatter.timeZone = TimeZone.getDefault()
        val defaultDate = formatter.format(UTCDate)
        return defaultDate
    }
    //The AuthTimeStamp from Firebase don't use UTC time. It only uses Default time.
    fun convertDefaultTimeToDefaultDate(defaultTime : Long): String {
        val date : Date = Date(defaultTime)
        val formatter = SimpleDateFormat(SESSION_DATE_PATTERN, Locale.getDefault())
        formatter.timeZone = TimeZone.getDefault()
        val defaultDate : String = formatter.format(date)
        return defaultDate
    }

    private fun convertDefaultTimeToUTCDate(defaultTime : Long): String {
        val defaultDate : Date = Date(defaultTime)
        val formatter = SimpleDateFormat(SESSION_DATE_PATTERN, Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val UTCDate : String = formatter.format(defaultDate)
        return UTCDate
    }

    private fun convertUTCDateToUTCTime(UTCDate : String) : Long {
        val parser = SimpleDateFormat(SESSION_DATE_PATTERN, Locale.UK)//or Default
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val UTCTime : Long = parser.parse(UTCDate).time
        return UTCTime
    }

    private fun convertDefaultTimeToUTCTime(defaultTime : Long): Long {
        val UTCDate : String = convertDefaultTimeToUTCDate(defaultTime)
        val UTCTime : Long = convertUTCDateToUTCTime(UTCDate)
        return UTCTime
    }

    fun currentTimeToUTC(): Long {
        val currentTime : Long = System.currentTimeMillis()
        return convertDefaultTimeToUTCTime(currentTime)
    }
}


/*private fun Date.formatTo(dateFormat: String = SESSION_DATE_PATTERN, timeZone: TimeZone = TimeZone.getDefault()): String {
    val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
    formatter.timeZone = timeZone
    return formatter.format(this)
}*/

/*private fun String.toDate(dateFormat: String = SESSION_DATE_PATTERN, timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Date {
    val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
    parser.timeZone = timeZone
    return parser.parse(this)
}*/