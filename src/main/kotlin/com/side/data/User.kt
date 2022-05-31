package com.side.data

import com.side.util.Constants.dateFormat
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime

object UserEntity : Table(name = "User") {
    val userId: Column<String> = varchar("userId", 45)
    val userName: Column<String> = varchar("userName", 45)
    val avatar: Column<String> = varchar("avatar", Int.MAX_VALUE)
    val email: Column<String> = varchar("email", 100)
    val password: Column<String> = varchar("password", 50)
    val phone: Column<String> = varchar("phone", 10)
    val token: Column<String> = varchar("token", 200)
    val address: Column<String> = varchar("address", 100)
    val flag: Column<Int> = integer("flag")
    val createTime: Column<DateTime> = datetime("createTime")
}

data class User(
    var userId: String,
    val userName: String,
    var avatar: String,
    val email: String,
    val password: String,
    val phone: String,
    val token: String,
    val address: String,
    val flag: Int,
    val createTime: String
) {
    companion object {
        fun fromRow(resultRow: ResultRow) = User(
            userId = resultRow[UserEntity.userId],
            userName = resultRow[UserEntity.userName],
            avatar = resultRow[UserEntity.avatar],
            email = resultRow[UserEntity.email],
            password = resultRow[UserEntity.password],
            phone = resultRow[UserEntity.phone],
            token = resultRow[UserEntity.token],
            address = resultRow[UserEntity.address],
            flag = resultRow[UserEntity.flag],
            createTime = resultRow[UserEntity.createTime].let { dateFormat.format(it.toDateTime().toDate()) }
                ?: "",
        )
    }
}
