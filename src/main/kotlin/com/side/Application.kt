package com.side

import com.google.gson.Gson
import com.oreo.session.DeviceSession
import com.side.api.getUser
import com.side.api.s3
import com.side.api.test
import io.ktor.server.netty.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*
import io.ktor.websocket.*



fun main(args: Array<String>): Unit = EngineMain.main(args)


val gson = Gson()
var applicationLog: Application? = null

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    applicationLog = this@module

    install(Sessions) {
        cookie<DeviceSession>("SESSION")
    }
    /**儲存來自client端的clientId*/
    intercept(ApplicationCallPipeline.Features) {
        if (call.sessions.get<DeviceSession>() == null) {
            val clientId = call.parameters["client_id"] ?: ""
            val deviceName = call.parameters["device_name"] ?: ""
            call.sessions.set(DeviceSession(clientId, generateNonce(), deviceName))
        }
    }


    install(ContentNegotiation) {
        gson {
        }
    }

    install(CallLogging)
    install(WebSockets)

    install(Routing) {
        test()
        getUser()
        s3()
    }


}