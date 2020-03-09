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
import kotlinx.android.synthetic.main.fragment_my_topics.*
import kotlinx.android.synthetic.main.fragment_my_topics.view.*
import kotlinx.android.synthetic.main.fragment_topics.view.*

class MyTopicsFragment : Fragment() {
    val allMyTopics: MutableList<Topic> = mutableListOf()
    lateinit var myTopicsRVAdapter : TopicsRVAdapter
    lateinit var et_search_my_topic : EditText
    lateinit var iv_buttom_search_my_topic : ImageView
    //This parameter takes my user id to search my topics. (!) When finish the firebase login design, it's necessary update this parameter.
    var myIdUser : String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_my_topics, container, false)
        et_search_my_topic = root.et_search_my_topic
        iv_buttom_search_my_topic = root.iv_buttom_search_my_topic
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

        iv_buttom_search_my_topic?.setOnClickListener {
            if(et_search_my_topic.text.toString().length == 0){
                loadMyTopics()
            }else{
                loadMyTopics(et_search_my_topic.text.toString())
            }
        }

        et_search_my_topic.addTextChangedListener(object : TextWatcher {
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
                val length_et_search_my_topic = s.toString().length
                if (length_et_search_my_topic == 0){
                    loadMyTopics()
                }
            }
        })

        return root
    }

    override fun onResume(){
        super.onResume()
        loadMyTopics()
        //val tableTopics : TopicsTable = TopicsTable()
        //tableTopics.create("abc",0,0,"xyz",0,"pqr","stv","0000000000", 0.0, 0.0,true)
    }

    private fun loadMyTopics() {

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("topics")

        allMyTopics.clear()

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(et_search_my_topic.text.toString().length == 0){
                    for (snapshot in dataSnapshot.children) {
                        val topic = snapshot.getValue(Topic::class.java)!!
                        if (topic.idUser == myIdUser){
                            allMyTopics.add(topic)
                        }
                    }
                    myTopicsRVAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Lista", "Failed to read value.", error.toException())
            }
        })
    }

    private fun loadMyTopics(title : String) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("topics")

        allMyTopics.clear()

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val topic = snapshot.getValue(Topic::class.java)!!
                    if (topic.idUser == myIdUser){
                        if (topic.title == title){
                            allMyTopics.add(topic)
                        }
                    }
                }
                myTopicsRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Lista", "Failed to read value.", error.toException())
            }
        })
    }

}
