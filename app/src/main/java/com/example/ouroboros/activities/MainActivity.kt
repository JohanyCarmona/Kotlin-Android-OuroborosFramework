package com.example.ouroboros.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.ouroboros.R
import com.example.ouroboros.activities.session.RegistryActivity
import com.example.ouroboros.activities.session.LoginActivity
import com.example.ouroboros.utils.Constants.ActivityCodes.Companion.INIT_CODE
import com.example.ouroboros.utils.Constants.ActivityCodes.Companion.LOGIN_CODE
import com.example.ouroboros.utils.Constants.ActivityCodes.Companion.LOGIN_CODE_BACK
import com.example.ouroboros.utils.Constants.ActivityCodes.Companion.MAIN_CODE
import com.example.ouroboros.utils.Constants.ActivityCodes.Companion.REGISTRY_CODE
import com.example.ouroboros.utils.Constants.ActivityCodes.Companion.REGISTRY_CODE_BACK
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private var codeRequest: Int = LOGIN_CODE
    private var codeResult: Int = INIT_CODE
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_account,
                R.id.navigation_topics,
                R.id.navigation_my_topics
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            codeRequest = MAIN_CODE
            codeResult = INIT_CODE
            /*// Name, email address, and profile photo Url
            val name = user.displayName
            val email = user.email
            val photoUrl = user.photoUrl
            val uid = user.uid*/
        }
    }

    override fun onResume() {
        super.onResume()
        when(codeRequest){
            LOGIN_CODE -> {
                when (codeResult){
                    INIT_CODE -> {
                        goToActivity(LOGIN_CODE)
                    }
                    MAIN_CODE -> {
                        Toast.makeText(this, getString(R.string.msg_user_started), Toast.LENGTH_SHORT).show()
                    }
                    REGISTRY_CODE -> {
                        goToActivity(REGISTRY_CODE)
                    }
                }
            }
            REGISTRY_CODE -> {
                when (codeResult){
                    MAIN_CODE -> {
                        Toast.makeText(this, getString(R.string.msg_user_created), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun goToActivity(request : Int){
        when(request){
            LOGIN_CODE -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivityForResult(intent, request)
            }
            REGISTRY_CODE -> {
                val intent = Intent(this, RegistryActivity::class.java)
                startActivityForResult(intent, request)
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        codeRequest = requestCode
        codeResult = resultCode
        when (requestCode){
            LOGIN_CODE -> {
                when (resultCode) {
                    LOGIN_CODE_BACK -> {
                        finish()
                    }
                }
            }
            REGISTRY_CODE -> {
                when(resultCode){
                    REGISTRY_CODE_BACK -> {
                        codeRequest = LOGIN_CODE
                        codeResult = INIT_CODE
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
