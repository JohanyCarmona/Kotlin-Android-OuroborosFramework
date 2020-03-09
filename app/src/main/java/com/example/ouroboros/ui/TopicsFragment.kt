package com.example.ouroboros.ui

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
import com.example.ouroboros.R
import com.example.ouroboros.model.Topic
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_topics.view.*


class TopicsFragment : Fragment() {
    val allTopics: MutableList<Topic> = mutableListOf()
    lateinit var topicsRVAdapter : TopicsRVAdapter
    lateinit var et_search_topic : EditText
    lateinit var iv_buttom_search_topic : ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_topics, container, false)
        et_search_topic = root.et_search_topic
        iv_buttom_search_topic = root.iv_buttom_search_topic
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

        iv_buttom_search_topic?.setOnClickListener {
            if(et_search_topic.text.toString().length == 0){
                loadTopics()
            }else{
                loadTopics(et_search_topic.text.toString())
            }
        }

        et_search_topic.addTextChangedListener(object : TextWatcher {
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
                val length_et_search_topic = s.toString().length
                if (length_et_search_topic == 0){
                    loadTopics()
                }
            }
        })


        return root
    }

    override fun onResume(){
        super.onResume()
        loadTopics()
    }


    private fun loadTopics() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("topics")

        allTopics.clear()

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(et_search_topic.text.toString().length == 0){
                    for (snapshot in dataSnapshot.children) {
                        val topic = snapshot.getValue(Topic::class.java)!!
                        allTopics.add(topic)
                    }
                    topicsRVAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Lista", "Failed to read value.", error.toException())
            }
        })
    }

    private fun loadTopics(title : String) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("topics")

        allTopics.clear()

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val topic = snapshot.getValue(Topic::class.java)!!
                    if (topic.title == title){
                        allTopics.add(topic)
                    }
                }
                topicsRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Lista", "Failed to read value.", error.toException())
            }
        })
    }
}

