package com.example.ouroboros.intent

import com.example.ouroboros.model.firebase.topics.Topic
import java.io.Serializable

class TopicSerializable(topic : Topic) : Serializable {
    val idTopic = topic.idTopic
    val idUser : String = topic.idUser
    val role_type : Int = topic.role_type
    val publication_type : Int = topic.publication_type
    val title : String = topic.title
    val resource_category : Int = topic.resource_category
    val image : String = topic.image
    val description : String = topic.description
    val publication_date : Long = topic.publication_date
    val latitude : Double = topic.latitude
    val longitude : Double = topic.longitude
    val enable : Boolean = topic.enable
}