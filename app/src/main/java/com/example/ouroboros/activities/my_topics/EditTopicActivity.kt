package com.example.ouroboros.activities.my_topics

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ouroboros.R
import com.example.ouroboros.intent.TopicSerializable
import com.example.ouroboros.model.room.SesionRoom
import com.example.ouroboros.model.room.topics.TopicRoom
import com.example.ouroboros.model.room.topics.TopicRoomDAO
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.APPLICANT
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.HELPER
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.TOPIC_SERIALIZABLE_CODE
import com.example.ouroboros.model.firebase.topics.Topic
import com.example.ouroboros.model.firebase.topics.TopicsTable
import com.example.ouroboros.utils.LocalTime
import com.example.ouroboros.utils.Validator
import kotlinx.android.synthetic.main.activity_add_topic.iv_buttom_get_resource_location
import kotlinx.android.synthetic.main.activity_add_topic.rb_applicant
import kotlinx.android.synthetic.main.activity_add_topic.rb_helper
import kotlinx.android.synthetic.main.activity_add_topic.rg_role_type
import kotlinx.android.synthetic.main.activity_add_topic.sp_resource_category
import kotlinx.android.synthetic.main.activity_edit_topic.*

class EditTopicActivity : AppCompatActivity() {

    val topicRoomTable : TopicsTable =
        TopicsTable()
    lateinit var topic : Topic

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
        var roleType : Int = topic.role_type
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
        }

        Log.d("ETA:topic.idTopic",topic.idTopic)
        bt_edit_topic?.setOnClickListener {
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



    override fun onBackPressed() {
        super.onBackPressed()

    }

}

