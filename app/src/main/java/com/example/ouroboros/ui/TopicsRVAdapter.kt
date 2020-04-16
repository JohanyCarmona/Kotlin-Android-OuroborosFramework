package com.example.ouroboros.ui

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ouroboros.R
import com.example.ouroboros.activities.my_topics.MyTopicDetailActivity
import com.example.ouroboros.activities.topics.TopicDetailActivity
import com.example.ouroboros.model.TableCodes.ColorRoleCodes.Companion.ROLE_COLORS
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.APPLICANT
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.HELPER
import com.example.ouroboros.model.TableCodes.RoomCodes.Companion.ROOM_ALPHA
import com.example.ouroboros.model.firebase.topics.Topic
import com.example.ouroboros.intent.TopicSerializable
import com.example.ouroboros.model.TableCodes.IntentCodes.Companion.TOPIC_SERIALIZABLE_CODE
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.item_topic.view.*

class TopicsRVAdapter(
    context: Context,
    topicsList: ArrayList<Topic>
) : RecyclerView.Adapter<TopicsRVAdapter.TopicsViewHolder>() {

    private var topicsList = emptyList<Topic>()
    private val context : Context

    init {
        this.topicsList = topicsList
        this.context = context
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TopicsRVAdapter.TopicsViewHolder {
        var itemView = LayoutInflater.from(context).inflate(R.layout.item_topic, parent, false) //var
        return TopicsViewHolder(itemView, context)
    }

    override fun getItemCount(): Int {
        return topicsList.size
    }

    override fun onBindViewHolder(
        holder: TopicsRVAdapter.TopicsViewHolder,
        position: Int
    ) {
        val topic : Topic = topicsList[position]
        holder.bindTopic(topic)
    }

    class TopicsViewHolder(
        itemView : View,
        context : Context
    ) : RecyclerView.ViewHolder(itemView) {
        private var context : Context

        init {
            this.context = context
        }

        fun bindTopic(topic: Topic) {
            //Añadir apartado para cambiar la imagen del recurso de la publicación.
            itemView.iv_resource.setImageResource(R.drawable.iconapp)
            itemView.tv_title.text = topic.title
            when (topic.role_type){
                HELPER -> {
                    //Añadir apartado para cambiar el ícono del tipo de rol de la publicación.
                    itemView.iv_role_type.setImageResource(R.mipmap.ic_helper_roletype)
                    itemView.tv_role_type.text = itemView.context.getString(R.string.role_type_helper_small)
                    itemView.tv_role_type.setTextColor(Color.parseColor(ROLE_COLORS[HELPER]))
                }
                APPLICANT -> {
                    //Añadir apartado para cambiar el ícono del tipo de rol de la publicación.
                    itemView.iv_role_type.setImageResource(R.mipmap.ic_applicant_roletype)
                    itemView.tv_role_type.text = itemView.context.getString(R.string.role_type_applicant_small)
                    itemView.tv_role_type.setTextColor(Color.parseColor(ROLE_COLORS[APPLICANT]))
                }
            }
            if(!topic.enable){
                //itemView.tv_title.setTextColor(Color.parseColor(ROOM_IDENTIFIER_COLOR))
                itemView.iv_resource.alpha = ROOM_ALPHA
                itemView.iv_role_type.alpha = ROOM_ALPHA
                itemView.tv_title.alpha = ROOM_ALPHA
                itemView.tv_role_type.alpha = ROOM_ALPHA
            }else{
                itemView.iv_resource.alpha = 1F
                itemView.iv_role_type.alpha = 1F
                itemView.tv_title.alpha = 1F
                itemView.tv_role_type.alpha = 1F

            }
            itemView.setOnClickListener{
                val user = FirebaseAuth.getInstance().currentUser!!
                if (topic.idUser.equals(user.uid)){
                    val intent = Intent(context, MyTopicDetailActivity::class.java)
                    val topic_serializable =
                        TopicSerializable(topic)
                    intent.putExtra(TOPIC_SERIALIZABLE_CODE, topic_serializable).addFlags(FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)

                }else{
                    val intent = Intent(context, TopicDetailActivity::class.java)
                    val topic_serializable =
                        TopicSerializable(topic)
                    intent.putExtra(TOPIC_SERIALIZABLE_CODE, topic_serializable).addFlags(FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            }
        }
    }
}
