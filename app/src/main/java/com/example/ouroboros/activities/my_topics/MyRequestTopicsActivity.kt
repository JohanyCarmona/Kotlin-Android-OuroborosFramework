package com.example.ouroboros.activities.my_topics

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ouroboros.R
import com.example.ouroboros.intent.TopicSerializable
import com.example.ouroboros.model.TableCodes.CouplingStateCodes.Companion.DISABLE
import com.example.ouroboros.model.TableCodes.CouplingStateCodes.Companion.WAITING
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.TOPIC_SERIALIZABLE_CODE
import com.example.ouroboros.model.TableCodes.PublicationTypeCodes.Companion.POST
import com.example.ouroboros.model.TableCodes.PublicationTypeCodes.Companion.REQUEST
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.APPLICANT
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.HELPER
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.COUPLING_TABLE_CODE
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.TOPIC_TABLE_CODE
import com.example.ouroboros.model.firebase.couplings.Coupling
import com.example.ouroboros.model.firebase.couplings.CouplingsTable
import com.example.ouroboros.model.firebase.topics.Topic
import com.example.ouroboros.model.firebase.topics.TopicsTable
import com.example.ouroboros.utils.Validator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_my_request_topics.*

class MyRequestTopicsActivity : AppCompatActivity() {
    private val allCouplingIdTopics : MutableList<String> = mutableListOf()
    private val allCouplingTopics : MutableList<Topic> = mutableListOf()
    private val allMyCouplings : MutableList<Coupling> = mutableListOf()
    lateinit var requestCouplingsRVAdapter : RequestCouplingsRVAdapter
    private lateinit var btEmptyRequest : Button
    private val user = FirebaseAuth.getInstance().currentUser
    private var myIdUser : String = user!!.uid
    private lateinit var topic : Topic
    private lateinit var myIdTopic : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_request_topics)

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

        myIdTopic = topic.idTopic
        requestCouplingsRVAdapter = RequestCouplingsRVAdapter(
            applicationContext,
            allMyCouplings as ArrayList<Coupling>,
            allCouplingTopics as ArrayList<Topic>,
            myIdTopic
        )

        rv_my_request_topics.layoutManager = LinearLayoutManager(
            applicationContext,
            RecyclerView.VERTICAL,
            false
        )

        rv_my_request_topics.setHasFixedSize(true)


        rv_my_request_topics.adapter = requestCouplingsRVAdapter

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(COUPLING_TABLE_CODE)

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allMyCouplings.clear()
                for (snapshot in dataSnapshot.children) {
                    val coupling = snapshot.getValue(Coupling::class.java)!!
                    if (coupling.coupledState == WAITING){
                        when(topic.role_type){
                            HELPER -> {
                                if (coupling.idHelperTopic == topic.idTopic){
                                    Log.d("TAG:MRTA:192:", "Helpcoupling:$coupling")
                                    allMyCouplings.add(coupling)
                                    allCouplingIdTopics.add(coupling.idApplicantTopic)
                                }
                            }
                            APPLICANT -> {
                                if (coupling.idApplicantTopic == topic.idTopic) {
                                    Log.d("TAG:MRTA:268:", "Appcoupling:$coupling")
                                    allMyCouplings.add(coupling)
                                    allCouplingIdTopics.add(coupling.idHelperTopic)
                                }
                            }
                        }
                    }

                }
                //requestCouplingsRVAdapter.notifyDataSetChanged()
                val database = FirebaseDatabase.getInstance()
                val myRef = database.getReference(TOPIC_TABLE_CODE)

                // Read from the database
                myRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        allCouplingIdTopics.clear()
                        allCouplingTopics.clear()
                        for (snapshot in dataSnapshot.children) {
                            val topic = snapshot.getValue(Topic::class.java)!!
                            if (topic.enable){
                                if (topic.publication_type == REQUEST){
                                    val validator : Validator = Validator()
                                    if (topic.role_type == validator.invert(topic.role_type)){
                                        if (allCouplingIdTopics.contains(topic.idTopic)){
                                            Log.d("TAG:MRTA:912:", "topic:$topic")
                                            allCouplingTopics.add(topic)
                                        }
                                    }
                                }
                            }
                        }
                        requestCouplingsRVAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w("TAG:MRTA:193:error", "Failed to read value: ", error.toException())
                    }
                })

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG:MRTA:391:error", "Failed to read value: ", error.toException())
            }
        })

        btEmptyRequest?.setOnClickListener {
            val alertDialog: AlertDialog? = this?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage(getString(R.string.empty_requests_warning))
                    setPositiveButton(
                        getString(R.string.dg_bt_accept)
                    ) { dialog, id ->
                        val validator : Validator = Validator()
                        if (validator.isConnected(context)){
                            val couplingsTable : CouplingsTable = CouplingsTable()
                            val topicsTable : TopicsTable = TopicsTable()
                            for (myCoupling in allMyCouplings){
                                couplingsTable.updateCoupledState(
                                    idCoupling = myCoupling.idCoupling,
                                    coupledState = DISABLE
                                )
                                when (topic.role_type){
                                    HELPER -> {
                                        topicsTable.updatePublicationType(myCoupling.idApplicantTopic, POST)
                                    }
                                    APPLICANT -> {
                                        topicsTable.updatePublicationType(myCoupling.idHelperTopic, POST)
                                    }
                                }
                            }
                            topicsTable.updatePublicationType(topic.idTopic, POST)
                            finish()
                        }else{
                            Toast.makeText( context, getString(R.string.msg_error_network), Toast.LENGTH_SHORT).show()
                        }
                    }
                    setNegativeButton(
                        getString(R.string.dg_bt_cancel)
                    ) { dialog, id ->
                        //
                    }
                }
                builder.create()
            }
            alertDialog?.show()
        }

    }

    override fun onResume(){
        super.onResume()
        allCouplingIdTopics.clear()
        allCouplingTopics.clear()
        allMyCouplings.clear()
        loadMyCouplings()
    }

    private fun loadMyCouplings(){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(COUPLING_TABLE_CODE)

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allMyCouplings.clear()
                for (snapshot in dataSnapshot.children) {
                    val coupling = snapshot.getValue(Coupling::class.java)!!
                    if (coupling.coupledState == WAITING){
                        when(topic.role_type){
                            HELPER -> {
                                if (coupling.idHelperTopic == topic.idTopic){
                                    Log.d("TAG:MRTA:374:", "Helpcoupling:$coupling")
                                    allMyCouplings.add(coupling)
                                    allCouplingIdTopics.add(coupling.idApplicantTopic)
                                }
                            }
                            APPLICANT -> {
                                if (coupling.idApplicantTopic == topic.idTopic) {
                                    Log.d("TAG:MRTA:123:", "Appcoupling:$coupling")
                                    allMyCouplings.add(coupling)
                                    allCouplingIdTopics.add(coupling.idHelperTopic)
                                }
                            }
                        }
                    }

                }
                //requestCouplingsRVAdapter.notifyDataSetChanged()
                loadCouplingTopics()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG:MRTA:581:error", "Failed to read value: ", error.toException())
            }
        })
    }

    private fun loadCouplingTopics(){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TOPIC_TABLE_CODE)

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allCouplingIdTopics.clear()
                allCouplingTopics.clear()
                for (snapshot in dataSnapshot.children) {
                    val topic = snapshot.getValue(Topic::class.java)!!
                    if (topic.enable){
                        if (topic.publication_type == REQUEST){
                            val validator : Validator = Validator()
                            if (topic.role_type == validator.invert(topic.role_type)){
                                if (allCouplingIdTopics.contains(topic.idTopic)){
                                    Log.d("TAG:MRTA:847:", "topic:$topic")
                                    allCouplingTopics.add(topic)
                                }
                            }
                        }
                    }
                }
                requestCouplingsRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG:MRTA:247:error", "Failed to read value: ", error.toException())
            }
        })
    }

}