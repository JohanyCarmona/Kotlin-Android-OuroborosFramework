package com.example.ouroboros.activities.session

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.ouroboros.R
import com.example.ouroboros.model.TableCodes
import com.example.ouroboros.model.TableCodes.OuroborosCodes.Companion.OUROBOROS_INIT
import com.example.ouroboros.model.firebase.users.User
import com.example.ouroboros.utils.Constants.ActivityCodes.Companion.MAIN_CODE
import com.example.ouroboros.utils.Constants.ActivityCodes.Companion.REGISTRY_CODE_BACK
import com.example.ouroboros.utils.Constants.RegistryCodes.Companion.MAX_LENGTH_PASSWORD
import com.example.ouroboros.utils.Validator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_registry.*


class RegistryActivity : AppCompatActivity() {
    var foundedUser : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registry)
        supportActionBar?.hide()

        et_registry_username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                val stEtUsername = s.toString()
                if (stEtUsername.isNotEmpty()){
                    foundUser(stEtUsername)
                }
            }
        })

        bt_registry.setOnClickListener{
            val et_email : EditText = findViewById<EditText>(R.id.et_registry_email)
            val et_username : EditText = findViewById<EditText>(R.id.et_registry_username)
            val et_password : EditText = findViewById<EditText>(R.id.et_registry_password)
            val et_repassword : EditText = findViewById<EditText>(R.id.et_registry_repassword)

            val st_email : String = et_email.text.toString()
            val st_username : String = et_username.text.toString()
            val st_password : String = et_password.text.toString()
            val st_repassword : String = et_repassword.text.toString()

            val auth : FirebaseAuth = FirebaseAuth.getInstance()

            if (st_email.isEmpty() || st_username.isEmpty() || st_password.isEmpty() || st_repassword.isEmpty()){
                Toast.makeText( this, getString(R.string.msg_error_empty_box), Toast.LENGTH_SHORT).show()
            } else {
                val validator : Validator = Validator()
                if (!validator.isEmailValid(st_email))  {
                    Toast.makeText(this, getString(R.string.msg_error_valid_email), Toast.LENGTH_SHORT).show()
                }else{
                    if(!validator.isConnected(this)){
                        Toast.makeText(this, getString(R.string.msg_error_network), Toast.LENGTH_SHORT).show()
                    }else{
                        foundUser(st_username)
                        if (foundedUser){
                            Toast.makeText(this, getString(R.string.msg_error_user_exist), Toast.LENGTH_SHORT).show()
                        }else{
                            if (st_password.length < MAX_LENGTH_PASSWORD || st_repassword.length < MAX_LENGTH_PASSWORD){
                                Toast.makeText(this, getString(R.string.msg_error_match_length_password), Toast.LENGTH_SHORT).show()
                            } else {
                                if (st_password != st_repassword){
                                    Toast.makeText(this, getString(R.string.msg_error_match_password), Toast.LENGTH_SHORT).show()
                                }else{
                                    auth.createUserWithEmailAndPassword(st_email, st_password)
                                        .addOnCompleteListener(this) { task ->
                                            if (task.isSuccessful) {
                                                val user: FirebaseUser? = auth.currentUser
                                                createUserDatabase(user, st_username)
                                                val intent = Intent()
                                                setResult(MAIN_CODE, intent)
                                                finish()
                                            } else {
                                                when(task.exception!!.message){
                                                    getString(R.string.error_msg_network) -> {
                                                        Toast.makeText(this, getString(
                                                            R.string.msg_error_network
                                                        ), Toast.LENGTH_SHORT).show()
                                                    }
                                                    getString(R.string.error_msg_email_exists) -> {
                                                        Toast.makeText(this, getString(
                                                            R.string.msg_error_email_exists
                                                        ), Toast.LENGTH_SHORT).show()
                                                    }
                                                    else -> {
                                                        Log.d("TAG:RA:task.exception",task.exception!!.message)
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
        }
    }

    private fun foundUser(username : String){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TableCodes.TableCodes.USER_TABLE_CODE)
        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                foundedUser = false
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)!!
                    if (user.username == username) {
                        foundedUser = true
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("MTDA_Ouro_fireba:", "Failed to read value.", error.toException())
            }
        })
    }

    private fun createUserDatabase(userFirebase : FirebaseUser?, username : String){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users")
        val new_user = User(
            userFirebase!!.uid,
            userFirebase!!.email.toString(),
            username,
            OUROBOROS_INIT
        )
        myRef.child(userFirebase!!.uid).setValue(new_user)
    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(REGISTRY_CODE_BACK, intent)
        finish()
        super.onBackPressed()
    }

}
