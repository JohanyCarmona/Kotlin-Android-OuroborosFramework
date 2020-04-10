package com.example.ouroboros.activities.my_topics

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ouroboros.R
import com.example.ouroboros.model.room.SesionRoom
import com.example.ouroboros.model.room.topics.TopicRoom
import com.example.ouroboros.model.room.topics.TopicRoomDAO
import com.example.ouroboros.model.TableCodes.PublicationTypeCodes.Companion.POST
import com.example.ouroboros.model.TableCodes.ResourceCategoryCodes.Companion.INDOOR
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.APPLICANT
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.HELPER
import com.example.ouroboros.model.firebase.topics.TopicsTable
import com.example.ouroboros.utils.LocalTime
import com.example.ouroboros.utils.Validator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_add_topic.*
import java.sql.Types.NULL

class AddTopicActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_topic)

        val et_title : EditText = findViewById<EditText>(R.id.et_title)
        val et_description : EditText = findViewById<EditText>(R.id.et_description)
        val et_resource_location_latitude : EditText = findViewById<EditText>(R.id.et_resource_location_latitude)
        val et_resource_location_longitude : EditText = findViewById<EditText>(R.id.et_resource_location_longitude)
        val tv_location_resource : TextView = findViewById<TextView>(R.id.tv_location_resource)
        var roleType : Int = HELPER
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
            //Get location Resource Location / Wish Resource Location
        }

        bt_add_topic?.setOnClickListener {
            val localTime : LocalTime = LocalTime()
            val publicationDate : Long = localTime.currentTimeToUTC()
            if (et_title.text.toString().isEmpty() || et_resource_location_latitude.text.toString().isEmpty() || et_resource_location_longitude.text.toString().isEmpty() ){
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
                            val publicationDate : Long = localTime.currentTimeToUTC()
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

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}


