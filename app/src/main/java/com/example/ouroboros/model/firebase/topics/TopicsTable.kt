package com.example.ouroboros.model.firebase.topics

import com.example.ouroboros.model.room.SesionRoom
import com.example.ouroboros.model.room.topics.TopicRoom
import com.example.ouroboros.model.room.topics.TopicRoomDAO
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.TOPIC_TABLE_CODE
import com.google.firebase.database.*

class TopicsTable {
    fun create(idUser : String,
               role_type : Int,
               publication_type : Int,
               title : String,
               resource_category : Int,
               image : String,
               description : String,
               publication_date : Long,
               latitude : Double,
               longitude : Double,
               enable : Boolean){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TOPIC_TABLE_CODE)
        val idTopic : String? = myRef.push().key
        val topic = Topic(
            idTopic!!,
            idUser,
            role_type,
            publication_type,
            title,
            resource_category,
            image,
            description,
            publication_date,
            latitude,
            longitude,
            enable
        )
        myRef.child(idTopic).setValue(topic)
    }

    fun create_(idUser : String,
               role_type : Int,
               publication_type : Int,
               title : String,
               resource_category : Int,
               image : String,
               description : String,
               publication_date : Long,
               latitude : Double,
               longitude : Double,
               enable : Boolean) : String {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TOPIC_TABLE_CODE)
        val idTopic : String? = myRef.push().key
        val topic = Topic(
            idTopic!!,
            idUser,
            role_type,
            publication_type,
            title,
            resource_category,
            image,
            description,
            publication_date,
            latitude,
            longitude,
            enable
        )
        myRef.child(idTopic).setValue(topic)
        return idTopic
    }

    fun update(idTopic : String,
               idUser : String,
               role_type : Int,
               publication_type : Int,
               title : String,
               resource_category : Int,
               image : String,
               description : String,
               publication_date : Long,
               latitude : Double,
               longitude : Double,
               enable : Boolean){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TOPIC_TABLE_CODE)
        val childUpdate = HashMap<String, Any>()
        childUpdate["idUser"] = idUser
        childUpdate["role_type"] = role_type
        childUpdate["publication_type"] = publication_type
        childUpdate["title"] = title
        childUpdate["resource_category"] = resource_category
        childUpdate["image"] = image
        childUpdate["description"] = description
        childUpdate["publication_date"] = publication_date
        childUpdate["latitude"] = latitude
        childUpdate["longitude"] = longitude
        childUpdate["enable"] = enable
        myRef.child(idTopic).updateChildren(childUpdate)
    }

    fun updatePublicationType(
        idTopic : String,
        publication_type : Int){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TOPIC_TABLE_CODE)
        val childUpdate = HashMap<String, Any>()
        childUpdate["publication_type"] = publication_type
        myRef.child(idTopic).updateChildren(childUpdate)
    }

    fun deleteTopic(idTopic : String){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(TOPIC_TABLE_CODE)
        myRef.child(idTopic).removeValue()
    }

    fun deleteRoomTopic(topic : Topic){
        val topicRoomDao: TopicRoomDAO = SesionRoom.database.TopicRoomDAO()
        Thread {
            topicRoomDao.deleteTopic(
                TopicRoom(
                    idTopic = topic.idTopic.toInt(),
                    idUser = topic.idUser,
                    role_type = topic.role_type,
                    publication_type = topic.publication_type,
                    title = topic.title,
                    resource_category = topic.resource_category,
                    image = topic.image,
                    description = topic.description,
                    publication_date = topic.publication_date,
                    latitude = topic.latitude,
                    longitude = topic.longitude,
                    enable = topic.enable
                )
            )
        }.start()
    }

}
