package com.example.ouroboros.model.firebase.couplings

import com.example.ouroboros.model.TableCodes.TableCodes.Companion.COUPLING_TABLE_CODE
import com.google.firebase.database.*

class CouplingsTable {
    fun create(
        idHelperTopic : String,
        idApplicantTopic : String,
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
            idHelperTopic,
            idApplicantTopic,
            roleDispatcher,
            ouroboros,
            coupledDate,
            coupledState
        )
        myRef.child(idCoupling).setValue(coupling)
    }

    fun update(idCoupling : String,
               idHelperTopic : String,
               idApplicantTopic : String,
               roleDispatcher : Int,
               ouroboros : Double,
               coupledDate : Long,
               coupledState : Int){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(COUPLING_TABLE_CODE)
        val childUpdate = HashMap<String, Any>()
        childUpdate["idHelperTopic"] = idHelperTopic
        childUpdate["idApplicantTopic"] = idApplicantTopic
        childUpdate["roleDispatcher"] = roleDispatcher
        childUpdate["ouroboros"] = ouroboros
        childUpdate["coupledDate"] = coupledDate
        childUpdate["coupledState"] = coupledState
        myRef.child(idCoupling).updateChildren(childUpdate)
    }

    fun updateCoupledState(
        idCoupling : String,
        coupledState : Int){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(COUPLING_TABLE_CODE)
        val childUpdate = HashMap<String, Any>()
        childUpdate["coupledState"] = coupledState
        myRef.child(idCoupling).updateChildren(childUpdate)
    }

    fun deleteCoupling(idCoupling: String){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(COUPLING_TABLE_CODE)
        myRef.child(idCoupling).removeValue()
    }

}
