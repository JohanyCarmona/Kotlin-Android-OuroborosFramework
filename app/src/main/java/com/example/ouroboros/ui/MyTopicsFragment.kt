package com.example.ouroboros.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ouroboros.activities.my_topics.AddTopicActivity
import com.example.ouroboros.R
import com.example.ouroboros.model.room.SesionRoom
import com.example.ouroboros.model.room.topics.TopicRoom
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.TOPIC_TABLE_CODE
import com.example.ouroboros.utils.Validator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_my_topics.view.*
import com.example.ouroboros.model.firebase.topics.Topic
import com.example.ouroboros.model.firebase.topics.TopicsTable

class MyTopicsFragment : Fragment() {
    val allMyTopics : MutableList<Topic> = mutableListOf()
    lateinit var allMyRoomTopics : List<TopicRoom>
    lateinit var myTopicsRVAdapter : TopicsRVAdapter
    lateinit var etSearchMyTopic : EditText
    private lateinit var ivButtomAddMyTopic : ImageView
    val user = FirebaseAuth.getInstance().currentUser
    var myIdUser : String = user!!.uid
    lateinit var fragmentContext : Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_my_topics, container, false)
        fragmentContext = root.context
        etSearchMyTopic = root.et_search_my_topic
        ivButtomAddMyTopic = root.iv_buttom_add_my_topic
        myTopicsRVAdapter = TopicsRVAdapter(
            activity!!.applicationContext,
            allMyTopics as ArrayList<Topic>
        )

        root.rv_my_topics.layoutManager = LinearLayoutManager(
            activity!!.applicationContext,
            RecyclerView.VERTICAL,
            false
        )
        root.rv_my_topics.setHasFixedSize(true)

        root.rv_my_topics.adapter = myTopicsRVAdapter

        ivButtomAddMyTopic?.setOnClickListener{
            val intent = Intent (getActivity()!!, AddTopicActivity::class.java)
            getActivity()!!.startActivity(intent)
        }

        etSearchMyTopic.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
                
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                val stEtSearchMyTopic = s.toString()
                if (stEtSearchMyTopic.isEmpty()){
                    loadMyTopics()
                }else{
                    loadMyTopics(stEtSearchMyTopic)
                }
            }
        })

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TOPIC_TABLE_CODE)
        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val validator : Validator = Validator()
                if (validator.isConnected(fragmentContext)){
                    uploadMyRoomTopics()
                }
                allMyTopics.clear()
                val stEtSearchMyTopic : String = etSearchMyTopic.text.toString()
                if (stEtSearchMyTopic.isEmpty()){
                    loadMyRoomTopics()
                }else{
                    loadMyRoomTopics(stEtSearchMyTopic)
                }

                for (snapshot in dataSnapshot.children) {
                    val topic = snapshot.getValue(Topic::class.java)!!
                    if (topic.enable){
                        if (topic.idUser == myIdUser){
                            if (stEtSearchMyTopic.isEmpty()){
                                allMyTopics.add(topic)
                            }else{
                                if (topic.title.contains(stEtSearchMyTopic)){
                                    allMyTopics.add(topic)
                                }
                            }
                        }
                    }
                }
                myTopicsRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("MTF:loadMyTopics()", "Failed to read value.", error.toException())
            }
        })

        return root
    }

    override fun onResume(){
        super.onResume()
        allMyTopics.clear()
        val stEtSearchMyTopic : String = etSearchMyTopic.text.toString()
        if (stEtSearchMyTopic.isEmpty()){
            loadMyTopics()
        }else{
            loadMyTopics(stEtSearchMyTopic)
        }
        myTopicsRVAdapter.notifyDataSetChanged()
    }

    private fun loadMyTopics() {

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TOPIC_TABLE_CODE)

        val validator : Validator = Validator()
        if (validator.isConnected(context)) {
            uploadMyRoomTopics()
        }else{
            allMyTopics.clear()
            loadMyRoomTopics()
            myTopicsRVAdapter.notifyDataSetChanged()
        }

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val validator : Validator = Validator()
                if (validator.isConnected(fragmentContext)){
                    uploadMyRoomTopics()
                }
                allMyTopics.clear()
                loadMyRoomTopics()

                for (snapshot in dataSnapshot.children) {
                    val topic = snapshot.getValue(Topic::class.java)!!
                    if (topic.enable){
                        if (topic.idUser == myIdUser){
                            allMyTopics.add(topic)
                        }
                    }
                }
                myTopicsRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("MTF:loadMyTopics()", "Failed to read value.", error.toException())
            }
        })
        //
    }

    private fun loadMyTopics(title : String) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TOPIC_TABLE_CODE)

        allMyTopics.clear()
        val validator : Validator = Validator()
        if (validator.isConnected(context)) {
            uploadMyRoomTopics()
        }else{
            loadMyRoomTopics(title)
            myTopicsRVAdapter.notifyDataSetChanged()
        }

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val validator : Validator = Validator()
                if (validator.isConnected(fragmentContext)){
                    uploadMyRoomTopics()
                }
                allMyTopics.clear()
                loadMyRoomTopics()

                for (snapshot in dataSnapshot.children) {
                    val topic = snapshot.getValue(Topic::class.java)!!
                    if (topic.enable){
                        if (topic.idUser == myIdUser){
                            if (topic.title.contains(title)){
                                allMyTopics.add(topic)
                            }
                        }
                    }
                }
                myTopicsRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MTF:loadMyTopics(title)", "Failed to read value.", error.toException())
            }
        })
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

    private fun loadMyRoomTopics(){
            val topicRoomDAO = SesionRoom.database.TopicRoomDAO()
            allMyRoomTopics = topicRoomDAO.getTopics()

            if (allMyRoomTopics.isNotEmpty()) {
                for (MyRoomTopic in allMyRoomTopics) {
                        if (MyRoomTopic.idUser == myIdUser) {
                            val MyTopic: Topic =
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
                            allMyTopics.add(MyTopic)
                        }
                }
            }

    }


    private fun loadMyRoomTopics(title : String){
            val topicRoomDAO = SesionRoom.database.TopicRoomDAO()
            allMyRoomTopics = topicRoomDAO.getTopics()

            if (allMyRoomTopics.isNotEmpty()){
                for (MyRoomTopic in allMyRoomTopics){
                        if(MyRoomTopic.idUser == myIdUser) {
                            if (MyRoomTopic.title.contains(title)) {
                                val MyTopic : Topic =
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
                                allMyTopics.add(MyTopic)
                            }
                        }
                }
            }
    }

}
