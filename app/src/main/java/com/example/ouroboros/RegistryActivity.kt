package com.example.ouroboros

import android.app.DatePickerDialog
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
import kotlinx.android.synthetic.main.activity_registry.*
import java.text.SimpleDateFormat
import java.util.*


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
                var formate = SimpleDateFormat("dd/MM/yyyy",Locale.US)
                var date = EMPTY
                var gender = getString(R.string.male)
                lateinit var city: String

                rb_male.setOnClickListener{
                    gender = getString(R.string.male)
                }

                rb_female.setOnClickListener{
                    gender = getString(R.string.female)
                }

                etBirthDate.setOnClickListener {
                    val now = Calendar.getInstance()
                    val datePicker = DatePickerDialog(
                        this,
                        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                            val selectedDate = Calendar.getInstance()
                            selectedDate.set(Calendar.YEAR, year)
                            selectedDate.set(Calendar.MONTH, month)
                            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            date = formate.format(selectedDate.time)
                            etBirthDate.setText(date)
                            Log.d("date = ", date)
                        },
                        now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
                    datePicker.show()
                }

                val adapter = ArrayAdapter.createFromResource( this, R.array.city_list, R.layout.support_simple_spinner_dropdown_item)
                adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                spCity.adapter = adapter

                btRegistry.setOnClickListener{
                    val name:String = input_name.text.toString()
                    val email:String = input_email.text.toString()
                    val phone:String = input_phone.text.toString()
                    val password:String = input_password.text.toString()
                    val repassword:String = input_repassword.text.toString()
                    var hobbies:String = EMPTY

                    if (cb_travel.isChecked){
                        if (hobbies != EMPTY) {hobbies = hobbies + ExpressionConstants.COMMA + SPACE}
                        hobbies = hobbies + getString(R.string.travel)
                    }
                    if (cb_swimming.isChecked){
                        if (hobbies != EMPTY) {hobbies = hobbies + ExpressionConstants.COMMA + SPACE}
                        hobbies = hobbies + getString(R.string.swimming)
                    }
                    if (cb_gym.isChecked){
                        if (hobbies != EMPTY) {hobbies = hobbies + ExpressionConstants.COMMA + SPACE}
                        hobbies = hobbies + getString(R.string.gym)
                    }
                    if (cb_running.isChecked){
                        if (hobbies != EMPTY) {hobbies = hobbies + ExpressionConstants.COMMA + SPACE}
                        hobbies = hobbies + getString(R.string.running)
                    }

                    city = spCity.selectedItem.toString()
                    //(!)Later: Create features to use in the App
                    if (name.isEmpty() ||
                        email.isEmpty() ||
                        //phone.isEmpty() ||
                        password.isEmpty() ||
                        repassword.isEmpty() ||
                        //gender.isEmpty() ||
                        //date.isEmpty() ||
                        city.isEmpty()){
                        Toast.makeText( this, getString(R.string.msg_error_empty_box), Toast.LENGTH_SHORT).show()
                    } else {
                        if (password != repassword){
                            Toast.makeText(this, getString(R.string.msg_error_match_password), Toast.LENGTH_SHORT).show()
                        }else {
                            if (password.length < 6){
                                Toast.makeText(this, getString(R.string.msg_error_match_length_password), Toast.LENGTH_SHORT).show()
                            }
                            else {
                                val intent = Intent()
                                intent.putExtra("user", email)
                                intent.putExtra("password", password)
                                intent.putExtra("name", name)
                                intent.putExtra("city", city)
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