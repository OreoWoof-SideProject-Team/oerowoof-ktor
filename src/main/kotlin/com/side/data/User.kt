package com.side.data

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table


object UserEntity : Table(name = "user") {
    val id: Column<Int> = integer("id")
    val name: Column<String> = varchar("name", 45)
    val age: Column<String> = varchar("age", 45)
}

data class User(
    val id: Int,
    val name: String,
    val age: String
) {

    companion object {
        fun fromRow(resultRow: ResultRow) = User(
            id = resultRow[UserEntity.id],
            name = resultRow[UserEntity.name],
            age = resultRow[UserEntity.age]
        )
    }
}
