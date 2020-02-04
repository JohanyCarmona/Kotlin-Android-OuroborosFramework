package com.example.ouroboros


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.ouroboros.ouroboros.DataBase.CodesDataBase.SessionCodes.Companion.INVALID_PASSWORD_CODE
import com.example.ouroboros.ouroboros.DataBase.CodesDataBase.SessionCodes.Companion.INVALID_USER_CODE
import com.example.ouroboros.utils.ActivityCodes.Companion.LOGIN_CODE_INIT
import com.example.ouroboros.utils.ActivityCodes.Companion.LOGIN_CODE_NOT
import com.example.ouroboros.utils.ActivityCodes.Companion.LOGIN_CODE_toREGISTRY
import com.example.ouroboros.utils.ActivityCodes.Companion.LOGIN_CODE_toMAIN_ASK

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val tv_registry = findViewById<TextView>(R.id.text_registry)
        val bt_login = findViewById<Button>(R.id.button_login)

        tv_registry?.setOnClickListener {
            val intent = Intent()
            setResult(LOGIN_CODE_toREGISTRY, intent)
            finish()
        }

        bt_login?.setOnClickListener {
            val et_email: String = findViewById<EditText>(R.id.input_email).text.toString()
            val et_password: String = findViewById<EditText>(R.id.input_password).text.toString()
            val intent = Intent()
            intent.putExtra("user", et_email)
            intent.putExtra("password", et_password)
            setResult(LOGIN_CODE_toMAIN_ASK, intent)
            finish()
        }
    }

    override fun onBackPressed() {
        //To disable de 'BackButtom' you must have comment this line.
        //super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        Log.d("Login", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Login", "onResume")
        val codeResult : Int = intent?.extras?.getInt("codeResult")!!
        if (codeResult == LOGIN_CODE_NOT){
            val ID : Int = intent?.extras?.getInt("ID")!!
            lateinit var error_message : String
            when(ID){
                INVALID_USER_CODE ->{
                    error_message = getString(R.string.msg_error_session_user)
                }
                INVALID_PASSWORD_CODE ->{
                    error_message = getString(R.string.msg_error_session_password)
                }
            }
            Toast.makeText(this, error_message, Toast.LENGTH_SHORT).show()
            }
        }

    override fun onPause() {
        super.onPause()
        Log.d("Login", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Login", "onStop")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("Login", "onRestart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Login", "onDestroy")
    }
}

