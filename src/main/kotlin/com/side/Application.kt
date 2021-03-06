package com.side

import com.google.gson.Gson
import com.side.api.*
import com.side.session.DeviceSession
import com.side.data.User
import io.ktor.server.netty.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.*
import io.ktor.websocket.*
import org.jetbrains.exposed.sql.Database


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

    Database.connect(
        "jdbc:mysql://aa1ok2whmxr82kn.ccipddwl43fw.ap-southeast-1.rds.amazonaws.com:3306/oreoWoof",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "admin",
        password = "52511101"
    )


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
        register()
        editProfile()
        login()
    }


}