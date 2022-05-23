package com.side.api


import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.toByteArray
import com.google.gson.annotations.SerializedName
import com.side.data.DataResponse
import com.side.data.User
import com.side.data.UserEntity
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction


class RequestError(errorMsg: String) : Exception(errorMsg)
class DataBaseError(errorMsg: String) : Exception(errorMsg)
data class CodeMessage(
    @field:SerializedName("Code") var code: String,
    @field:SerializedName("Message") var message: String,
)

data class Status<T>(
    var status: Int = 0, var data: T? = null, var error: CodeMessage = CodeMessage(code = "", message = "")
) : java.io.Serializable

fun Route.test() {
    route("/") {
        get {
            val resultStatus = Status<String>()
            resultStatus.status = 1
            resultStatus.data = "OK"
            respondWithResult(call, resultStatus)
        }
    }
}

fun Route.s3() {
    route("/s3") {
        get {
            var msg = ""
            val s3Client = S3Client.fromEnvironment()

            val request = GetObjectRequest {
                key = "test.png"
                bucket = "oreowoofbucket"
            }

            s3Client.use { s3 ->
                s3.getObject(request) { resp ->
                    val byteImg = resp.body?.toByteArray()
                    msg = if (byteImg != null) {
                        "Successfully read test from oreowoof test"
                    } else {
                        "fail read test from oreowoof"
                    }
                }
            }


            call.respond(HttpStatusCode.OK, msg.ifBlank { "UnExcepted error" })
        }
    }
}

fun Route.getUser() {
    route("/getUser") {
        get {
            val query = call.parameters["query"]
            val offset = call.parameters["offset"]
            val limit = call.parameters["limit"]
            val currentTime = call.parameters["currentTime"]

            if (offset == null || limit == null) {
                call.respond(HttpStatusCode.NotAcceptable, "need params to select")
                return@get
            }

            println("offset: $offset")

            val allUser = transaction {
                if (query?.isNotBlank() == true) {
                    UserEntity.select { (UserEntity.name like query) }.limit(limit.toInt(), offset = offset.toLong())
                        .map { User.fromRow(it) }
                } else {
                    UserEntity.selectAll().limit(limit.toInt(), offset = offset.toLong())
                        .orderBy(UserEntity.id to SortOrder.DESC).map { User.fromRow(it) }
                }
            }

            println(allUser.toString())

            call.respond(HttpStatusCode.OK, DataResponse(users = allUser))
        }
    }
}

suspend inline fun <reified T> respondWithResult(call: ApplicationCall, status: Status<T>) {
    call.respond(HttpStatusCode.OK, status)
}




