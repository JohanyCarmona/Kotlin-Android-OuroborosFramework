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
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.LOCATION_SERIALIZABLE_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.MAP_REQUEST_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.ROLE_TYPE_REQUEST_CODE
import com.example.ouroboros.model.room.SesionRoom
import com.example.ouroboros.model.room.topics.TopicRoom
import com.example.ouroboros.model.room.topics.TopicRoomDAO
import com.example.ouroboros.model.TableCodes.PublicationTypeCodes.Companion.POST
import com.example.ouroboros.model.TableCodes.ResourceCategoryCodes.Companion.INDOOR
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.APPLICANT
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.HELPER
import com.example.ouroboros.model.firebase.topics.TopicsTable
import com.example.ouroboros.utils.Constants.ActivityCodes.Companion.MAPS_CODE
import com.example.ouroboros.utils.Constants.ConstantsStrings.Companion.EMPTY
import com.example.ouroboros.utils.Constants.sharedPreferenceKeys.Companion.MAPS_ACTIVITY_KEY
import com.example.ouroboros.utils.Constants.sharedPreferenceVariables.Companion.LATITUDE
import com.example.ouroboros.utils.Constants.sharedPreferenceVariables.Companion.LONGITUDE
import com.example.ouroboros.utils.Constants.sharedPreferenceVariables.Companion.PRESSED
import com.example.ouroboros.utils.LocalTime
import com.example.ouroboros.utils.Validator
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_add_topic.*
import java.sql.Types.NULL

class AddTopicActivity : AppCompatActivity() {
    private var roleType : Int = HELPER
    private var codeRequest : Int = MAPS_CODE
    private var codeResult : Int = MAPS_CODE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_topic)

        val et_title : EditText = findViewById<EditText>(R.id.et_title)
        val et_description : EditText = findViewById<EditText>(R.id.et_description)
        val et_resource_location_latitude : EditText = findViewById<EditText>(R.id.et_resource_location_latitude)
        val et_resource_location_longitude : EditText = findViewById<EditText>(R.id.et_resource_location_longitude)
        val tv_location_resource : TextView = findViewById<TextView>(R.id.tv_location_resource)
        val adapter = ArrayAdapter.createFromResource( this,
            R.array.sp_resource_categories,
            R.layout.support_simple_spinner_dropdown_item
        )
        var resourceCategory : Int = INDOOR


        var topicRoomDao : TopicRoomDAO = SesionRoom.database.TopicRoomDAO()

        rb_helper.setOnClickListener{
            roleType = HELPER
            et_title.setHint(getString(R.string.et_real_resource_title))
            et_description.setHint(getString(R.string.et_real_resource_description))
            tv_location_resource.setText(getString(R.string.tv_real_resource_location))
        }

        rb_applicant.setOnClickListener{
            roleType = APPLICANT
            et_title.setHint(getString(R.string.et_wish_resource_title))
            et_description.setHint(getString(R.string.et_wish_resource_description))
            tv_location_resource.setText(getString(R.string.tv_wish_resource_location))
        }

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
                            topicsTable.create(
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
                        }else{
                            val topicRoom =
                                TopicRoom(
                                    NULL,
                                    idUser = user!!.uid,
                                    role_type = roleType,
                                    publication_type = POST,
                                    title = et_title.text.toString(),
                                    resource_category = resourceCategory,
                                    image = "",
                                    description = et_description.text.toString(),
                                    publication_date = publicationDate,
                                    latitude = et_resource_location_latitude.text.toString()
                                        .toDouble(),
                                    longitude = et_resource_location_longitude.text.toString()
                                        .toDouble(),
                                    enable = false
                                )
                            Thread {
                                topicRoomDao.insertTopic(topicRoom)
                            }.start()
                        }
                        finish()
                    }

                }
            }
        }
    }



    private fun setLocation(latitude : String, longitude : String){
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra(ROLE_TYPE_REQUEST_CODE, roleType)
        intent.putExtra(MAP_REQUEST_CODE, SET_LOCATION_MAP)
        val validator : Validator = Validator()
        lateinit var resourceLocation : LatLng
        resourceLocation = if(validator.isLocationValid(latitude, longitude)){
            LatLng(latitude.toDouble(), longitude.toDouble())
        }else{
            LatLng(OUROBOROS_LOCATION_LATITUDE, OUROBOROS_LOCATION_LONGITUDE)
        }
        Log.d("TAG:ATA:4", ":resourceLocation:$resourceLocation")
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



/*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    codeRequest = requestCode
    codeResult = resultCode
    Log.d("TAG:ATA:-2","onActivityResult:requestCode:"+requestCode.toString()+":resultCode:"+resultCode.toString())
    if (resultCode.equals(MAPS_CODE_OK)){
        //val resourceLocationSerializable: LocationSerializable = data?.getSerializableExtra(LOCATION_SERIALIZABLE_CODE) as LocationSerializable
        //val latitude : Double = data?.extras?.getDouble(LATITUDE_SERIALIZABLE_CODE)!!
        val latitude : Double = data?.extras?.getDouble(LATITUDE_SERIALIZABLE_CODE)!!.toDouble()
        //val longitude: Double = data?.extras?.getDouble(LONGITUDE_SERIALIZABLE_CODE)!!
        val longitude: Double = data?.extras?.getDouble(LONGITUDE_SERIALIZABLE_CODE)!!.toDouble()
        Log.d("TAG:ATA:-1:","intent returned:"+"latitude:"+latitude.toString()+"longitude:"+longitude.toString())
        et_resource_location_latitude.setText(latitude.toString())
        et_resource_location_longitude.setText(longitude.toString())
        Log.d("TAG:ETA:0", "resultCode:$resultCode requestCode:$requestCode")
    }
    super.onActivityResult(requestCode, resultCode, data)
}*/

