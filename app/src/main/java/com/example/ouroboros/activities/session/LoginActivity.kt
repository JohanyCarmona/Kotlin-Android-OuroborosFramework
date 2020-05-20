package com.example.ouroboros.activities.session


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.ouroboros.R
import com.example.ouroboros.utils.Constants.ActivityCodes.Companion.LOGIN_CODE_BACK
import com.example.ouroboros.utils.Constants.ActivityCodes.Companion.MAIN_CODE
import com.example.ouroboros.utils.Constants.ActivityCodes.Companion.REGISTRY_CODE
import com.example.ouroboros.utils.Constants.RegistryCodes.Companion.MAX_LENGTH_PASSWORD
import com.example.ouroboros.utils.Validator
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        val tv_registry = findViewById<TextView>(R.id.tv_registry)
        val bt_login = findViewById<Button>(R.id.bt_login)
        val et_email: EditText = findViewById<EditText>(R.id.et_login_email)
        val et_password: EditText = findViewById<EditText>(R.id.et_login_password)

        tv_registry?.setOnClickListener {
            val intent = Intent()
            setResult(REGISTRY_CODE, intent)
            finish()
        }

        bt_login?.setOnClickListener {
            val st_et_email = et_email.text.toString()
            val st_et_password = et_password.text.toString()
            val auth: FirebaseAuth = FirebaseAuth.getInstance()
            if (st_et_email.isEmpty() || st_et_password.isEmpty()) {
                Toast.makeText(this, getString(R.string.msg_error_empty_box), Toast.LENGTH_SHORT).show()
            } else {
                val validator : Validator = Validator()
                if (!validator.isEmailValid(st_et_email)) {
                    Toast.makeText(this, getString(R.string.msg_error_valid_email), Toast.LENGTH_SHORT).show()
                } else {
                    if (st_et_password.length < MAX_LENGTH_PASSWORD) {
                        Toast.makeText(this, getString(R.string.msg_error_match_length_password), Toast.LENGTH_SHORT).show()
                    } else {
                        if (!validator.isConnected(this)) {
                            Toast.makeText(this, getString(R.string.msg_error_network), Toast.LENGTH_SHORT).show()
                            }else {
                            auth.signInWithEmailAndPassword(st_et_email, st_et_password)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        val intent = Intent()
                                        setResult(MAIN_CODE, intent)
                                        finish()
                                    } else {
                                        Log.w("LA:", "signInWithEmail:failure", task.exception)
                                        when (task.exception!!.message) {
                                            getString(R.string.error_msg_user_exists) -> {
                                                Toast.makeText(this, getString(R.string.msg_error_sesion_email), Toast.LENGTH_SHORT).show()
                                            }
                                            getString(R.string.error_msg_network) -> {
                                                Toast.makeText(this, getString(R.string.msg_error_network), Toast.LENGTH_SHORT).show()
                                            }
                                            getString(R.string.error_msg_credentials_access) -> {
                                                Toast.makeText(this, getString(R.string.msg_error_sesion_password), Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(LOGIN_CODE_BACK, intent)
        super.onBackPressed()
    }

}
