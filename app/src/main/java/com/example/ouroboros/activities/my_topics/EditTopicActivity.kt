package com.example.ouroboros.activities.my_topics

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.LOCATION_SERIALIZABLE_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.MAP_REQUEST_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.ROLE_TYPE_REQUEST_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.TOPIC_SERIALIZABLE_CODE
import com.example.ouroboros.model.room.SesionRoom
import com.example.ouroboros.model.room.topics.TopicRoom
import com.example.ouroboros.model.room.topics.TopicRoomDAO
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.APPLICANT
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.HELPER
import com.example.ouroboros.model.firebase.topics.Topic
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
import kotlinx.android.synthetic.main.activity_add_topic.iv_buttom_get_resource_location
import kotlinx.android.synthetic.main.activity_add_topic.rb_applicant
import kotlinx.android.synthetic.main.activity_add_topic.rb_helper
import kotlinx.android.synthetic.main.activity_add_topic.rg_role_type
import kotlinx.android.synthetic.main.activity_add_topic.sp_resource_category
import kotlinx.android.synthetic.main.activity_edit_topic.*

class EditTopicActivity : AppCompatActivity() {
    lateinit var topic : Topic
    private var roleType : Int = HELPER
    private var codeRequest : Int = MAPS_CODE
    private var codeResult : Int = MAPS_CODE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_topic)

        val topic_serializable: TopicSerializable = intent?.getSerializableExtra(TOPIC_SERIALIZABLE_CODE) as TopicSerializable
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

        val et_title : EditText = findViewById<EditText>(R.id.et_title)
        et_title.setText(topic.title)
        val et_description : EditText = findViewById<EditText>(R.id.et_description)
        et_description.setText(topic.description)
        val et_resource_location_latitude : EditText = findViewById<EditText>(R.id.et_resource_location_latitude)
        et_resource_location_latitude.setText(topic.latitude.toString())
        val et_resource_location_longitude : EditText = findViewById<EditText>(R.id.et_resource_location_longitude)
        et_resource_location_longitude.setText(topic.longitude.toString())
        val tv_location_resource : TextView = findViewById<TextView>(R.id.tv_location_resource)
        roleType = topic.role_type
        rg_role_type.check((rg_role_type.getChildAt(roleType)).getId())
        when(roleType){
            HELPER -> {
                et_title.setHint(getString(R.string.et_real_resource_title))
                et_description.setHint(getString(R.string.et_real_resource_description))
                tv_location_resource.setText(getString(R.string.tv_real_resource_location))
            }
            APPLICANT -> {
                et_title.setHint(getString(R.string.et_wish_resource_title))
                et_description.setHint(getString(R.string.et_wish_resource_description))
                tv_location_resource.setText(getString(R.string.tv_wish_resource_location))
            }
        }

        val adapter = ArrayAdapter.createFromResource( this,
            R.array.sp_resource_categories,
            R.layout.support_simple_spinner_dropdown_item
        )
        var resourceCategory : Int = topic.resource_category

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

        sp_resource_category.setSelection(topic.resource_category)

        iv_buttom_get_resource_location.setOnClickListener {
            //Get location Resource Location / Wish Resource Location
            val latitude : String = et_resource_location_latitude.text.toString()
            val longitude : String = et_resource_location_longitude.text.toString()
            setLocation(latitude, longitude)
        }

        bt_edit_topic?.setOnClickListener {
            val localTime : LocalTime = LocalTime()
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
                        if (!topic.enable){
                            val topicRoomDao: TopicRoomDAO = SesionRoom.database.TopicRoomDAO()
                            val topicsRoom: List<TopicRoom> = topicRoomDao.getTopics()
                            if (topicsRoom.isNotEmpty()){
                                val idTopicRoom: Int = topic.idTopic.toInt()
                                val idTopicRoomResult = topicRoomDao.searchIdTopic(idTopicRoom)
                                if (idTopicRoomResult != null) {
                                    Thread {
                                        topicRoomDao.updateTopic(
                                            TopicRoom(
                                                idTopic = idTopicRoom,
                                                idUser = topic.idUser,
                                                role_type = roleType,
                                                publication_type = topic.publication_type,
                                                title = et_title.text.toString(),
                                                resource_category = resourceCategory,
                                                image = "",
                                                description = et_description.text.toString(),
                                                publication_date = topic.publication_date,
                                                latitude = et_resource_location_latitude.text.toString()
                                                    .toDouble(),
                                                longitude = et_resource_location_longitude.text.toString()
                                                    .toDouble(),
                                                enable = topic.enable
                                            )
                                        )
                                    }.start()
                                    finish()
                                }
                            }

                        }else{
                            if (validator.isConnected(this)){
                                val publicationDate : Long = localTime.currentTimeToUTC()
                                val topicsTable =
                                    TopicsTable()
                                topicsTable.update(
                                    idTopic = topic.idTopic,
                                    idUser = topic.idUser,
                                    role_type = roleType,
                                    publication_type = topic.publication_type,
                                    title = et_title.text.toString(),
                                    resource_category = resourceCategory,
                                    image = "",
                                    description = et_description.text.toString(),
                                    publication_date = publicationDate,
                                    latitude = et_resource_location_latitude.text.toString().toDouble(),
                                    longitude = et_resource_location_longitude.text.toString().toDouble(),
                                    enable = topic.enable
                                )
                                finish()
                            }else{
                                Toast.makeText( this, getString(R.string.msg_error_network), Toast.LENGTH_SHORT).show()
                            }
                        }
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
        val resourceLocation : LatLng = if(validator.isLocationValid(latitude, longitude)){
            LatLng(latitude.toDouble(), longitude.toDouble())
        }else{
            LatLng(OUROBOROS_LOCATION_LATITUDE, OUROBOROS_LOCATION_LONGITUDE)
        }
        val resourceLocationSerializable = LocationSerializable(resourceLocation)
        intent.putExtra(LOCATION_SERIALIZABLE_CODE, resourceLocationSerializable).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivityForResult(intent, MAPS_CODE)
    }

    private fun readLocationPreferences() : HashMap <String, String> {
        val sharedPref : SharedPreferences = getSharedPreferences(MAPS_ACTIVITY_KEY, 0)
        val savePressed : Boolean = sharedPref.getBoolean(PRESSED, false)
        val latitude : String = if(savePressed){sharedPref.getString(LATITUDE, EMPTY)!!}else{topic.latitude.toString()}//.substring(0,9)}
        val longitude : String = if(savePressed){sharedPref.getString(LONGITUDE, EMPTY)!!}else{topic.longitude.toString()}//.substring(0,8)}
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
    Log.d("TAG:ETA:-2","onActivityResult:")
    Log.d("TAG:ETA:0", "resultCode:$resultCode requestCode:$requestCode")
    if (resultCode.equals(MAPS_CODE_OK)){
        //val resourceLocationSerializable: LocationSerializable = data?.getSerializableExtra(LOCATION_SERIALIZABLE_CODE) as LocationSerializable
        //val latitude : Double = data?.extras?.getDouble(LATITUDE_SERIALIZABLE_CODE)!!
        val latitude : Double = data?.extras?.getDouble(LATITUDE_SERIALIZABLE_CODE)!!.toDouble()
        //val longitude: Double = data?.extras?.getDouble(LONGITUDE_SERIALIZABLE_CODE)!!
        val longitude: Double = data?.extras?.getDouble(LONGITUDE_SERIALIZABLE_CODE)!!.toDouble()
        Log.d("TAG:ETA:-1:","intent returned:"+"latitude:"+latitude.toString()+"longitude:"+longitude.toString())
        et_resource_location_latitude.setText(latitude.toString())
        et_resource_location_longitude.setText(longitude.toString())
    }
    super.onActivityResult(requestCode, resultCode, data)
}*/