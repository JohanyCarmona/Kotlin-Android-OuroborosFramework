package com.example.ouroboros.intent
import com.example.ouroboros.model.firebase.couplings.Coupling
import java.io.Serializable

class CouplingSerializable(coupling : Coupling) : Serializable {
    val idCoupling = coupling.idCoupling
    val idHelperTopic : String = coupling.idHelperTopic
    val idApplicantTopic : String = coupling.idApplicantTopic
    val roleDispatcher : Int = coupling.roleDispatcher
    val ouroboros : Double = coupling.ouroboros
    val coupledDate : Long = coupling.coupledDate
    val coupledState : Int = coupling.coupledState
}