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
        val lastSignInTimestamp : Long = timezoneToUTC(myMetaData.lastSignInTimestamp)
        val nowTimestamp : Long = currentTimeToUTC()
        return (lastSignInTimestamp < nowTimestamp)
    }

    private fun String.toDate(dateFormat: String = SESSION_DATE_PATTERN, timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Date {
        val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
        parser.timeZone = timeZone
        return parser.parse(this)
    }

    private fun Date.formatTo(dateFormat: String = SESSION_DATE_PATTERN, timeZone: TimeZone = TimeZone.getDefault()): String {
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        formatter.timeZone = timeZone
        return formatter.format(this)
    }


    private fun timezoneToUTC(defaultTime : Long): Long {
        val stDefaultTime : String = convertLongUTCToTime(defaultTime)
        val dateDefaultTime : Date = stDefaultTime.toDate()
        return dateDefaultTime.time
    }

    private fun timezoneToDefault(UTCTime: Long): Long {
        val dateUTCTime : Date = Date(UTCTime)
        val stUTCTime : String = dateUTCTime.formatTo()
        return convertDateToLong(stUTCTime)
    }

    fun currentTimeToUTC(): Long {
        val currentTime : Long = currentTimeToLong()
        return timezoneToUTC(currentTime)
    }

    private fun currentTimeToLong(): Long {
        return System.currentTimeMillis()
    }

    fun convertLongUTCToTime(UTCTime : Long): String {
        val time = timezoneToDefault(UTCTime)
        return convertLongToTime(time)
    }

    private fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat(SESSION_DATE_PATTERN)
        return format.format(date)
    }


    private fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat(SESSION_DATE_PATTERN)
        return df.parse(date).time
    }

}