package com.example.ouroboros.model.firebase.couplings

import com.example.ouroboros.model.TableCodes.TableCodes.Companion.COUPLING_TABLE_CODE
import com.google.firebase.database.*

class CouplingsTable {
    fun create(
        idReceiverTopic : String,
        idSenderTopic : String,
        roleDispatcher : Int,
        ouroboros : Double,
        coupledDate : Long,
        coupledState : Int
    ){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(COUPLING_TABLE_CODE)
        val idCoupling : String? = myRef.push().key
        val coupling = Coupling(
            idCoupling!!,
            idReceiverTopic,
            idSenderTopic,
            roleDispatcher,
            ouroboros,
            coupledDate,
            coupledState
        )
        myRef.child(idCoupling).setValue(coupling)
    }

    fun update(idCoupling : String,
               idReceiverTopic : String,
               idSenderTopic : String,
               roleDispatcher : Int,
               ouroboros : Double,
               coupledDate : Long,
               coupledState : Int){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(COUPLING_TABLE_CODE)
        val childUpdate = HashMap<String, Any>()
        childUpdate["idReceiverTopic"] = idReceiverTopic
        childUpdate["idSenderTopic"] = idSenderTopic
        childUpdate["roleDispatcher"] = roleDispatcher
        childUpdate["ouroboros"] = ouroboros
        childUpdate["coupledDate"] = coupledDate
        childUpdate["coupledState"] = coupledState
        myRef.child(idCoupling).updateChildren(childUpdate)
    }

    fun deleteCoupling(idCoupling: String){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(COUPLING_TABLE_CODE)
        myRef.child(idCoupling).removeValue()
    }

}
