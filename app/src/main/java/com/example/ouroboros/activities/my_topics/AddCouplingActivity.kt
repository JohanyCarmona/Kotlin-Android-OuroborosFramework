package com.example.ouroboros.activities.my_topics

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.ouroboros.R
import com.example.ouroboros.activities.coin.OuroborosCoin
import com.example.ouroboros.intent.TopicSerializable
import com.example.ouroboros.model.TableCodes.CouplingStateCodes.Companion.WAITING
import com.example.ouroboros.model.TableCodes.DispatcherRoleTypeStrings.Companion.DISPATCHER_ROLE_STRING
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.MY_ID_TOPIC_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.TOPIC_SERIALIZABLE_CODE
import com.example.ouroboros.model.TableCodes.PublicationTypeCodes.Companion.POST_REQUESTED
import com.example.ouroboros.model.TableCodes.PublicationTypeCodes.Companion.REQUEST
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.APPLICANT
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.HELPER
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.UNKNOWN_ROLE
import com.example.ouroboros.model.firebase.couplings.CouplingsTable
import com.example.ouroboros.model.firebase.topics.Topic
import com.example.ouroboros.model.firebase.topics.TopicsTable
import com.example.ouroboros.utils.Constants
import com.example.ouroboros.utils.Constants.ConstantsStrings.Companion.EMPTY
import com.example.ouroboros.utils.Constants.ConstantsStrings.Companion.SPACE
import com.example.ouroboros.utils.Constants.sharedPreferenceKeys.Companion.REQUEST_TOPIC_ACTIVITY_KEY
import com.example.ouroboros.utils.Constants.sharedPreferenceVariables.Companion.SAVED
import com.example.ouroboros.utils.LocalTime
import com.example.ouroboros.utils.Validator
import kotlinx.android.synthetic.main.activity_add_coupling.*

class AddCouplingActivity : AppCompatActivity() {
    var roleTypeDispatcher : Int = UNKNOWN_ROLE
    lateinit var topic : Topic
    var myRoleType : Int = UNKNOWN_ROLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_coupling)
        supportActionBar?.hide()

        val myIdTopic : String = intent?.getStringExtra(MY_ID_TOPIC_CODE) as String
        Log.d("TAG:ACA:843:myIdTopic:",myIdTopic)

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
        val tv_ouroboros : TextView = findViewById<EditText>(R.id.tv_ouroboros)
        when (myRoleType){
            APPLICANT -> {
                rg_dispatcher_role_type.check(rb_applicant_dispatcher.id)
                rb_applicant_dispatcher.text = DISPATCHER_ROLE_STRING[myRoleType]
                tv_ouroboros.text = getString(R.string.ouroboros_offering_label)
            }
            HELPER -> {
                rg_dispatcher_role_type.check(rb_helper_dispatcher.id)
                rb_helper_dispatcher.text = DISPATCHER_ROLE_STRING[myRoleType]
                tv_ouroboros.text = getString(R.string.ouroboros_load_label)
            }
        }

        rb_helper_dispatcher.setOnClickListener{
            roleTypeDispatcher = HELPER
        }

        rb_applicant_dispatcher.setOnClickListener{
            roleTypeDispatcher = APPLICANT
        }

        val ouroborosCoin : OuroborosCoin = OuroborosCoin()
        ouroborosCoin.updateCoin()
        ouroborosCoin.updateCoin(topic.idTopic)

        bt_add_coupling?.setOnClickListener {
            val localTime : LocalTime = LocalTime()
            val coupledDate : Long = localTime.currentTimeToUTC()
            if ( et_ouroboros.text.toString().isEmpty() ){
                Toast.makeText( this, getString(R.string.msg_error_empty_box), Toast.LENGTH_SHORT).show()
            }else{
                if (localTime.isValidLocalTime()){
                    val validator : Validator = Validator()
                    if (validator.isConnected(this)) {
                        Log.d("TAG:ACA:571:ouroboros:", et_ouroboros.text.toString())
                        val ouroboros : Double = et_ouroboros.text.toString().toDouble()
                        if (ouroborosCoin.checkCoin(ouroboros, this)){
                            val message : String = when(myRoleType) {
                                APPLICANT -> {
                                    getString(R.string.applicant_coupling_warning_part_0) +
                                            SPACE
                                            ouroboros.toString() +
                                            SPACE +
                                            getString(R.string.ouroboros) +
                                            SPACE +
                                            getString(R.string.applicant_coupling_warning_part_1)
                                }
                                HELPER -> {
                                    getString(R.string.helper_coupling_warning) +
                                            SPACE
                                            ouroboros.toString() +
                                            SPACE
                                            getString(R.string.ouroboros)
                                }
                                else -> {
                                    getString(R.string.invalid_role_type_coupling_warning)
                                }
                            }
                            val context : Context = this
                            val alertDialog: AlertDialog? = this?.let {
                                val builder = AlertDialog.Builder(it)
                                builder.apply {
                                    setMessage(message)
                                    setPositiveButton(
                                        getString(R.string.dg_bt_accept)
                                    ) { dialog, id ->
                                        if (validator.isConnected(context)){
                                            val couplingsTable : CouplingsTable = CouplingsTable()
                                            when (myRoleType) {
                                                APPLICANT -> {
                                                    couplingsTable.create(
                                                        idHelperTopic = topic.idTopic,
                                                        idApplicantTopic = myIdTopic,
                                                        roleDispatcher = roleTypeDispatcher,
                                                        ouroboros = ouroboros,
                                                        coupledDate = coupledDate,
                                                        coupledState = WAITING
                                                    )
                                                }
                                                HELPER -> {
                                                    couplingsTable.create(
                                                        idHelperTopic = myIdTopic,
                                                        idApplicantTopic = topic.idTopic,
                                                        roleDispatcher = roleTypeDispatcher,
                                                        ouroboros = ouroboros,
                                                        coupledDate = coupledDate,
                                                        coupledState = WAITING
                                                    )
                                                }
                                            }
                                            val topicsTable = TopicsTable()
                                            topicsTable.updatePublicationType(
                                                idTopic = topic.idTopic,
                                                publication_type = topic.publication_type + POST_REQUESTED
                                            )
                                            topicsTable.updatePublicationType(
                                                idTopic = myIdTopic,
                                                publication_type = REQUEST
                                            )

                                            /*val couplingsTable : CouplingsTable = CouplingsTable()
                                            when (myRoleType){
                                                APPLICANT -> {
                                                    couplingsTable.create(
                                                        idHelperTopic = topic.idTopic,
                                                        idApplicantTopic = myIdTopic,
                                                        roleDispatcher = roleTypeDispatcher,
                                                        ouroboros = ouroboros,
                                                        coupledDate = coupledDate,
                                                        coupledState = WAITING
                                                    )
                                                }
                                                HELPER -> {
                                                    couplingsTable.create(
                                                        idHelperTopic = myIdTopic,
                                                        idApplicantTopic = topic.idTopic,
                                                        roleDispatcher = roleTypeDispatcher,
                                                        ouroboros = ouroboros,
                                                        coupledDate = coupledDate,
                                                        coupledState = WAITING
                                                    )
                                                }
                                            }*/

                                            //resetSavedPreferences()
                                            //finish()
                                        }else{
                                            Toast.makeText( context, getString(R.string.msg_error_network), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    setNegativeButton(
                                        getString(R.string.dg_bt_cancel)
                                    ) { dialog, id ->
                                        //finish()
                                    }
                                }
                                builder.create()
                            }
                            alertDialog?.show()
                            resetSavedPreferences()
                            finish()
                        }
                    }else{
                        Toast.makeText( this, getString(R.string.msg_error_network), Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText( this, getString(R.string.msg_error_invalid_time), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun resetSavedPreferences(){
        val sharedPref : SharedPreferences = getSharedPreferences(REQUEST_TOPIC_ACTIVITY_KEY, 0)
        val editor : SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(SAVED, false)
        editor.commit()
    }

}