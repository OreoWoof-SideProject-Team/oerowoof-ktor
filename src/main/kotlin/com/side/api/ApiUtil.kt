package com.side.api

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*

class RequestError(errorMsg: String) : Exception(errorMsg)
class DataBaseError(errorMsg: String) : Exception(errorMsg)
data class ErrorMessage(
   var code: String = "",
   var describe: String = "",
   var message: String = "",
)

data class Status<T>(
    var status: Int = 0, var data: T? = null, var error: ErrorMessage = ErrorMessage()
) : java.io.Serializable

enum class ErrorCode(var code: String) {
    UNEXPECTED_ERROR("500"),
    MEMBER_ALREADY_EXIST("201"),
    MEMBER_NOT_EXIST("202"),
    WRONG_PSW("203"),
}

suspend inline fun <reified T> respondWithResult(call: ApplicationCall, status: Status<T>) {
    call.respond(HttpStatusCode.OK, status)
}

fun unexpectedErrorResult(errorMsg:String):ErrorMessage {
    return ErrorMessage(code = ErrorCode.UNEXPECTED_ERROR.code, describe = ErrorCode.UNEXPECTED_ERROR.name, errorMsg)
}