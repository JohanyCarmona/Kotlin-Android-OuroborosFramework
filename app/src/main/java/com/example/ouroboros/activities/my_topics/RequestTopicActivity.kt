package com.example.ouroboros.activities.my_topics

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ouroboros.R
import com.example.ouroboros.activities.maps.MapConstants.LocationCodes.Companion.OUROBOROS_LOCATION_LATITUDE
import com.example.ouroboros.activities.maps.MapConstants.LocationCodes.Companion.OUROBOROS_LOCATION_LONGITUDE
import com.example.ouroboros.activities.maps.MapConstants.MapCodes.Companion.SET_LOCATION_MAP
import com.example.ouroboros.activities.maps.MapsActivity
import com.example.ouroboros.intent.LocationSerializable
import com.example.ouroboros.intent.TopicSerializable
import com.example.ouroboros.model.TableCodes
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.LOCATION_SERIALIZABLE_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.MAP_REQUEST_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.ROLE_TYPE_REQUEST_CODE
import com.example.ouroboros.model.TableCodes.PublicationTypeCodes.Companion.POST
import com.example.ouroboros.model.TableCodes.PublicationTypeCodes.Companion.REQUEST
import com.example.ouroboros.model.TableCodes.ResourceCategoryCodes.Companion.INDOOR
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.APPLICANT
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.HELPER
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.UNKNOWN_ROLE
import com.example.ouroboros.model.TableCodes.RoleTypeStrings.Companion.ROLE_STRING
import com.example.ouroboros.model.firebase.topics.Topic
import com.example.ouroboros.model.firebase.topics.TopicsTable
import com.example.ouroboros.utils.Constants.ActivityCodes.Companion.MAPS_CODE
import com.example.ouroboros.utils.Constants.ConstantsStrings.Companion.EMPTY
import com.example.ouroboros.utils.Constants.sharedPreferenceKeys.Companion.MAPS_ACTIVITY_KEY
import com.example.ouroboros.utils.Constants.sharedPreferenceKeys.Companion.REQUEST_TOPIC_ACTIVITY_KEY
import com.example.ouroboros.utils.Constants.sharedPreferenceVariables.Companion.LATITUDE
import com.example.ouroboros.utils.Constants.sharedPreferenceVariables.Companion.LONGITUDE
import com.example.ouroboros.utils.Constants.sharedPreferenceVariables.Companion.MY_ID_TOPIC
import com.example.ouroboros.utils.Constants.sharedPreferenceVariables.Companion.PRESSED
import com.example.ouroboros.utils.Constants.sharedPreferenceVariables.Companion.SAVED
import com.example.ouroboros.utils.LocalTime
import com.example.ouroboros.utils.Validator
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_add_topic.*

class RequestTopicActivity : AppCompatActivity() {
    private var roleType : Int = UNKNOWN_ROLE
    lateinit var topic : Topic
    lateinit var myIdTopic : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_topic)
        supportActionBar?.hide()

        val topic_serializable: TopicSerializable = intent?.getSerializableExtra(TableCodes.IntentCodes.TOPIC_SERIALIZABLE_CODE) as TopicSerializable
        topic = Topic(
            topic_serializable.idTopic,
            topic_serializable.idUser,
            topic_serializable.role_type,
            topic_serializable.publication_type,
            topic_serializable.title,
            topic_serializable.resource_category,
            topic_serializable.image,
            topic_serializable.description,
            topic_serializable.publication_date,
            topic_serializable.latitude,
            topic_serializable.longitude,
            topic_serializable.enable
        )

        val validator : Validator = Validator()
        roleType = validator.invert(topic.role_type)
        val tvRoleTypeResult : TextView = findViewById<TextView>(R.id.tvRoleTypeResult)
        tvRoleTypeResult.text = if (roleType != UNKNOWN_ROLE) {
            ROLE_STRING[roleType]
        }else {
            ROLE_STRING[ROLE_STRING.size - UNKNOWN_ROLE]
        }

        val et_title : EditText = findViewById<EditText>(R.id.et_title)
        val et_description : EditText = findViewById<EditText>(R.id.et_description)
        val et_resource_location_latitude : EditText = findViewById<EditText>(R.id.et_resource_location_latitude)
        val et_resource_location_longitude : EditText = findViewById<EditText>(R.id.et_resource_location_longitude)
        val tv_location_resource : TextView = findViewById<TextView>(R.id.tv_location_resource)
        when (roleType){
            APPLICANT -> {
                tv_location_resource.text = getString(R.string.tv_wish_resource_location)
            }
            HELPER -> {
                tv_location_resource.text = getString(R.string.tv_real_resource_location)
            }
        }

        val adapter = ArrayAdapter.createFromResource( this,
            R.array.sp_resource_categories,
            R.layout.support_simple_spinner_dropdown_item
        )

        var resourceCategory : Int = INDOOR

        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        sp_resource_category.adapter = adapter

        iv_buttom_get_resource_location.setOnClickListener {
            val latitude : String = et_resource_location_latitude.text.toString()
            val longitude : String = et_resource_location_longitude.text.toString()
            setLocation(latitude, longitude)
        }

        bt_add_topic?.setOnClickListener {
            val localTime : LocalTime = LocalTime()
            val publicationDate : Long = localTime.currentTimeToUTC()
            if (et_title.text.toString().isEmpty() || et_description.text.toString().isEmpty() || et_resource_location_latitude.text.toString().isEmpty() || et_resource_location_longitude.text.toString().isEmpty() ){
                Toast.makeText( this, getString(R.string.msg_error_empty_box), Toast.LENGTH_SHORT).show()
            }else{
                val validator : Validator = Validator()
                if (!validator.isLocationValid(et_resource_location_latitude.text.toString(),et_resource_location_longitude.text.toString())){
                    Toast.makeText( this, getString(R.string.msg_error_valid_location), Toast.LENGTH_SHORT).show()
                }else{
                    if (!localTime.isValidLocalTime()){
                        Toast.makeText( this, getString(R.string.msg_error_valid_local_time), Toast.LENGTH_SHORT).show()
                    }else{
                        resourceCategory = sp_resource_category.selectedItemPosition
                        val user = FirebaseAuth.getInstance().currentUser
                        if (validator.isConnected(this)) {
                            val topicsTable : TopicsTable =
                                TopicsTable()
                            myIdTopic = topicsTable.create_(
                                idUser = user!!.uid,
                                role_type = roleType,
                                publication_type = POST,
                                title = et_title.text.toString(),
                                resource_category = resourceCategory,
                                image = "",
                                description = et_description.text.toString(),
                                publication_date = publicationDate,
                                latitude = et_resource_location_latitude.text.toString().toDouble(),
                                longitude = et_resource_location_longitude.text.toString().toDouble(),
                                enable = true
                            )
                            writeMyIdTopicPreferences(myIdTopic, true)
                            finish()
                        }else{
                            Toast.makeText( this, getString(R.string.msg_error_network), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun writeMyIdTopicPreferences(myIdTopic : String, saved : Boolean){
        val sharedPref : SharedPreferences = getSharedPreferences(REQUEST_TOPIC_ACTIVITY_KEY, 0)
        val editor : SharedPreferences.Editor = sharedPref.edit()
        editor.putString(MY_ID_TOPIC, myIdTopic)
        editor.putBoolean(SAVED, saved)
        editor.commit()
    }

    private fun setLocation(latitude : String, longitude : String){
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra(ROLE_TYPE_REQUEST_CODE, this.roleType)
        intent.putExtra(MAP_REQUEST_CODE, SET_LOCATION_MAP)
        val validator : Validator = Validator()
        lateinit var resourceLocation : LatLng
        resourceLocation = if(validator.isLocationValid(latitude, longitude)){
            LatLng(latitude.toDouble(), longitude.toDouble())
        }else{
            LatLng(OUROBOROS_LOCATION_LATITUDE, OUROBOROS_LOCATION_LONGITUDE)
        }
        Log.d("TAG:RTA:435", ":resourceLocation:$resourceLocation")
        val resourceLocationSerializable = LocationSerializable(resourceLocation)
        intent.putExtra(LOCATION_SERIALIZABLE_CODE, resourceLocationSerializable).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivityForResult(intent, MAPS_CODE)
    }

    private fun readLocationPreferences() : HashMap <String, String> {
        val sharedPref : SharedPreferences = getSharedPreferences(MAPS_ACTIVITY_KEY, 0)
        val savePressed : Boolean = sharedPref.getBoolean(PRESSED, false)
        val latitude : String = if(savePressed){sharedPref.getString(LATITUDE, EMPTY)!!}else{EMPTY}
        val longitude : String = if(savePressed){sharedPref.getString(LONGITUDE, EMPTY)!!}else{EMPTY}
        val inputResourceLocation : HashMap <String, String> = hashMapOf(
            LATITUDE to latitude,
            LONGITUDE to longitude
        )
        return inputResourceLocation
    }

    private fun fillLocationEditText(inputResourceLocation : HashMap<String, String>){
        et_resource_location_latitude.setText(inputResourceLocation[LATITUDE])
        et_resource_location_longitude.setText(inputResourceLocation[LONGITUDE])
    }

    override fun onResume() {
        super.onResume()
        fillLocationEditText(readLocationPreferences())
    }

    private fun resetPressedPreferences(){
        val sharedPref : SharedPreferences = getSharedPreferences(MAPS_ACTIVITY_KEY, 0)
        val editor : SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(PRESSED, false)
        editor.commit()
    }

    override fun onPause() {
        resetPressedPreferences()
        super.onPause()
    }

}
