package com.example.ouroboros.model.firebase.interactions

import com.example.ouroboros.model.TableCodes.TableCodes.Companion.INTERACTION_TABLE_CODE
import com.google.firebase.database.*

class InteractionsTable {
    fun create(
        idCoupling: String,
        startDate : Long,
        startLatitudeDispatcher : Double,
        startLongitudeDispatcher : Double,
        LatitudeDispatcher : Double,
        LongitudeDispatcher : Double,
        endDate : Long,
        interactionState : Int
    ){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(INTERACTION_TABLE_CODE)
        val idInteraction : String? = myRef.push().key
        val interaction = Interaction(
            idInteraction!!,
            idCoupling,
            startDate,
            startLatitudeDispatcher,
            startLongitudeDispatcher,
            LatitudeDispatcher,
            LongitudeDispatcher,
            endDate,
            interactionState
        )
        myRef.child(idInteraction).setValue(interaction)
    }

    fun update(idInteraction : String,
               idCoupling: String,
               startDate : Long,
               startLatitudeDispatcher : Double,
               startLongitudeDispatcher : Double,
               LatitudeDispatcher : Double,
               LongitudeDispatcher : Double,
               endDate : Long,
               interactionState : Int
    ){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(INTERACTION_TABLE_CODE)
        val childUpdate = HashMap<String, Any>()
        childUpdate["idCoupling"] = idCoupling
        childUpdate["startDate"] = startDate
        childUpdate["startLatitudeDispatcher"] = startLatitudeDispatcher
        childUpdate["startLongitudeDispatcher"] = startLongitudeDispatcher
        childUpdate["LatitudeDispatcher"] = LatitudeDispatcher
        childUpdate["LongitudeDispatcher"] = LongitudeDispatcher
        childUpdate["endDate"] = endDate
        childUpdate["interactionState"] = interactionState
        myRef.child(idInteraction).updateChildren(childUpdate)
    }

    fun deleteInteraction(idInteraction: String){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(INTERACTION_TABLE_CODE)
        myRef.child(idInteraction).removeValue()
    }

}
