package com.side.dbOperate

import com.side.data.User
import com.side.data.UserEntity
import com.side.util.Constants.fmt
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object UserTable {
    fun insertUser(user: User): String = transaction {

        val ifExist = UserEntity.select {
            if (user.token.isNotBlank()) (UserEntity.token eq user.token) else (UserEntity.email eq user.email)
        }.map {
            User.fromRow(it)
        }.firstOrNull()


        if(ifExist != null) {
            return@transaction "MEMBER_ALREADY_EXIST"
        }



        val maxId: String? = UserEntity.slice(UserEntity.userId).selectAll().groupBy(UserEntity.userId).having {
            UserEntity.userId eq wrapAsExpression(UserEntity.slice(UserEntity.userId.max()).selectAll())
        }.map { it[UserEntity.userId] }.firstOrNull()

        user.userId =
            maxId?.let {
                var num = it.substring(2, it.length).toInt()

                num += 1

                val title = if (num < 10) {
                    "ow00"
                } else if (num > 99) {
                    "ow"
                } else {
                    "ow0"
                }
                title + num
            } ?: "ow001"

        val userId = UserEntity.insert {
            it[userId] = user.userId
            it[userName] = user.userName
            it[avatar] = if (user.token.isNotBlank() && user.avatar.isNotBlank()) user.avatar else ""
            it[email] = user.email
            it[password] = user.password
            it[phone] = user.phone
            it[token] = user.token
            it[address] = user.address
            it[createTime] = DateTime.parse(user.createTime.replace("/", "-"), fmt)
        } get UserEntity.userId

        return@transaction userId
    }


    fun updateAvatar(user: User):Int = transaction {
       val count =  UserEntity.update({ UserEntity.userId eq user.userId }) {
            it[userName] = user.userName
            it[avatar] = user.avatar
            it[email] = user.email
            it[password] = user.password
            it[phone] = user.phone
            it[token] = user.token
            it[address] = user.address
        }
        return@transaction count
    }

    fun loginBySpecific(verifyKey: String, isSocialLogin: Boolean): User? = transaction {
        val user = UserEntity.select {
            if (isSocialLogin) (UserEntity.token eq verifyKey) else (UserEntity.email eq verifyKey)
        }.map {
            User.fromRow(it)
        }.firstOrNull()

        return@transaction user
    }


}