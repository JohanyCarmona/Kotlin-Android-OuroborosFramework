package com.example.ouroboros

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import com.example.ouroboros.ouroboros.DataBase.CodesDataBase.SessionCodes.Companion.DONT_LOGGED_USER_CODE
import com.example.ouroboros.utils.ActivityCodes.Companion.LOGIN_CODE
import com.example.ouroboros.utils.ActivityCodes.Companion.LOGIN_CODE_OK
import java.lang.Integer.getInteger
import java.util.*
import kotlin.concurrent.timerTask

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_splash)

        val timer = Timer()
        timer.schedule(timerTask {
            goToMainActivity()
        }, 2000
        )
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}