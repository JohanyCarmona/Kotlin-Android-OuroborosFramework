package com.example.ouroboros.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ouroboros.R
import com.example.ouroboros.model.room.SesionRoom
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.TOPIC_TABLE_CODE
import com.example.ouroboros.model.firebase.topics.Topic
import com.example.ouroboros.model.room.topics.TopicRoom
import com.example.ouroboros.model.firebase.topics.TopicsTable
import com.example.ouroboros.utils.Validator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_topics.view.*


class TopicsFragment : Fragment() {
    val allTopics: MutableList<Topic> = mutableListOf()
    lateinit var allRoomTopics: List<TopicRoom>
    lateinit var topicsRVAdapter : TopicsRVAdapter
    lateinit var etSearchTopic : EditText
    lateinit var fragmentContext : Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_topics, container, false)
        fragmentContext = root.context
        etSearchTopic = root.et_search_topic
        topicsRVAdapter = TopicsRVAdapter(
            activity!!.applicationContext,
            allTopics as ArrayList<Topic>
        )

        root.rv_topics.layoutManager = LinearLayoutManager(
            activity!!.applicationContext,
            RecyclerView.VERTICAL,
            false
        )
        root.rv_topics.setHasFixedSize(true)

        root.rv_topics.adapter = topicsRVAdapter

        etSearchTopic.addTextChangedListener(object : TextWatcher {
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
                val stEtSearchTopic = s.toString()
                if (stEtSearchTopic.isEmpty()){
                    loadTopics()
                }else{
                    loadTopics(stEtSearchTopic)
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
                    uploadRoomTopics()
                }
                allTopics.clear()
                for (snapshot in dataSnapshot.children) {
                    val topic = snapshot.getValue(Topic::class.java)!!
                    if (topic.enable){
                        val stEtSearchTopic : String = etSearchTopic.text.toString()
                        if (stEtSearchTopic.isEmpty()){
                            allTopics.add(topic)
                        }else{
                            if (topic.title.contains(stEtSearchTopic)){
                                allTopics.add(topic)
                            }
                        }
                    }
                }
                topicsRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("MTF:loadTopics()", "Failed to read value.", error.toException())
            }
        })

        return root
    }

    override fun onResume(){
        super.onResume()
        allTopics.clear()
        val stEtSearchTopic : String = etSearchTopic.text.toString()
        if (stEtSearchTopic.isEmpty()){
            loadTopics()
        }else{
            loadTopics(stEtSearchTopic)
        }
        topicsRVAdapter.notifyDataSetChanged()
    }


    private fun loadTopics() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TOPIC_TABLE_CODE)

        val validator : Validator = Validator()
        if (validator.isConnected(context)) {
            uploadRoomTopics()
        }

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val validator : Validator = Validator()
                if (validator.isConnected(fragmentContext)){
                    uploadRoomTopics()
                }
                allTopics.clear()

                for (snapshot in dataSnapshot.children) {
                    val topic = snapshot.getValue(Topic::class.java)!!
                    if (topic.enable){
                        allTopics.add(topic)
                    }
                }
                topicsRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TF:loadTopics()", "Failed to read value.", error.toException())
            }
        })
    }

    private fun loadTopics(title : String) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TOPIC_TABLE_CODE)

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val validator : Validator = Validator()
                if (validator.isConnected(fragmentContext)){
                    uploadRoomTopics()
                }
                allTopics.clear()

                for (snapshot in dataSnapshot.children) {
                    val topic = snapshot.getValue(Topic::class.java)!!
                    if (topic.enable){
                        if (topic.title.contains(title)){
                            allTopics.add(topic)
                        }
                    }
                }
                topicsRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MTF:loadTopics(title)", "Failed to read value.", error.toException())
            }
        })
    }

    private fun uploadRoomTopics(){
        val topicRoomDAO = SesionRoom.database.TopicRoomDAO()
        allRoomTopics = topicRoomDAO.getTopics()
        for (RoomTopic in allRoomTopics){
            val topicsTable : TopicsTable =
                TopicsTable()
            topicsTable.create(
                idUser = RoomTopic.idUser,
                role_type = RoomTopic.role_type,
                publication_type = RoomTopic.publication_type,
                title = RoomTopic.title,
                resource_category = RoomTopic.resource_category,
                image = RoomTopic.image,
                description = RoomTopic.description,
                publication_date = RoomTopic.publication_date,
                latitude = RoomTopic.latitude,
                longitude = RoomTopic.longitude,
                enable = true
            )
            topicRoomDAO.deleteTopic(RoomTopic)
        }
    }

}

