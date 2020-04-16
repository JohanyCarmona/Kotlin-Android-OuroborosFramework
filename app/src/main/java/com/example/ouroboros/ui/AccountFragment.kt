package com.example.ouroboros.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.ouroboros.R
import com.example.ouroboros.activities.SplashActivity
import com.example.ouroboros.model.TableCodes
import com.example.ouroboros.model.firebase.users.User
import com.example.ouroboros.model.firebase.users.UsersTable
import com.example.ouroboros.utils.Constants.RegistryCodes.Companion.MAX_LENGTH_PASSWORD
import com.example.ouroboros.utils.LocalTime
import com.example.ouroboros.utils.Validator
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.dialog_update_email.view.*
import kotlinx.android.synthetic.main.dialog_update_email.view.et_password
import kotlinx.android.synthetic.main.dialog_update_password.view.*
import kotlinx.android.synthetic.main.dialog_update_username.view.*
import kotlinx.android.synthetic.main.fragment_account.view.*


class AccountFragment : Fragment() {
    lateinit var root : View
    val myFirebaseUser : FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    lateinit var myUser : User
    var foundedUsername : Boolean = false
    var foundedEmail : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root =  inflater.inflate(R.layout.fragment_account, container, false)
        root.bt_logout.setOnClickListener {
            val auth: FirebaseAuth = FirebaseAuth.getInstance()
            auth.signOut()
            goToSplashActivity()
        }

        root.iv_bt_update_email.setOnClickListener{
            dialogUpdateEmail(savedInstanceState)
        }

        root.iv_bt_update_password.setOnClickListener{
            dialogUpdatePassword(savedInstanceState)
        }


        root.iv_bt_update_username.setOnClickListener{
            dialogUpdateUsername(savedInstanceState)
        }

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TableCodes.TableCodes.USER_TABLE_CODE)

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val user : User = snapshot.getValue(
                        User::class.java)!!
                    val myIdUser : String = myFirebaseUser.uid
                    if (user.idUser == myIdUser) {
                        showAccount(user)
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("MTDA_Ouro_fireba:", "Failed to read value.", error.toException())
            }
        })

        return root
    }

    private fun dialogUpdateEmail(savedInstanceState: Bundle?){
        val updateEmailDialog : Dialog = onCreateDialogUpdateEmail(savedInstanceState)
        updateEmailDialog.show()
    }

    private fun updateEmailRealTime(newEmail: String) {
        val usersTable = UsersTable()
        usersTable.update(
            idUser = myUser.idUser,
            email = newEmail,
            username = myUser.username,
            ouroboros = myUser.ouroboros
        )
    }

    private fun updateEmail(newEmail: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user!!.updateEmail(newEmail)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(activity!!, getString(R.string.msg_email_update_succesfully), Toast.LENGTH_SHORT).show()
                    updateEmailRealTime(newEmail)
                }else{
                    Toast.makeText(activity!!, getString(R.string.msg_email_update_error), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUpdateEmail(email : String, password : String, newEmail : String){
        val user = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider
            .getCredential(email, password)
        // Prompt the user to re-provide their sign-in credentials
        user!!.reauthenticate(credential)
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    updateEmail(newEmail)
                }else{
                    Toast.makeText(activity!!, getString(R.string.msg_error_session_access), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun onCreateDialogUpdateEmail(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val viewDialogUpdateEmail : View = inflater.inflate(R.layout.dialog_update_email, null)

            builder.setView(viewDialogUpdateEmail)
                // Add action buttons
                .setPositiveButton(getString(R.string.update),
                    DialogInterface.OnClickListener { dialog, id ->
                        // update the email
                        val stNewEmail : String = viewDialogUpdateEmail.et_new_email.text.toString()
                        val stPassword : String = viewDialogUpdateEmail.et_password.text.toString()

                        if (stPassword.isEmpty() || stNewEmail.isEmpty()){
                            Toast.makeText(activity!!, getString(R.string.msg_error_empty_box), Toast.LENGTH_SHORT).show()
                        } else {
                            if (stPassword.length < MAX_LENGTH_PASSWORD){
                                Toast.makeText(activity!!, getString(R.string.msg_error_match_length_password), Toast.LENGTH_SHORT).show()
                            }else{
                                val stEmail : String = myFirebaseUser.email.toString()
                                if (stEmail.equals(stNewEmail)){
                                    Toast.makeText(activity!!, getString(R.string.msg_error_changed_email), Toast.LENGTH_SHORT).show()
                                }else{
                                    //Create a method to check if the email is valid
                                    val validator : Validator = Validator()
                                    if (!validator.isEmailValid(stNewEmail)){
                                        Toast.makeText(activity!!, getString(R.string.msg_error_valid_email), Toast.LENGTH_SHORT).show()
                                    } else {
                                        if (!validator.isConnected(root.context)){
                                            Toast.makeText(activity!!, getString(R.string.msg_error_network), Toast.LENGTH_SHORT).show()
                                        }else{
                                            foundEmail(stNewEmail)
                                            if (foundedEmail){
                                                Toast.makeText(activity!!, getString(R.string.msg_error_email_exists), Toast.LENGTH_SHORT).show()
                                            }else {
                                                checkUpdateEmail(stEmail, stPassword, stNewEmail)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    })
                .setNegativeButton(getString(R.string.cancel),
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun dialogUpdatePassword(savedInstanceState: Bundle?){
        val updatePasswordDialog : Dialog = onCreateDialogUpdatePassword(savedInstanceState)
        updatePasswordDialog.show()
    }

    private fun onCreateDialogUpdatePassword(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val viewDialogUpdatePassword : View = inflater.inflate(R.layout.dialog_update_password, null)

            builder.setView(viewDialogUpdatePassword)
                // Add action buttons
                .setPositiveButton(getString(R.string.update),
                    DialogInterface.OnClickListener { dialog, id ->
                        // update the password
                        val stOldPassword : String = viewDialogUpdatePassword.et_old_password.text.toString()
                        val stNewPassword : String = viewDialogUpdatePassword.et_new_password.text.toString()
                        val stNewRePassword : String = viewDialogUpdatePassword.et_new_repassword.text.toString()

                        if (stOldPassword.isEmpty() || stNewPassword.isEmpty() || stNewRePassword.isEmpty()){
                            Toast.makeText(activity!!, getString(R.string.msg_error_empty_box), Toast.LENGTH_SHORT).show()
                        } else {
                            //Create a method to check if the user is valid.
                            if (stOldPassword.length < MAX_LENGTH_PASSWORD || stNewPassword.length < MAX_LENGTH_PASSWORD || stNewRePassword.length < MAX_LENGTH_PASSWORD){
                                Toast.makeText(activity!!, getString(R.string.msg_error_match_length_passwords), Toast.LENGTH_SHORT).show()
                            } else {
                                if(stOldPassword == stNewPassword){
                                    Toast.makeText(activity!!, getString(R.string.msg_error_changed_password), Toast.LENGTH_SHORT).show()
                                }else{
                                    if (stNewPassword != stNewRePassword){
                                        Toast.makeText(activity!!, getString(R.string.msg_error_match_new_password), Toast.LENGTH_SHORT).show()
                                    }else{
                                        val validator : Validator = Validator()
                                        if (!validator.isConnected(root.context)){
                                            Toast.makeText(activity!!, getString(R.string.msg_error_network), Toast.LENGTH_SHORT).show()
                                        }else{
                                            val stEmail : String = myFirebaseUser.email.toString()
                                            checkUpdatePassword(stEmail, stOldPassword, stNewPassword)
                                        }
                                    }
                                }
                            }
                        }



                    })
                .setNegativeButton(getString(R.string.cancel),
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun foundEmail(email : String){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TableCodes.TableCodes.USER_TABLE_CODE)
        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                foundedEmail = false
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)!!
                    if (user.email == email) {
                        foundedEmail = true
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
    private fun foundUsername(username : String){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TableCodes.TableCodes.USER_TABLE_CODE)
        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                foundedUsername = false
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)!!
                    if (user.username == username) {
                        foundedUsername = true
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

    private fun updateUsername(newUsername: String) {
        val usersTable = UsersTable()
        usersTable.update(
            idUser = myUser.idUser,
            email = myUser.email,
            username = newUsername,
            ouroboros = myUser.ouroboros
        )
        Toast.makeText(activity!!, getString(R.string.msg_username_update_succesfully), Toast.LENGTH_SHORT).show()
    }


    private fun onCreateDialogUpdateUsername(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            val viewDialogUpdateUsername : View = inflater.inflate(R.layout.dialog_update_username, null)

            builder.setView(viewDialogUpdateUsername)
                // Add action buttons
                .setPositiveButton(getString(R.string.update),
                    DialogInterface.OnClickListener { dialog, id ->
                        val stNewUsername : String = viewDialogUpdateUsername.et_new_username.text.toString()
                        if (stNewUsername.isEmpty()){//if (stPassword.isEmpty() || stNewUsername.isEmpty()){
                            Toast.makeText(activity!!, getString(R.string.msg_error_empty_box), Toast.LENGTH_SHORT).show()
                        } else {
                            val validator : Validator = Validator()
                            if (!validator.isConnected(root.context)){
                                Toast.makeText(activity!!, getString(R.string.msg_error_network), Toast.LENGTH_SHORT).show()
                            }else{
                                val myIdUser : String = myFirebaseUser.uid
                                loadUser(myIdUser)
                                val stUsername : String = myUser.username
                                if(stUsername == stNewUsername){
                                    Toast.makeText(activity!!, getString(R.string.msg_error_changed_username), Toast.LENGTH_SHORT).show()
                                }else{
                                    foundUsername(stUsername)
                                    if (foundedUsername){
                                        Toast.makeText(activity!!, getString(R.string.msg_error_user_exist), Toast.LENGTH_SHORT).show()
                                    }else{
                                        updateUsername(stNewUsername)
                                    }
                                }
                            }
                        }



                    })
                .setNegativeButton(getString(R.string.cancel),
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun dialogUpdateUsername(savedInstanceState: Bundle?){
        val updateUsernameDialog : Dialog = onCreateDialogUpdateUsername(savedInstanceState)
        updateUsernameDialog.show()
    }

    private fun showAccount(user: User){
        root.tv_fragment_account_email_result.text = user.email
        root.tv_fragment_account_user_result.text = user.username
        root.tv_fragment_account_ouroboros_result.text = user.ouroboros.toString()
        //Adapt a function that obtain data in date format.
        val localTime : LocalTime = LocalTime()
        val creationTimestamp : Long = myFirebaseUser.metadata!!.creationTimestamp
        root.tv_fragment_account_registration_date_result.hint = localTime.convertDefaultTimeToDefaultDate(creationTimestamp)
        val lastSignInTimestamp : Long = myFirebaseUser.metadata!!.lastSignInTimestamp
        root.tv_fragment_account_last_sign_in_date_result.hint = localTime.convertDefaultTimeToDefaultDate(lastSignInTimestamp)
    }

    override fun onResume() {
        super.onResume()
        val myIdUser : String = myFirebaseUser.uid
        loadUser(myIdUser)
    }

    private fun loadUser(idUser : String) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TableCodes.TableCodes.USER_TABLE_CODE)

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val user : User = snapshot.getValue(
                        User::class.java)!!
                    if (user.idUser == idUser) {
                        showAccount(user)
                        myUser = user
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

    private fun updatePassword(newPassword: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user!!.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(activity!!, getString(R.string.msg_password_update_succesfully), Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(activity!!, getString(R.string.msg_password_update_error), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUpdatePassword(email : String, oldPassword : String, newPassword : String){
        val user = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider
            .getCredential(email, oldPassword)
        // Prompt the user to re-provide their sign-in credentials
        user!!.reauthenticate(credential)
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    updatePassword(newPassword)
                }else{
                    Toast.makeText(activity!!, getString(R.string.msg_error_session_access), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun goToSplashActivity(){
        val intent = Intent(activity!!.applicationContext, SplashActivity::class.java)
        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
