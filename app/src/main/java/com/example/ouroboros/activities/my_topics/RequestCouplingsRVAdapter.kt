package com.example.ouroboros.activities.my_topics

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ouroboros.R
import com.example.ouroboros.activities.topics.CouplingTopicDetailActivity
import com.example.ouroboros.intent.CouplingSerializable
import com.example.ouroboros.intent.TopicSerializable
import com.example.ouroboros.model.TableCodes.CouplingStateCodes.Companion.ENABLE
import com.example.ouroboros.model.TableCodes.CouplingStateCodes.Companion.WAITING
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.COUPLING_SERIALIZABLE_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.MY_ID_TOPIC_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.TOPIC_SERIALIZABLE_CODE
import com.example.ouroboros.model.TableCodes.OuroborosTypeStrings.Companion.OUROBOROS_STRING
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.APPLICANT
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.HELPER
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.UNKNOWN_ROLE
import com.example.ouroboros.model.TableCodes.RoleTypeStrings.Companion.ROLE_STRING
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.TOPIC_TABLE_CODE
import com.example.ouroboros.model.firebase.couplings.Coupling
import com.example.ouroboros.model.firebase.topics.Topic
import com.example.ouroboros.utils.LocalTime
import com.example.ouroboros.utils.Validator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.item_request_coupling.view.*


class RequestCouplingsRVAdapter(
    context: Context,
    couplingsList: ArrayList<Coupling>,
    topicsList: ArrayList<Topic>,
    myIdTopic: String
) : RecyclerView.Adapter<RequestCouplingsRVAdapter.CouplingsViewHolder>() {

    private var topicsList = emptyList<Topic>()
    private var couplingsList = emptyList<Coupling>()
    private var myIdTopic : String
    private val context : Context

    init {
        this.topicsList = topicsList
        this.couplingsList = couplingsList
        this.myIdTopic = myIdTopic
        this.context = context
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RequestCouplingsRVAdapter.CouplingsViewHolder {
        var itemView = LayoutInflater.from(context).inflate(R.layout.item_request_coupling, parent, false) //var
        return CouplingsViewHolder(itemView, context)
    }

    override fun getItemCount(): Int {
        return couplingsList.size
    }

    override fun onBindViewHolder(
        holder: RequestCouplingsRVAdapter.CouplingsViewHolder,
        position: Int
    ) {
        val coupling : Coupling = couplingsList[position]
        val topic : Topic = topicsList[position]
        val myIdTopic : String = myIdTopic
        holder.bindCoupling(coupling, topic, myIdTopic)
    }

    class CouplingsViewHolder(
        itemView : View,
        context : Context
    ) : RecyclerView.ViewHolder(itemView) {
        private var context : Context = context

        fun bindCoupling(coupling: Coupling, topic : Topic, myIdTopic : String) {
            itemView.tv_role_dispatcher.text = ROLE_STRING[coupling.roleDispatcher]
            val validator : Validator = Validator()
            itemView.tv_ouroboros_label.text = OUROBOROS_STRING[validator.invert(topic.role_type)]
            itemView.tv_ouroboros.text = coupling.ouroboros.round(2)

            val localTime : LocalTime = LocalTime()
            itemView.tv_coupled_date.text = localTime.convertUTCTimeToDefaultDate(coupling.coupledDate)

            itemView.setOnClickListener {
                when (coupling.coupledState) {
                    ENABLE -> {
                        val intent = Intent(context, CoupledTopicDetailActivity::class.java)
                        //intent.putExtra(MY_ID_TOPIC_CODE, myIdTopic)
                        val coupling_serializable = CouplingSerializable(coupling)
                        intent.putExtra(COUPLING_SERIALIZABLE_CODE, coupling_serializable).addFlags(FLAG_ACTIVITY_NEW_TASK)
                        val topic_serializable = TopicSerializable(topic)
                        intent.putExtra(TOPIC_SERIALIZABLE_CODE, topic_serializable).addFlags(FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                    WAITING -> {
                        val intent = Intent(context, CouplingTopicDetailActivity::class.java)
                        intent.putExtra(MY_ID_TOPIC_CODE, myIdTopic)
                        val coupling_serializable = CouplingSerializable(coupling)
                        intent.putExtra(COUPLING_SERIALIZABLE_CODE, coupling_serializable).addFlags(FLAG_ACTIVITY_NEW_TASK)
                        val topic_serializable = TopicSerializable(topic)
                        intent.putExtra(TOPIC_SERIALIZABLE_CODE, topic_serializable).addFlags(FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                }
            }
        }

        private fun Double.round(decimals: Int = 2): String = "%.${decimals}f".format(this)

    }
}
