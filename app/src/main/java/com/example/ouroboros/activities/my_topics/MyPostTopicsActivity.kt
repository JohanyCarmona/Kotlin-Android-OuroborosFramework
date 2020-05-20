package com.example.ouroboros.activities.my_topics

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ouroboros.R
import com.example.ouroboros.intent.TopicSerializable
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.TOPIC_SERIALIZABLE_CODE
import com.example.ouroboros.model.TableCodes.PublicationTypeCodes.Companion.POST
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.UNKNOWN_ROLE
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.TOPIC_TABLE_CODE
import com.example.ouroboros.model.firebase.topics.Topic
import com.example.ouroboros.utils.Constants.ConstantsStrings.Companion.EMPTY
import com.example.ouroboros.utils.Constants.sharedPreferenceKeys.Companion.REQUEST_TOPIC_ACTIVITY_KEY
import com.example.ouroboros.utils.Constants.sharedPreferenceVariables.Companion.MY_ID_TOPIC
import com.example.ouroboros.utils.Constants.sharedPreferenceVariables.Companion.SAVED
import com.example.ouroboros.utils.Validator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_my_post_topics.*

class MyPostTopicsActivity : AppCompatActivity() {
    val allMyPostTopics : MutableList<Topic> = mutableListOf()
    lateinit var myPostTopicsRVAdapter : MyPostTopicsRVAdapter
    val user = FirebaseAuth.getInstance().currentUser
    var myIdUser : String = user!!.uid
    private var myRoleType : Int = UNKNOWN_ROLE
    lateinit var topic : Topic
    lateinit var myIdTopic : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_post_topics)
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

        val validator : Validator = Validator()
        myRoleType = validator.invert(topic.role_type)

        myPostTopicsRVAdapter = MyPostTopicsRVAdapter(
            applicationContext,
            allMyPostTopics as ArrayList<Topic>,
            topic
        )

        rv_my_post_topics.layoutManager = LinearLayoutManager(
            applicationContext,
            RecyclerView.VERTICAL,
            false
        )

        rv_my_post_topics.setHasFixedSize(true)

        rv_my_post_topics.adapter = myPostTopicsRVAdapter

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TOPIC_TABLE_CODE)

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allMyPostTopics.clear()
                for (snapshot in dataSnapshot.children) {
                    val myPostTopic = snapshot.getValue(Topic::class.java)!!
                    if (myPostTopic.enable){
                        if (myPostTopic.idUser == myIdUser){
                            if (myPostTopic.publication_type == POST ){
                                if (myPostTopic.role_type == myRoleType) {
                                    Log.d("TAG:MPTA:392:mPTopic",myPostTopic.toString())
                                    allMyPostTopics.add(myPostTopic)
                                }
                            }
                        }
                    }
                }
                myPostTopicsRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG:MPTA:391:error:", "Failed to read value.", error.toException())
            }
        })

        /*rv_my_post_topics.setOnClickListener {
            myIdTopic = readMyIdTopicPreferences()
            Log.d("TAG:MPTA:402:rv_m_p_t", myIdTopic)
            if (myIdTopic.isNotEmpty()){
                finish()
            }
        }*/

    }

    override fun onResume(){
        super.onResume()
        Log.d("TAG:MPTA:456","onResume")
        loadMyPostTopics()
        myPostTopicsRVAdapter.notifyDataSetChanged()
        myIdTopic = readMyIdTopicPreferences()
        Log.d("TAG:MPTA:432:rv_m_p_t", myIdTopic)
        if (myIdTopic.isNotEmpty()){
            finish()
        }
    }

    private fun loadMyPostTopics(){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TOPIC_TABLE_CODE)

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allMyPostTopics.clear()
                for (snapshot in dataSnapshot.children) {
                    val myPostTopic = snapshot.getValue(Topic::class.java)!!
                    if (myPostTopic.enable){
                        if (myPostTopic.idUser == myIdUser){
                            if (myPostTopic.publication_type == POST ){
                                if (myPostTopic.role_type == myRoleType) {
                                    Log.d("TAG:MPTA:853:mPTopic",myPostTopic.toString())
                                    allMyPostTopics.add(myPostTopic)
                                }
                            }
                        }
                    }
                }
                myPostTopicsRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG:MPTA:137:error:", "Failed to read value.", error.toException())
            }
        })
    }

    private fun readMyIdTopicPreferences() : String {
        val sharedPref : SharedPreferences = getSharedPreferences(REQUEST_TOPIC_ACTIVITY_KEY, 0)
        val saved : Boolean = sharedPref.getBoolean(SAVED, false)
        Log.d("TAG:MPTA:932:saved",saved.toString())
        return if (saved){
            Log.d("TAG:MPTA:392:M_I_TOPIC",
                sharedPref.getString(MY_ID_TOPIC, EMPTY)!!)
            sharedPref.getString(MY_ID_TOPIC, EMPTY)!!
        }else {
            EMPTY
        }
    }
}