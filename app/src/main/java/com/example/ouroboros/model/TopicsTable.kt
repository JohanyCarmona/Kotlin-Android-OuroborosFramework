package com.example.ouroboros.model

import android.util.Log
import com.google.firebase.database.*

class TopicsTable {
    fun create(idUser : String,
               role_type : Int,
               publication_type : Int,
               title : String,
               resource_category : Int,
               image : String,
               description : String,
               publication_date : String,
               latitude : Double,
               longitude : Double,
               enable : Boolean){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("topics")
        val idTopic : String? = myRef.push().key
        val topic = Topic(idTopic!!,
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
            enable)
        myRef.child(idTopic).setValue(topic)
    }

    fun update(idTopic : String,
               title : String,
               resource_category : Int,
               image : String,
               description : String,
               publication_date : String,
               latitude : String,
               longitude : String){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("topics")
        val childUpdate = HashMap<String, Any>()
        childUpdate["title"] = title
        childUpdate["resource_category"] = resource_category
        childUpdate["image"] = image
        childUpdate["description"] = description
        childUpdate["publication_date"] = publication_date
        childUpdate["latitude"] = latitude
        childUpdate["longitude"] = longitude
        myRef.child(idTopic).updateChildren(childUpdate)
    }

    fun delete(idTopic : String){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("topics")
        myRef.child(idTopic).removeValue()
    }

}