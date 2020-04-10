package com.example.ouroboros.model.firebase.users
import com.google.firebase.database.*

class UsersTable {
    fun create(idUser : String,
               email : String,
               username : String,
               ouroboros : Double){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users")
        val idUser : String? = myRef.push().key
        val user = User(
            idUser!!,
            email,
            username,
            ouroboros
        )
        myRef.child(idUser).setValue(user)
    }

    fun update(idUser : String,
               email : String,
               username : String,
               ouroboros : Double){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users")
        val childUpdate = HashMap<String, Any>()
        childUpdate["email"] = email
        childUpdate["username"] = username
        childUpdate["ouroboros"] = ouroboros
        myRef.child(idUser).updateChildren(childUpdate)
    }

    fun delete(idUser : String){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users")
        myRef.child(idUser).removeValue()
    }

}