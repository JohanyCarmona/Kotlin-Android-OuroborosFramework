package com.example.ouroboros.ui

import android.content.Context
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ouroboros.R
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.APPLICANT
import com.example.ouroboros.model.TableCodes.RoleTypeCodes.Companion.HELPER
import com.example.ouroboros.model.Topic
import kotlinx.android.synthetic.main.item_topic.view.*

class TopicsRVAdapter(
    private val context: Context,
    private val topicsList: ArrayList<Topic>
) : RecyclerView.Adapter<TopicsRVAdapter.TopicsViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TopicsViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_topic, parent, false)
        return TopicsViewHolder(itemView, context)
    }

    override fun getItemCount(): Int = topicsList.size

    override fun onBindViewHolder(
        holder: TopicsViewHolder,
        position: Int
    ) {
        val topic: Topic = topicsList[position]
        holder.bindTopic(topic)
    }

    class TopicsViewHolder(
        itemView: View,
        context: Context
    ) : RecyclerView.ViewHolder(itemView) {

        fun bindTopic(topic: Topic) {
            //Añadir apartado para cambiar la imagen del recurso de la publicación.
            itemView.tv_title.text = topic.title
            itemView.tv_description.text = topic.description
            when (topic.role_type){
                HELPER ->
                    itemView.tv_role_type.text = "HELPER"
                //Añadir apartado para cambiar el ícono del tipo de rol de la publicación.
                APPLICANT ->
                    itemView.tv_role_type.text = "APPLICANT"
                    //itemView.tv_role_type.text = getString(R.string.role_type_applicant)
                //Añadir apartado para cambiar el ícono del tipo de rol de la publicación.
            }
        }
    }
}