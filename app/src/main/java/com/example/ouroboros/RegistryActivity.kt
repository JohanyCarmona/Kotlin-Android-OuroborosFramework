package com.example.ouroboros

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.ouroboros.utils.ActivityCodes.Companion.REGISTRY_CODE_BACK
import com.example.ouroboros.utils.ActivityCodes.Companion.REGISTRY_CODE_INIT
import com.example.ouroboros.utils.ActivityCodes.Companion.REGISTRY_CODE_NOT
import com.example.ouroboros.utils.ActivityCodes.Companion.REGISTRY_CODE_toMAIN_ASK
import com.example.ouroboros.utils.ExpressionConstants
import com.example.ouroboros.utils.ExpressionConstants.Companion.EMPTY
import com.example.ouroboros.utils.ExpressionConstants.Companion.SPACE
import com.example.ouroboros.utils.RegistryCodes.Companion.MAX_LENGTH_PASSWORD
import kotlinx.android.synthetic.main.activity_registry.*


class RegistryActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registry)
    }

    override fun onStart() {
        super.onStart()
        Log.d("Registry", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Registry", "onResume")
        val codeResult : Int = intent?.extras?.getInt("codeResult")!!
        when (codeResult){
            REGISTRY_CODE_INIT -> {
                btRegistry.setOnClickListener{
                    val user:String = input_user.text.toString()
                    val password:String = input_password.text.toString()
                    val repassword:String = input_repassword.text.toString()

                    if (user.isEmpty() || password.isEmpty() || repassword.isEmpty()){
                        Toast.makeText( this, getString(R.string.msg_error_empty_box), Toast.LENGTH_SHORT).show()
                    } else {
                        if (password != repassword){
                            Toast.makeText(this, getString(R.string.msg_error_match_password), Toast.LENGTH_SHORT).show()
                        }else {
                            if (password.length < MAX_LENGTH_PASSWORD){
                                Toast.makeText(this, getString(R.string.msg_error_match_length_password), Toast.LENGTH_SHORT).show()
                            }
                            else {
                                val intent = Intent()
                                intent.putExtra("user", user)
                                intent.putExtra("password", password)
                                setResult(REGISTRY_CODE_toMAIN_ASK, intent)
                                finish()
                            }
                        }
                    }
                }
            }
            REGISTRY_CODE_NOT -> {
                Toast.makeText(this, getString(R.string.msg_error_user_exist), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("Registry", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Registry", "onStop")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("Registry", "onRestart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Registry", "onDestroy")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent()
        setResult(REGISTRY_CODE_BACK, intent)
        finish()
    }

}