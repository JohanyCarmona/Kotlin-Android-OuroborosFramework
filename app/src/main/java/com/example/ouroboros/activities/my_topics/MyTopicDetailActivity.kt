package com.example.ouroboros.activities.my_topics

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import com.example.ouroboros.activities.maps.MapsActivity
import com.example.ouroboros.R
import com.example.ouroboros.activities.maps.MapConstants.MapCodes.Companion.SHOW_LOCATION_MAP
import com.example.ouroboros.intent.LocationSerializable
import com.example.ouroboros.intent.TopicSerializable
import com.example.ouroboros.model.*
import com.example.ouroboros.model.room.SesionRoom
import com.example.ouroboros.model.room.topics.TopicRoom
import com.example.ouroboros.model.TableCodes.ColorOuroborosCodes.Companion.NEGATIVE_COLOR
import com.example.ouroboros.model.TableCodes.ColorOuroborosCodes.Companion.NEUTRAL_COLOR
import com.example.ouroboros.model.TableCodes.ColorOuroborosCodes.Companion.POSITIVE_COLOR
import com.example.ouroboros.model.TableCodes.CouplingStateCodes.Companion.DISABLE
import com.example.ouroboros.model.TableCodes.CouplingStateCodes.Companion.ENABLE
import com.example.ouroboros.model.TableCodes.CouplingStateCodes.Companion.WAITING
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.LOCATION_SERIALIZABLE_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.MAP_REQUEST_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.ROLE_TYPE_REQUEST_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.TOPIC_SERIALIZABLE_CODE
import com.example.ouroboros.model.TableCodes.PublicationTypeCodes.Companion.POST
import com.example.ouroboros.model.TableCodes.PublicationTypeCodes.Companion.REQUEST
import com.example.ouroboros.model.TableCodes.PublicationTypeStrings.Companion.PUBLICATION_STRING
import com.example.ouroboros.model.TableCodes.ResourceCategoryStrings.Companion.CATEGORY_STRING
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.APPLICANT
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.HELPER
import com.example.ouroboros.model.TableCodes.RoleTypeStrings.Companion.ROLE_STRING
import com.example.ouroboros.model.TableCodes.RoomCodes.Companion.ROOM_ALPHA
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.COUPLING_TABLE_CODE
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.TOPIC_TABLE_CODE
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.USER_TABLE_CODE
import com.example.ouroboros.model.firebase.couplings.Coupling
import com.example.ouroboros.model.firebase.couplings.CouplingsTable
import com.example.ouroboros.model.firebase.topics.Topic
import com.example.ouroboros.model.firebase.topics.TopicsTable
import com.example.ouroboros.model.firebase.users.User
import com.example.ouroboros.utils.Constants.ActivityCodes.Companion.EDIT_TOPIC_CODE
import com.example.ouroboros.utils.LocalTime
import com.example.ouroboros.utils.Validator
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_my_topic_detail.*

class MyTopicDetailActivity : AppCompatActivity() {
    lateinit private var allMyRoomTopics: List<TopicRoom>
    lateinit private var topic : Topic
    val allMyWaitingCouplings : MutableList<Coupling> = mutableListOf()
    private var enableCoupled : Boolean = false
    private var waitingCoupled : Boolean = false
    private var coupled : Boolean = false
    val allCouplings : MutableList<Coupling> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_topic_detail)
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
        if (!topic.enable){
            setMyTopicDetailTransparency()
        }

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(COUPLING_TABLE_CODE)

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                enableCoupled = false
                waitingCoupled = false
                for (snapshot in dataSnapshot.children) {
                    val coupling = snapshot.getValue(Coupling::class.java)!!
                    val saved : Boolean = when(topic.role_type){
                        APPLICANT -> {
                            coupling.idApplicantTopic == topic.idTopic
                        }
                        HELPER -> {
                            coupling.idHelperTopic == topic.idTopic
                        }
                        else -> {
                            false
                        }
                    }
                    if (saved){
                        when (coupling.coupledState) {
                            ENABLE -> {
                                enableCoupled = true
                            }
                            WAITING -> {
                                allMyWaitingCouplings.add(coupling)
                                waitingCoupled = true
                            }
                        }
                        coupled = enableCoupled || waitingCoupled
                        if (enableCoupled && waitingCoupled) break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("MTDA:587:error", "Failed to read value: ", error.toException())
            }
        })

        iv_bt_edit?.setOnClickListener{
                if (!topic.enable){
                    goToActivity(EDIT_TOPIC_CODE)
                }else{
                    val validator : Validator = Validator()
                    if (validator.isConnected(this)) {
                        if (!enableCoupled){
                            goToActivity(EDIT_TOPIC_CODE)
                        }else {
                            Toast.makeText( this, getString(R.string.msg_error_already_coupled_topic), Toast.LENGTH_SHORT).show()
                        }
                    }else {
                        Toast.makeText( this, getString(R.string.msg_error_network), Toast.LENGTH_SHORT).show()
                    }
                }
            }



        iv_bt_delete?.setOnClickListener{
            if (!enableCoupled) {
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
                                    if (waitingCoupled) {
                                        disableCouplings()
                                    }
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
            } else {
                Toast.makeText( this, getString(R.string.msg_error_cannot_delete_coupled_topic), Toast.LENGTH_SHORT).show()
            }

        }

        iv_bt_show_location?.setOnClickListener {
            showLocation(topic.latitude,topic.longitude)
        }

        iv_coupled_request_topic?.setOnClickListener {
            if (topic.enable){
                val validator : Validator = Validator()
                if (validator.isConnected(this)){
                    if (topic.publication_type < POST){
                        val intent = Intent(this, MyRequestTopicsActivity::class.java)
                        val topic_serializable = TopicSerializable(topic)
                        intent.putExtra(TOPIC_SERIALIZABLE_CODE, topic_serializable).addFlags(FLAG_ACTIVITY_NEW_TASK)
                        this.startActivity(intent)
                    }else if (topic.publication_type == POST){
                        loadMyCouplings(this)
                    }else if (topic.publication_type == REQUEST){
                        //(x12)
                        loadMyRequestCouplings(this)
                    }
                }else {
                    Toast.makeText(this, getString(R.string.msg_error_network), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadMyRequestCouplings(context : Context){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(COUPLING_TABLE_CODE)

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allCouplings.clear()
                var allIsDisabled : Boolean = true
                for (snapshot in dataSnapshot.children) {
                    val coupling = snapshot.getValue(Coupling::class.java)!!
                    when(topic.role_type){
                        HELPER -> {
                            if (coupling.idHelperTopic == topic.idTopic){
                                if(coupling.coupledState != DISABLE){
                                    allIsDisabled = false
                                }
                            }
                        }
                        APPLICANT -> {
                            if (coupling.idApplicantTopic == topic.idTopic){
                                if(coupling.coupledState != DISABLE){
                                    allIsDisabled = false
                                }
                            }
                        }
                    }
                    if (allIsDisabled) break

                }
                if (!allIsDisabled){
                    val intent = Intent(context, MyRequestTopicActivity::class.java)
                    val topic_serializable = TopicSerializable(topic)
                    intent.putExtra(TOPIC_SERIALIZABLE_CODE, topic_serializable).addFlags(FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }else {
                    Toast.makeText(context, getString(R.string.msg_error_requests_denied_already), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG:MRTA:581:error", "Failed to read value: ", error.toException())
            }
        })
    }

    private fun loadMyCouplings(context : Context){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(COUPLING_TABLE_CODE)

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allCouplings.clear()
                var coupled : Boolean = false
                for (snapshot in dataSnapshot.children) {
                    val coupling = snapshot.getValue(Coupling::class.java)!!
                    when(topic.role_type){
                        HELPER -> {
                            if (coupling.idHelperTopic == topic.idTopic){
                                if(coupling.coupledState == DISABLE){
                                    coupled = true
                                }
                            }
                        }
                        APPLICANT -> {
                            if (coupling.idApplicantTopic == topic.idTopic){
                                if(coupling.coupledState == DISABLE){
                                    coupled = true
                                }
                            }
                        }
                    }
                    if (coupled) break

                }
                if (coupled){
                    Toast.makeText(context, getString(R.string.msg_error_requests_denied_already), Toast.LENGTH_SHORT).show()
                }else {
                    Toast.makeText(context, getString(R.string.msg_error_not_found_requests), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG:MRTA:581:error", "Failed to read value: ", error.toException())
            }
        })
    }

    private fun disableCouplings(){
        for (myWaitingCoupling in allMyWaitingCouplings) {
            val tableCouplings : CouplingsTable = CouplingsTable()
            tableCouplings.updateCoupledState(myWaitingCoupling.idCoupling, DISABLE)
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

    private fun setMyTopicDetailTransparency(alpha : Float = ROOM_ALPHA){
        iv_ouroboros_publisher.alpha = alpha
        iv_coupled_request_topic.alpha = alpha
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
        iv_bt_show_location.alpha = alpha
    }

    private fun showMyTopic(topic : Topic){
        tv_category_type_topic_result.text = CATEGORY_STRING[topic.resource_category]

        when (topic.role_type){
            HELPER -> {
                iv_coupled_request_topic.setImageResource(R.mipmap.ic_helper_roletype)
            }
            APPLICANT -> {
                iv_coupled_request_topic.setImageResource(R.mipmap.ic_applicant_roletype)
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
        tv_resource_location_latitude.text = topic.latitude.round(6)
        tv_resource_location_longitude.text = topic.longitude.round(6)
        val localTime : LocalTime = LocalTime()
        tv_publication_date_result.hint = localTime.convertUTCTimeToDefaultDate(topic.publication_date)//.convertLongUTCToTime(topic.publication_date)
    }

    private fun Double.round(decimals: Int = 2): String = "%.${decimals}f".format(this)

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
                val topic_serializable = TopicSerializable(topic)
                intent.putExtra(TOPIC_SERIALIZABLE_CODE, topic_serializable).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this.startActivity(intent)
            }
        }
    }
}


