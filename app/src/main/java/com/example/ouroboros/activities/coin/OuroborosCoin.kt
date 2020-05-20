package com.example.ouroboros.activities.coin
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.ouroboros.activities.coin.OuroborosCoin.CashCodeStrings.Companion.CASH_CODE_STRING
import com.example.ouroboros.activities.coin.OuroborosCoin.ConstantsStrings.Companion.INVALID_COIN_CODE_OUROBOROS
import com.example.ouroboros.activities.coin.OuroborosCoin.ConstantsValues.Companion.MAX_COIN_DEBT_PER_USER
import com.example.ouroboros.activities.coin.OuroborosCoin.InvalidCoinStrings.Companion.INVALID_COIN_CODE_STRING
import com.example.ouroboros.activities.coin.OuroborosCoin.InvalidOwnerCoinStrings.Companion.INVALID_OWNER_COIN_CODE_STRING
import com.example.ouroboros.model.TableCodes.TableCodes.Companion.USER_TABLE_CODE
import com.example.ouroboros.model.firebase.users.User
import com.example.ouroboros.model.firebase.users.UsersTable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OuroborosCoin {
    private var myCoin : Double? = null
    private var ownerCoin : Double? = null

    init {
        updateCoin()
    }

    fun updateCoin(){
        val myIdUser : String = FirebaseAuth.getInstance().currentUser!!.uid
        val myRef : DatabaseReference = FirebaseDatabase.getInstance().getReference(USER_TABLE_CODE)
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)!!
                    if (user.idUser == myIdUser) {
                        myCoin = user.ouroboros
                        /*if(myCoin == null) {
                            Log.d("TAG:OC:294:myCoin:",myCoin.toString())
                            myCoin = user.ouroboros
                        }else {
                            if (myCoin != user.ouroboros) {
                                Log.d("TAG:OC:234:myCoin:",myCoin.toString())
                                myCoin = user.ouroboros
                            }
                        }*/
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("OC:236:error", "Failed to read value: ", error.toException())
            }
        })
    }

    fun updateCoin(idUser : String) {
        val myRef : DatabaseReference = FirebaseDatabase.getInstance().getReference(USER_TABLE_CODE)
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)!!
                    if (user.idUser == idUser) {
                        ownerCoin = user.ouroboros
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("OC:236:error", "Failed to read value: ", error.toException())
            }
        })
    }

    private class ConstantsValues{
        companion object {
            const val MIN_COIN_TRANSACTION : Double = 1.00
            const val MAX_COIN_DEBT_PER_USER : Double = 3.00
        }
    }

    private class ConstantsStrings{
        companion object{
            const val INVALID_COIN_CODE_OUROBOROS = " ouroboros."
        }
    }

    private class InvalidCoinStrings{
        companion object{
            val INVALID_COIN_CODE_STRING : List<String>  = listOf(
                "Ouroboros cannot be null",
                "Ouroboros must be equal or have more than 1.00",
                "Your debt must don't lower than -",
                "You only can offer "
            )
        }
    }

    private class InvalidOwnerCoinStrings{
        companion object{
            val INVALID_OWNER_COIN_CODE_STRING : List<String>  = listOf(
                "Ouroboros cannot be null",
                "Ouroboros must be equal or have more than 1.00",
                "The owner debt must don't lower than -",
                "The owner only can offer "
            )
        }
    }

    private class CashCodeStrings{
        companion object{
            val CASH_CODE_STRING : List<String>  = listOf(
                "Ouroboros cashed successfully",
                "Ouroboros cannot cashed successfully"
            )
        }
    }

    fun checkCoin(coin : Double): Boolean {
        updateCoin()
        return if (myCoin != null){
            if (coin >= 1.00) coin <= myCoin!! + MAX_COIN_DEBT_PER_USER else false
        }else false
    }

    fun checkCoin(coin : Double, context: Context): Boolean {
        updateCoin()
        return if (myCoin != null){
            if (coin >= 1.00){
                if (coin <= myCoin!! + MAX_COIN_DEBT_PER_USER) {
                    true
                } else {
                    if (myCoin!! + MAX_COIN_DEBT_PER_USER < 1) {
                        val errorMessage : String =  INVALID_COIN_CODE_STRING[2] + MAX_COIN_DEBT_PER_USER.toString() + INVALID_COIN_CODE_OUROBOROS
                        Toast.makeText( context, errorMessage, Toast.LENGTH_SHORT).show()
                        false
                    } else {
                        val coinMax : Double = myCoin!! + MAX_COIN_DEBT_PER_USER
                        val errorMessage : String = INVALID_COIN_CODE_STRING[3] + coinMax.toString() + INVALID_COIN_CODE_OUROBOROS
                        Toast.makeText( context, errorMessage, Toast.LENGTH_SHORT).show()
                        false
                    }
                }
            }else {
                Toast.makeText( context, INVALID_COIN_CODE_STRING[1], Toast.LENGTH_SHORT).show()
                false
            }
        }else {
            Toast.makeText( context, INVALID_COIN_CODE_STRING[0], Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun checkCoin(coin : Double, idUser : String, context: Context): Boolean {
        updateCoin(idUser)
        updateCoin()
        return if (ownerCoin != null){
            if (coin >= 1.00){
                if (coin <= ownerCoin!! + MAX_COIN_DEBT_PER_USER) {
                    true
                } else {
                    if (ownerCoin!! + MAX_COIN_DEBT_PER_USER < 1) {
                        val errorMessage : String =  INVALID_OWNER_COIN_CODE_STRING[2] + MAX_COIN_DEBT_PER_USER.toString() + INVALID_COIN_CODE_OUROBOROS
                        Toast.makeText( context, errorMessage, Toast.LENGTH_SHORT).show()
                        false
                    } else {
                        val coinMax : Double = ownerCoin!! + MAX_COIN_DEBT_PER_USER
                        val errorMessage : String = INVALID_OWNER_COIN_CODE_STRING[3] + coinMax.toString() + INVALID_COIN_CODE_OUROBOROS
                        Toast.makeText( context, errorMessage, Toast.LENGTH_SHORT).show()
                        false
                    }
                }
            }else {
                Toast.makeText( context, INVALID_OWNER_COIN_CODE_STRING[1], Toast.LENGTH_SHORT).show()
                false
            }
        }else {
            Toast.makeText( context, INVALID_OWNER_COIN_CODE_STRING[0], Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun cashOuroboros(idUser : String, coin : Double, context: Context) {
        val myIdUser : String = FirebaseAuth.getInstance().currentUser!!.uid
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(USER_TABLE_CODE)

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var userOuroboros : Double? = null
                var found : Boolean = false
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)!!
                    val usersTable : UsersTable = UsersTable()
                    if (user.idUser == idUser){
                        userOuroboros = user.ouroboros
                        found = true
                        break
                    }
                }
                if (found){
                    val usersTable : UsersTable = UsersTable()
                    usersTable.updateOuroboros(myIdUser, myCoin!! - coin)
                    usersTable.updateOuroboros(idUser, userOuroboros!! + coin)
                    Toast.makeText( context, CASH_CODE_STRING[0], Toast.LENGTH_SHORT).show()
                }else {
                    Toast.makeText( context, CASH_CODE_STRING[1], Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG:OC:324", "Failed to read value.", error.toException())
            }
        })
    }

}