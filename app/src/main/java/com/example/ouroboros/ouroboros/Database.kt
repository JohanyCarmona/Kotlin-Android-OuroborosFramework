package com.example.ouroboros.ouroboros

import com.example.ouroboros.ouroboros.DataBase.CodesDataBase.SessionCodes.Companion.DONT_EXIST_USER_CODE
import com.example.ouroboros.ouroboros.DataBase.CodesDataBase.SessionCodes.Companion.INVALID_PASSWORD_CODE
import com.example.ouroboros.ouroboros.DataBase.CodesDataBase.SessionCodes.Companion.INVALID_USER_CODE
import com.example.ouroboros.ouroboros.DataBase.DataBase.Companion.CITIES
import com.example.ouroboros.ouroboros.DataBase.DataBase.Companion.NAMES
import com.example.ouroboros.ouroboros.DataBase.DataBase.Companion.PASSWORDS
import com.example.ouroboros.ouroboros.DataBase.DataBase.Companion.USERS
import com.example.ouroboros.ouroboros.DataBase.CodesDataBase.ExpressionConstants.Companion.EMPTY as EMPTY

//(!) This class simulates the reading data from a database.
class DataBase {

    class CodesDataBase {

        class ExpressionConstants{
            companion object {
                const val EMPTY = ""
            }
        }
        class SessionCodes {
            companion object {
                //The values always must be lower than < 0
                const val DONT_EXIST_USER_CODE = -1
                const val INVALID_USER_CODE = -11
                const val INVALID_PASSWORD_CODE = -111
            }
        }
    }
    class DataBase {
        companion object {
            //(!)This values must be encrypted and sorted.
            val USERS: List<String> = listOf(
                "Ouroboros",
                "admin"
            )

            val PASSWORDS: List<String> = listOf(
                "info",
                "admin"
            )//(!)

            val NAMES: List<String> = listOf(
                "Ouroboros",
                "Admin"
            )
            val CITIES: List<String> = listOf(
                "Medellín",
                "Medellín"
            )
        }
    }

    class Session{
        val ID : Int
        val user : String
        val name : String
        val city : String
        constructor(){
            this.ID = DONT_EXIST_USER_CODE
            this.user = EMPTY
            this.name = EMPTY
            this.city = EMPTY
        }
        constructor(ID : Int){
            if (ID < 0){
                this.ID = ID
            }else{
                this.ID = DONT_EXIST_USER_CODE
            }
            this.user = EMPTY
            this.name = EMPTY
            this.city = EMPTY
        }
        constructor(ID : Int, user: String, name : String, city : String) {
            this.ID = ID
            this.user = user
            this.name = name
            this.city = city
        }
    }

    class SessionsManager(users : List<String> = USERS,
                          passwords : List<String> = PASSWORDS,
                          names : List<String> = NAMES,
                          cities : List<String> = CITIES){
        var users : List<String>
        var passwords : List<String>
        var names : List<String>
        var cities : List<String>
        var session : Session

        init {
            this.users = users
            this.passwords = passwords
            this.names = names
            this.cities = cities
            this.session = Session()
        }

        fun write (user : String,
                   password : String, name: String, city: String) {
            this.users = this.users + user
            this.passwords = this.passwords + password
            this.names = this.names + name
            this.cities = this.cities + city
            this.session = Session(this.users.size,user, name, city)
        }

        fun search(user : String): Int {
            val index : Int = this.users.indexOf(user)
            if(index >= 0){
                return index
            } else {
                return DONT_EXIST_USER_CODE
            }
        }

        fun check(user : String,
                  password: String) : Int {
            val user_check: Int = this.search(user)

            if (user_check != DONT_EXIST_USER_CODE) {
                val password_check: Boolean = this.passwords[user_check].equals(password)
                if (password_check) {
                    return user_check
                } else {
                    return INVALID_PASSWORD_CODE
                }
            } else {
                return INVALID_USER_CODE
            }
        }

        fun session(user : String,
                    password: String) {
            val ID : Int = this.check(user, password)
            if (ID != INVALID_USER_CODE && ID != INVALID_PASSWORD_CODE){
                this.session = Session(ID,this.users[ID],this.names[ID],this.cities[ID])
            }else{
                this.session = Session(ID)
            }
        }

        fun session(){
            this.session = Session()
        }
    }
}
//(!)






