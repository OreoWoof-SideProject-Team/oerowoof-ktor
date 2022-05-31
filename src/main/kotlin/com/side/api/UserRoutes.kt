package com.side.api


import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import com.google.gson.annotations.SerializedName
import com.side.data.LoginInfo
import com.side.data.User
import com.side.dbOperate.UserTable
import com.side.dbOperate.UserTable.updateAvatar
import com.side.util.Constants
import com.side.util.Constants.MEMBER_ALREADY_EXIST
import com.side.util.Constants.MEMBER_NOT_EXIST
import com.side.util.Constants.UNEXPECTED_ERROR
import com.side.util.Constants.WRONG_PSW
import com.side.util.Constants.generateUserAvatarFile
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Paths
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


fun Route.test() {
    route("/") {
        get {
            val resultStatus = Status<String>()
            resultStatus.status = 1
            resultStatus.data = "ok"
            respondWithResult(call, resultStatus)
        }
    }
}

fun Route.register() {
    route("/register") {
        post {
            val resultStatus = Status<String>()
            try {
                val clientUserInfo = call.receive<User>()
                val userId = UserTable.insertUser(clientUserInfo)

                if (userId == MEMBER_ALREADY_EXIST) {
                    resultStatus.error.code = ErrorCode.MEMBER_ALREADY_EXIST.code
                    resultStatus.error.describe = ErrorCode.MEMBER_ALREADY_EXIST.name
                    resultStatus.error.message = "e-mail has been registered"
                } else {
                    resultStatus.status = 1
                    resultStatus.data = userId
                }
            } catch (e: Exception) {
                resultStatus.data = ""
                resultStatus.error = unexpectedErrorResult(e.localizedMessage)
            } finally {
                call.respond(HttpStatusCode.OK, resultStatus)
            }
        }
    }
}

fun Route.login() {
    route("/login") {
        post {
            val resultStatus = Status<String>()
            val clientUserInfo = call.receive<LoginInfo>()
            val isSocialLogin = clientUserInfo.isSocialLogin
            val verifyKey = clientUserInfo.verifyKey
            val verifyPsw = clientUserInfo.verifyPsw

            try {
                val user = UserTable.loginBySpecific(verifyKey = verifyKey, isSocialLogin = isSocialLogin)

                if (user == null) {
                    resultStatus.error.code = ErrorCode.MEMBER_NOT_EXIST.code
                    resultStatus.error.describe = ErrorCode.MEMBER_NOT_EXIST.name
                    resultStatus.error.message = "Please check your verify info."
                } else {
                    if (!isSocialLogin) {
                        if (user.password != verifyPsw) {
                            resultStatus.error.code = ErrorCode.WRONG_PSW.code
                            resultStatus.error.describe = ErrorCode.WRONG_PSW.name
                            resultStatus.error.message = "Please check your password."
                        } else {
                            resultStatus.status = 1
                            resultStatus.data = ""
                        }
                    }
                }
            } catch (e: Exception) {
                resultStatus.data = ""
                resultStatus.error = unexpectedErrorResult(e.localizedMessage)
            } finally {
                call.respond(HttpStatusCode.OK, resultStatus)
            }
        }
    }
}

fun Route.editProfile() {
    route("/editProfile") {
        put {
            val resultStatus = Status<String>()
            try {
                val clientUserInfo = call.receive<User>()
                var base64Str = ""

                if (clientUserInfo.avatar.isNotBlank() && !clientUserInfo.avatar.startsWith(
                        "http",
                        ignoreCase = true
                    )
                ) {
                    base64Str = clientUserInfo.avatar
                    val (fileType, file) = generateUserAvatarFile(base64 = base64Str, userId = clientUserInfo.userId)
                    val metadataVal = mutableMapOf<String, String>()
                    metadataVal["myVal"] = "avatar"

                    val photoKey = "${clientUserInfo.userId}/avatar.$fileType"

                    val request = PutObjectRequest {
                        bucket = "oreowoofbucket"
                        key = photoKey
                        metadata = metadataVal
                        body = Paths.get(file.toURI()).asByteStream()
                    }

                    S3Client.fromEnvironment().use { s3 ->
                        val response = s3.putObject(request)
                        println("Tag information is ${response.eTag}")
                    }

                    val url = "https://oreowoofbucket.s3.ap-southeast-1.amazonaws.com/$photoKey"
                    clientUserInfo.avatar = url
                }

                resultStatus.status = updateAvatar(clientUserInfo)
                resultStatus.data = if (base64Str.isNotBlank()) clientUserInfo.avatar else ""

            } catch (e: Exception) {
                resultStatus.data = ""
                resultStatus.error = unexpectedErrorResult(e.localizedMessage)
            } finally {
                call.respond(HttpStatusCode.OK, resultStatus)
            }
        }
    }
}




