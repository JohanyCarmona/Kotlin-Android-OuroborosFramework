package com.example.ouroboros.activities.my_topics

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ouroboros.activities.maps.MapsActivity
import com.example.ouroboros.R
import com.example.ouroboros.activities.maps.MapConstants.MapCodes.Companion.SHOW_LOCATION_MAP
import com.example.ouroboros.intent.LocationSerializable
import com.example.ouroboros.intent.TopicSerializable
import com.example.ouroboros.model.TableCodes.ColorOuroborosCodes.Companion.NEGATIVE_COLOR
import com.example.ouroboros.model.TableCodes.ColorOuroborosCodes.Companion.NEUTRAL_COLOR
import com.example.ouroboros.model.TableCodes.ColorOuroborosCodes.Companion.POSITIVE_COLOR
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.LOCATION_SERIALIZABLE_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.MAP_REQUEST_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.ROLE_TYPE_REQUEST_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.TOPIC_SERIALIZABLE_CODE
import com.example.ouroboros.model.TableCodes.PublicationTypeStrings.Companion.PUBLICATION_STRING
import com.example.ouroboros.model.TableCodes.ResourceCategoryStrings.Companion.CATEGORY_STRING
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.APPLICANT
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.HELPER
import com.example.ouroboros.model.TableCodes.RoleTypeStrings.Companion.ROLE_STRING
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.TOPIC_TABLE_CODE
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.USER_TABLE_CODE
import com.example.ouroboros.model.firebase.topics.Topic
import com.example.ouroboros.model.firebase.users.User
import com.example.ouroboros.utils.LocalTime
import com.example.ouroboros.utils.Validator
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_my_topic_detail.iv_bt_show_location
import kotlinx.android.synthetic.main.activity_my_topic_detail.tv_category_type_topic_result
import kotlinx.android.synthetic.main.activity_my_topic_detail.tv_description_result
import kotlinx.android.synthetic.main.activity_my_topic_detail.tv_location_resource
import kotlinx.android.synthetic.main.activity_my_topic_detail.tv_publication_date_result
import kotlinx.android.synthetic.main.activity_my_topic_detail.tv_publication_type_result
import kotlinx.android.synthetic.main.activity_my_topic_detail.tv_resource_location_latitude
import kotlinx.android.synthetic.main.activity_my_topic_detail.tv_resource_location_longitude
import kotlinx.android.synthetic.main.activity_my_topic_detail.tv_role_type_topic_result
import kotlinx.android.synthetic.main.activity_my_topic_detail.tv_title_result

class CoupledTopicDetailActivity : AppCompatActivity() {
    lateinit private var topic : Topic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coupled_topic_detail)
        supportActionBar?.hide()

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

        iv_bt_show_location?.setOnClickListener {
            showLocation(topic.latitude, topic.longitude)
        }
    }

    private fun showLocation(latitude : Double, longitude : Double){
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra(ROLE_TYPE_REQUEST_CODE, topic.role_type)
        intent.putExtra(MAP_REQUEST_CODE, SHOW_LOCATION_MAP)
        val resourceLocation : LatLng = LatLng(latitude, longitude)
        val resourceLocationSerializable = LocationSerializable(resourceLocation)
        intent.putExtra(LOCATION_SERIALIZABLE_CODE, resourceLocationSerializable).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(intent)
    }

    private fun showMyTopic(topic : Topic){
        tv_category_type_topic_result.text = CATEGORY_STRING[topic.resource_category]
        tv_role_type_topic_result.text = ROLE_STRING[topic.role_type]
        val validator : Validator = Validator()
        if(validator.isConnected(this)){
            loadMyOuroboros(topic.idUser)
        }

        tv_publication_type_result.text = PUBLICATION_STRING[topic.publication_type]
        tv_title_result.text = topic.title
        tv_description_result.text = topic.description
        tv_location_resource.text = when (topic.role_type){
            APPLICANT -> {
                getString(R.string.tv_wish_resource_location)
            }
            HELPER -> {
                getString(R.string.tv_real_resource_location)
            }
            else -> {
                getString(R.string.tv_resource_location)
            }
        }
        tv_resource_location_latitude.text = topic.latitude.round(6)
        tv_resource_location_longitude.text = topic.longitude.round(6)
        val localTime : LocalTime = LocalTime()
        tv_publication_date_result.hint = localTime.convertUTCTimeToDefaultDate(topic.publication_date)
    }

    private fun Double.round(decimals: Int = 2): String = "%.${decimals}f".format(this)

    override fun onResume() {
        super.onResume()
        if(topic.enable){
            loadMyTopic(topic.idTopic)
            showMyTopic(topic)
        }
    }

    private fun setOuroborosPublisher(ouroboros : Double){
        val tv_ouroboros_publisher_result : TextView = findViewById(R.id.tv_ouroboros_publisher_result)
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
                Log.w("CTDA:625:", "Failed to read value.", error.toException())
            }
        })
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
                Log.w("TAG:CTDA:346:RF", "Failed to read value.", error.toException())
            }
        })
    }
}


