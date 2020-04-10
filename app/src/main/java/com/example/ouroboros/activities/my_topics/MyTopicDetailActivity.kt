package com.example.ouroboros.activities.my_topics

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import com.example.ouroboros.R
import com.example.ouroboros.intent.TopicSerializable
import com.example.ouroboros.model.*
import com.example.ouroboros.model.room.SesionRoom
import com.example.ouroboros.model.room.topics.TopicRoom
import com.example.ouroboros.model.TableCodes.ColorOuroborosCodes.Companion.NEGATIVE_COLOR
import com.example.ouroboros.model.TableCodes.ColorOuroborosCodes.Companion.NEUTRAL_COLOR
import com.example.ouroboros.model.TableCodes.ColorOuroborosCodes.Companion.POSITIVE_COLOR
import com.example.ouroboros.model.TableCodes.PublicationTypeStrings.Companion.PUBLICATION_STRING
import com.example.ouroboros.model.TableCodes.ResourceCategoryStrings.Companion.CATEGORY_STRING
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.APPLICANT
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.HELPER
import com.example.ouroboros.model.TableCodes.RoleTypeStrings.Companion.ROLE_STRING
import com.example.ouroboros.model.TableCodes.RoomCodes.Companion.ROOM_ALPHA
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.TOPIC_TABLE_CODE
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.TOPIC_SERIALIZABLE_CODE
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.USER_TABLE_CODE
import com.example.ouroboros.model.firebase.topics.Topic
import com.example.ouroboros.model.firebase.topics.TopicsTable
import com.example.ouroboros.model.firebase.users.User
import com.example.ouroboros.utils.Constants.ActivityCodes.Companion.EDIT_TOPIC_CODE
import com.example.ouroboros.utils.LocalTime
import com.example.ouroboros.utils.Validator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_my_topic_detail.*

class MyTopicDetailActivity : AppCompatActivity() {
    lateinit var allMyRoomTopics: List<TopicRoom>
    lateinit var topic : Topic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_topic_detail)

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
        showMyTopic(topic)
        if (!topic.enable){
            setMyTopicDetailTransparency()
        }

        iv_bt_edit?.setOnClickListener{

                if (!topic.enable){
                    goToActivity(EDIT_TOPIC_CODE)
                }else{
                    val validator : Validator = Validator()
                    if (validator.isConnected(this)) {
                        goToActivity(EDIT_TOPIC_CODE)
                    }else {
                        Toast.makeText( this, getString(R.string.msg_error_network), Toast.LENGTH_SHORT).show()
                    }
                }
            }



        iv_bt_delete?.setOnClickListener{
            val context : Context? = this
            //Open window that contains a delete warning
            val alertDialog: AlertDialog? = this.let{//activity?.let{
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage(getString(R.string.msg_delete_question))
                    setPositiveButton(
                        getString(R.string.msg_delete_question_positive)
                    ) { dialog, id ->
                        if (!topic.enable){
                            //Delete Room
                            val tableTopics : TopicsTable =
                                TopicsTable()
                            tableTopics.deleteRoomTopic(topic)
                            finish()
                        }else{
                            val validator : Validator = Validator()
                            if (validator.isConnected(context)) {
                                //Delete Firebase
                                val tableTopics : TopicsTable =
                                    TopicsTable()
                                tableTopics.deleteTopic(topic.idTopic)
                                finish()

                            }else {
                                Toast.makeText( context, getString(R.string.msg_error_network), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    setNegativeButton(
                        getString(R.string.msg_delete_question_negative)
                    ) {dialog, id ->
                    }
                }
                builder.create()
            }
            alertDialog?.show()
        }

        iv_role_type?.setOnClickListener {

        }
    }


    private fun setMyTopicDetailTransparency(alpha : Float = ROOM_ALPHA){
        iv_ouroboros_publisher.alpha = alpha
        iv_role_type.alpha = alpha
        iv_show_topic_resource.alpha = alpha
        tv_role_type.alpha = alpha
        tv_publication_type_result.alpha = alpha
        tv_role_type_topic_result.alpha = alpha
        tv_category_type_topic_result.alpha = alpha
        tv_title_result.alpha = alpha
        tv_description_result.alpha = alpha
        tv_resource_location_latitude.alpha = alpha
        tv_resource_location_longitude.alpha = alpha
        tv_publication_date_result.alpha = alpha
    }

    private fun showMyTopic(topic : Topic){
        tv_category_type_topic_result.text = CATEGORY_STRING[topic.resource_category]

        when (topic.role_type){
            HELPER -> {
                iv_role_type.setImageResource(R.mipmap.ic_helper_roletype)
            }
            APPLICANT -> {
                iv_role_type.setImageResource(R.mipmap.ic_applicant_roletype)
            }
        }
        tv_role_type.setTextColor(Color.parseColor(TableCodes.ColorRoleCodes.ROLE_COLORS[topic.role_type]))
        tv_role_type.text = ROLE_STRING[topic.role_type]
        tv_role_type_topic_result.text = ROLE_STRING[topic.role_type]
        val validator : Validator = Validator()
        if(validator.isConnected(this)){
            loadMyOuroboros(topic.idUser)
        }

        tv_publication_type_result.text = PUBLICATION_STRING[topic.publication_type]

        tv_title_result.text = topic.title

        tv_description_result.text = topic.description

        tv_resource_location_latitude.text = topic.latitude.toString()
        tv_resource_location_longitude.text = topic.longitude.toString()
        val localTime : LocalTime = LocalTime()
        tv_publication_date_result.hint = localTime.convertLongUTCToTime(topic.publication_date)
    }

    override fun onResume() {
        super.onResume()
        if(topic.enable){
            loadMyTopic(topic.idTopic)
            showMyTopic(topic)
        }else{
            if (topic.idTopic.isDigitsOnly()) {
                loadMyRoomTopic(topic.idTopic.toInt())
                showMyTopic(topic)
                setMyTopicDetailTransparency()
            }
        }
    }


    private fun uploadMyRoomTopics(){
        val topicRoomDAO = SesionRoom.database.TopicRoomDAO()
        allMyRoomTopics = topicRoomDAO.getTopics()
        for (MyRoomTopic in allMyRoomTopics){
            val topicsTable : TopicsTable =
                TopicsTable()
            topicsTable.create(
                idUser = MyRoomTopic.idUser,
                role_type = MyRoomTopic.role_type,
                publication_type = MyRoomTopic.publication_type,
                title = MyRoomTopic.title,
                resource_category = MyRoomTopic.resource_category,
                image = MyRoomTopic.image,
                description = MyRoomTopic.description,
                publication_date = MyRoomTopic.publication_date,
                latitude = MyRoomTopic.latitude,
                longitude = MyRoomTopic.longitude,
                enable = true
            )
            topicRoomDAO.deleteTopic(MyRoomTopic)
        }
    }

    private fun setOuroborosPublisher(ouroboros : Double){
        val tv_ouroboros_publisher_result :TextView = findViewById(R.id.tv_ouroboros_publisher_result)
        tv_ouroboros_publisher_result.text = ouroboros.toString()
        if (ouroboros < 0.0){
            tv_ouroboros_publisher_result.setTextColor(Color.parseColor(NEGATIVE_COLOR))
        }
        if (ouroboros == 0.0){
            tv_ouroboros_publisher_result.setTextColor(Color.parseColor(NEUTRAL_COLOR))
        }
        if (ouroboros > 0.0){
            tv_ouroboros_publisher_result.setTextColor(Color.parseColor(POSITIVE_COLOR))
        }
    }

    private fun loadMyOuroboros(idUser : String) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(USER_TABLE_CODE)

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)!!
                    if (user.idUser == idUser) {
                        setOuroborosPublisher(user.ouroboros)
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

    private fun loadMyRoomTopic(myIdTopic : Int){
        val topicRoomDAO = SesionRoom.database.TopicRoomDAO()

        allMyRoomTopics = topicRoomDAO.getTopics()

        if (allMyRoomTopics.isNotEmpty()){
            for (MyRoomTopic in allMyRoomTopics){
                if(MyRoomTopic.idTopic == myIdTopic) {
                        topic =
                            Topic(
                                idTopic = MyRoomTopic.idTopic.toString(),
                                idUser = MyRoomTopic.idUser,
                                role_type = MyRoomTopic.role_type,
                                publication_type = MyRoomTopic.publication_type,
                                title = MyRoomTopic.title,
                                resource_category = MyRoomTopic.resource_category,
                                image = MyRoomTopic.image,
                                description = MyRoomTopic.description,
                                publication_date = MyRoomTopic.publication_date,
                                latitude = MyRoomTopic.latitude,
                                longitude = MyRoomTopic.longitude,
                                enable = MyRoomTopic.enable
                            )
                    break
                }
            }
        }
    }

    private fun loadMyTopic(idTopic : String) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TOPIC_TABLE_CODE)

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val firebase_topic = snapshot.getValue(Topic::class.java)!!
                    if (firebase_topic.idTopic == idTopic) {
                        topic = firebase_topic
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("MTDA:ReadFirebase", "Failed to read value.", error.toException())
            }
        })
    }

    private fun goToActivity(request : Int){
        when(request){
            EDIT_TOPIC_CODE -> {
                val intent = Intent(this, EditTopicActivity::class.java)
                val topic_serializable =
                    TopicSerializable(topic)
                intent.putExtra(TOPIC_SERIALIZABLE_CODE, topic_serializable).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this.startActivity(intent)
            }
        }
    }
}


