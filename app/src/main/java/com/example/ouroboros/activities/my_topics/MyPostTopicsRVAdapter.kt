package com.example.ouroboros.activities.my_topics

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ouroboros.R
import com.example.ouroboros.intent.TopicSerializable
import com.example.ouroboros.model.TableCodes.ColorRoleCodes.Companion.ROLE_COLORS
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.MY_ID_TOPIC_CODE
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.TOPIC_SERIALIZABLE_CODE
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.APPLICANT
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.HELPER
import com.example.ouroboros.model.TableCodes.RoomCodes.Companion.ROOM_ALPHA
import com.example.ouroboros.model.firebase.topics.Topic
import com.example.ouroboros.utils.Constants.sharedPreferenceKeys.Companion.REQUEST_TOPIC_ACTIVITY_KEY
import com.example.ouroboros.utils.Constants.sharedPreferenceVariables.Companion.MY_ID_TOPIC
import com.example.ouroboros.utils.Constants.sharedPreferenceVariables.Companion.SAVED
import kotlinx.android.synthetic.main.item_topic.view.*

class MyPostTopicsRVAdapter(
    context: Context,
    topicsList: ArrayList<Topic>,
    topic: Topic
) : RecyclerView.Adapter<MyPostTopicsRVAdapter.TopicsViewHolder>() {

    private var topicsList = emptyList<Topic>()
    private var topic : Topic
    private val context : Context

    init {
        this.topicsList = topicsList
        this.topic = topic
        this.context = context
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyPostTopicsRVAdapter.TopicsViewHolder {
        var itemView = LayoutInflater.from(context).inflate(R.layout.item_topic, parent, false) //var
        return TopicsViewHolder(itemView, context)
    }

    override fun getItemCount(): Int {
        return topicsList.size
    }

    override fun onBindViewHolder(
        holder: MyPostTopicsRVAdapter.TopicsViewHolder,
        position: Int
    ) {
        val myTopic : Topic = topicsList[position]
        holder.bindTopic(myTopic, topic)
    }

    class TopicsViewHolder(
        itemView : View,
        context : Context
    ) : RecyclerView.ViewHolder(itemView) {
        private var context : Context = context


        fun bindTopic(myTopic : Topic, topic : Topic) {
            //Añadir apartado para cambiar la imagen del recurso de la publicación.
            itemView.iv_resource.setImageResource(R.drawable.iconapp)
            itemView.tv_title.text = myTopic.title
            when (myTopic.role_type){
                HELPER -> {
                    itemView.iv_select_request_topic.setImageResource(R.mipmap.ic_helper_roletype)
                    itemView.tv_role_type.text = itemView.context.getString(R.string.role_type_helper_small)
                    itemView.tv_role_type.setTextColor(Color.parseColor(ROLE_COLORS[HELPER]))
                }
                APPLICANT -> {
                    itemView.iv_select_request_topic.setImageResource(R.mipmap.ic_applicant_roletype)
                    itemView.tv_role_type.text = itemView.context.getString(R.string.role_type_applicant_small)
                    itemView.tv_role_type.setTextColor(Color.parseColor(ROLE_COLORS[APPLICANT]))
                }
            }
            itemView.setOnClickListener{
                writeMyIdTopicPreferences(topic.idTopic, true)
                startAddCouplingActivity(myTopic.idTopic, topic)
            }
        }

        private fun startAddCouplingActivity(myIdTopic : String, topic : Topic){
            val intent = Intent(context, AddCouplingActivity::class.java)
            intent.putExtra(MY_ID_TOPIC_CODE, myIdTopic)
            val topicSerializable = TopicSerializable(topic)
            intent.putExtra(TOPIC_SERIALIZABLE_CODE, topicSerializable).addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        private fun writeMyIdTopicPreferences(myIdTopic : String, saved : Boolean){
            val sharedPref : SharedPreferences = context.getSharedPreferences(REQUEST_TOPIC_ACTIVITY_KEY, 0)
            val editor : SharedPreferences.Editor = sharedPref.edit()
            Log.d("TAG:MPTRVA:124:mITopic",myIdTopic)
            editor.putString(MY_ID_TOPIC, myIdTopic)
            Log.d("TAG:MPTRVA:293:saved",saved.toString())
            editor.putBoolean(SAVED, saved)
            editor.commit()
        }
    }
}

