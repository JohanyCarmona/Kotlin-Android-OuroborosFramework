package com.example.ouroboros.activities.topics

import android.content.Context
import android.graphics.Color
import android.icu.lang.UCharacter.LineBreak.SPACE
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.ouroboros.R
import com.example.ouroboros.activities.coin.OuroborosCoin
import com.example.ouroboros.intent.CouplingSerializable
import com.example.ouroboros.intent.TopicSerializable
import com.example.ouroboros.model.TableCodes.ColorOuroborosCodes.Companion.NEGATIVE_COLOR
import com.example.ouroboros.model.TableCodes.ColorOuroborosCodes.Companion.NEUTRAL_COLOR
import com.example.ouroboros.model.TableCodes.ColorOuroborosCodes.Companion.POSITIVE_COLOR
import com.example.ouroboros.model.TableCodes.CouplingStateCodes.Companion.DISABLE
import com.example.ouroboros.model.TableCodes.CouplingStateCodes.Companion.ENABLE
import com.example.ouroboros.model.TableCodes.CouplingStateCodes.Companion.WAITING
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.COUPLING_SERIALIZABLE_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.MY_ID_TOPIC_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.TOPIC_SERIALIZABLE_CODE
import com.example.ouroboros.model.TableCodes.PublicationTypeCodes.Companion.POST
import com.example.ouroboros.model.TableCodes.ResourceCategoryStrings.Companion.CATEGORY_STRING
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.APPLICANT
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.HELPER
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.COUPLING_TABLE_CODE
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.TOPIC_TABLE_CODE
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.USER_TABLE_CODE
import com.example.ouroboros.model.firebase.couplings.Coupling
import com.example.ouroboros.model.firebase.couplings.CouplingsTable
import com.example.ouroboros.model.firebase.topics.Topic
import com.example.ouroboros.model.firebase.topics.TopicsTable
import com.example.ouroboros.model.firebase.users.User
import com.example.ouroboros.utils.LocalTime
import com.example.ouroboros.utils.Validator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_coupling_topic_detail.*

class CouplingTopicDetailActivity : AppCompatActivity() {

    lateinit var coupling : Coupling
    lateinit var topic : Topic
    lateinit var context : Context
    private lateinit var myIdTopic : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coupling_topic_detail)
        supportActionBar?.hide()
        context = this
        myIdTopic = intent?.getStringExtra(MY_ID_TOPIC_CODE) as String
        val coupling_serializable: CouplingSerializable = intent?.getSerializableExtra(COUPLING_SERIALIZABLE_CODE) as CouplingSerializable
        coupling = Coupling(
            coupling_serializable.idCoupling,
            coupling_serializable.idHelperTopic,
            coupling_serializable.idApplicantTopic,
            coupling_serializable.roleDispatcher,
            coupling_serializable.ouroboros,
            coupling_serializable.coupledDate,
            coupling_serializable.coupledState
        )

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

        showTopic(topic)

        iv_coupled_request_topic?.setOnClickListener {
            //Starting coupling
            val validator : Validator = Validator()
            val message : String = when(validator.invert(topic.role_type)) {
                HELPER -> {
                    getString(R.string.helper_my_coupling_warning) +
                            coupling.ouroboros.toString() +
                            getString(R.string.ouroboros)
                }
                APPLICANT -> {
                    getString(R.string.applicant_my_coupling_warning_part_0) +
                            coupling.ouroboros.toString() +
                            getString(R.string.ouroboros) +
                            SPACE +
                            getString(R.string.applicant_coupling_warning_part_1)
                }
                else -> {
                    getString(R.string.invalid_role_type_coupling_warning)
                }
            }
            val alertDialog: AlertDialog? = this?.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setMessage(message)
                    setPositiveButton(
                        getString(R.string.dg_bt_accept)
                    ) { dialog, id ->
                        if (validator.isConnected(context)){
                            coupledTopic()
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

    private fun coupledTopic(){
        val ouroborosCoin : OuroborosCoin = OuroborosCoin()
        val validator : Validator = Validator()
        val toUpdate : Boolean = when (validator.invert(topic.role_type)){
            HELPER -> {
                if (ouroborosCoin.checkCoin(coupling.ouroboros, topic.idUser, context)){
                    ouroborosCoin.cashOuroboros(topic.idUser, - coupling.ouroboros, this)
                    true
                }else false
            }
            APPLICANT -> {
                if (ouroborosCoin.checkCoin(coupling.ouroboros, context)){
                    ouroborosCoin.cashOuroboros(topic.idUser, + coupling.ouroboros, this)
                    true
                }else false
            }
            else -> false
        }
        if (toUpdate) updateStateCoupling(myIdTopic)
    }

    private fun updateStateCoupling(myIdTopic : String){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(COUPLING_TABLE_CODE)

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val coupling = snapshot.getValue(Coupling::class.java)!!
                    val validator : Validator = Validator()
                    if (coupling.coupledState == WAITING){
                        val couplingsTable = CouplingsTable()
                        when (validator.invert(topic.role_type)){
                            HELPER -> {
                                if (coupling.idHelperTopic == myIdTopic){
                                    if (coupling.idApplicantTopic == topic.idTopic){
                                        couplingsTable.updateCoupledState(
                                            idCoupling = coupling.idCoupling,
                                            coupledState = ENABLE
                                        )
                                    }else {
                                        couplingsTable.updateCoupledState(
                                            idCoupling = coupling.idCoupling,
                                            coupledState = DISABLE
                                        )
                                        //val topicsTable : TopicsTable = TopicsTable()
                                        //topicsTable.updatePublicationType(topic.idTopic, POST)
                                    }
                                }
                            }
                            APPLICANT -> {
                                if (coupling.idApplicantTopic == myIdTopic){
                                    if (coupling.idHelperTopic == topic.idTopic){
                                        couplingsTable.updateCoupledState(
                                            idCoupling = coupling.idCoupling,
                                            coupledState = ENABLE
                                        )
                                    }else {
                                        couplingsTable.updateCoupledState(
                                            idCoupling = coupling.idCoupling,
                                            coupledState = DISABLE
                                        )
                                        //val topicsTable : TopicsTable = TopicsTable()
                                        //topicsTable.updatePublicationType(topic.idTopic, POST)
                                    }
                                }
                            }
                        }
                    }
                }
                finish()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TDA:937:error", "Failed to read value: ", error.toException())
            }
        })
    }

    private fun showTopic(topic : Topic){

        tv_category_type_topic_result.text = CATEGORY_STRING[topic.resource_category]

        when (topic.role_type){
            HELPER -> {
                iv_coupled_request_topic.setImageResource(R.mipmap.ic_helper_roletype)
            }
            APPLICANT -> {
                iv_coupled_request_topic.setImageResource(R.mipmap.ic_applicant_roletype)
            }
        }
        tv_title_result.text = topic.title
        tv_description_result.text = topic.description
        val validator : Validator = Validator()
        if(validator.isConnected(this)){
            loadOuroboros(topic.idUser)
        }
        val localTime : LocalTime = LocalTime()
        tv_publication_date_result.hint = localTime.convertUTCTimeToDefaultDate(topic.publication_date)
    }

    override fun onResume() {
        super.onResume()
        loadTopic(topic.idTopic)
        showTopic(topic)

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

    private fun loadOuroboros(idUser : String) {
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
                Log.w("TAG:CTDA:284","Ouro_fireba:"+"Failed to read value.", error.toException())
            }
        })
    }

    private fun loadTopic(idTopic : String) {
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
                Log.w("TAG:CTDA:521", "Failed to read value.", error.toException())
            }
        })
    }

}


