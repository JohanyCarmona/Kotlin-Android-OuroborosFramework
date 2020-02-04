package com.example.ouroboros

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ouroboros.ouroboros.DataBase
import com.example.ouroboros.ouroboros.DataBase.CodesDataBase.SessionCodes.Companion.DONT_EXIST_USER_CODE
import com.example.ouroboros.utils.ActivityCodes.Companion.INIT_CODE
import com.example.ouroboros.utils.ActivityCodes.Companion.LOGIN_CODE
import com.example.ouroboros.utils.ActivityCodes.Companion.LOGIN_CODE_INIT
import com.example.ouroboros.utils.ActivityCodes.Companion.LOGIN_CODE_NOT
import com.example.ouroboros.utils.ActivityCodes.Companion.LOGIN_CODE_OK
import com.example.ouroboros.utils.ActivityCodes.Companion.LOGIN_CODE_toREGISTRY
import com.example.ouroboros.utils.ActivityCodes.Companion.LOGIN_CODE_toMAIN_ASK
import com.example.ouroboros.utils.ActivityCodes.Companion.REGISTRY_CODE
import com.example.ouroboros.utils.ActivityCodes.Companion.REGISTRY_CODE_BACK
import com.example.ouroboros.utils.ActivityCodes.Companion.REGISTRY_CODE_INIT
import com.example.ouroboros.utils.ActivityCodes.Companion.REGISTRY_CODE_NOT
import com.example.ouroboros.utils.ActivityCodes.Companion.REGISTRY_CODE_toMAIN_ASK


class MainActivity : AppCompatActivity() {

    private val ManagerSessions : DataBase.SessionsManager = DataBase.SessionsManager()
    private var codeRequest: Int = LOGIN_CODE
    private var codeResult: Int = INIT_CODE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("Main", "onCreate")
    }

    override fun onStart() {
        super.onStart()
        Log.d("Main", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Main", "onResume")
        when (codeResult){
            INIT_CODE -> {
                goToActivity(LOGIN_CODE)
            }
            LOGIN_CODE_OK -> {
                this.setTitle(ManagerSessions.session.user)
            }
            LOGIN_CODE_NOT -> {
                goToActivity(LOGIN_CODE, LOGIN_CODE_NOT)
            }
            LOGIN_CODE_toREGISTRY -> {
                goToActivity(REGISTRY_CODE)
            }
            REGISTRY_CODE_NOT -> {
                goToActivity(REGISTRY_CODE, REGISTRY_CODE_NOT)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("Main", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Main", "onStop")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("Main", "onRestart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Main", "onDestroy")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.overflow_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.logout -> {
                this.setTitle(getString(R.string.app_name))
                ManagerSessions.session()
                codeResult = INIT_CODE
                goToActivity(LOGIN_CODE)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goToActivity(request : Int){
        when(request){
            LOGIN_CODE->{
                val intent = Intent(this, LoginActivity::class.java)
                val result = LOGIN_CODE_INIT
                intent.putExtra("codeResult", result)
                startActivityForResult(intent, request)
            }
            REGISTRY_CODE->{
                val intent = Intent(this, RegistryActivity::class.java)
                val result = REGISTRY_CODE_INIT
                intent.putExtra("codeResult", result)
                startActivityForResult(intent, request)
            }
        }
    }

    private fun goToActivity(request : Int, result : Int){
        when(request){
            LOGIN_CODE->{
                codeRequest = LOGIN_CODE
                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("codeResult", result)
                if (result == LOGIN_CODE_NOT){
                    intent.putExtra("ID", ManagerSessions.session.ID)
                }
                startActivityForResult(intent, request)
            }
            REGISTRY_CODE->{
                codeRequest = REGISTRY_CODE
                val intent = Intent(this, RegistryActivity::class.java)
                intent.putExtra("codeResult", result)
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
                    LOGIN_CODE_toMAIN_ASK -> {
                        val user: String = data?.extras?.getString("user").toString()
                        val password: String = data?.extras?.getString("password").toString()
                        ManagerSessions.session(user,password)
                        if (ManagerSessions.session.ID >= 0) {
                            codeResult = LOGIN_CODE_OK
                        } else {
                            codeResult = LOGIN_CODE_NOT
                        }
                    }
                }
            }
            REGISTRY_CODE -> {
                when(resultCode){
                    REGISTRY_CODE_toMAIN_ASK -> {
                        val user : String = data?.extras?.getString("user").toString()
                        val password : String = data?.extras?.getString("password").toString()
                        val name : String = data?.extras?.getString("name").toString()
                        val city : String = data?.extras?.getString("city").toString()
                        if (ManagerSessions.search(user) == DONT_EXIST_USER_CODE){
                            ManagerSessions.write(user, password, name, city)
                            ManagerSessions.session(user, password)
                            codeResult = LOGIN_CODE_OK
                            Toast.makeText(this, getString(R.string.msg_user_created), Toast.LENGTH_SHORT).show()
                        }
                        else{
                            codeResult = REGISTRY_CODE_NOT
                            Toast.makeText(this, getString(R.string.msg_error_user_exist), Toast.LENGTH_SHORT).show()
                        }
                    }
                    REGISTRY_CODE_BACK -> {
                        codeRequest = LOGIN_CODE
                        codeResult = INIT_CODE
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        //To disable de 'BackButtom' you must have comment this line.
        //super.onBackPressed()
    }


}