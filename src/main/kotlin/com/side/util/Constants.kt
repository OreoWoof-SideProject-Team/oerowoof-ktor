package com.side.util

import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object Constants {
    var dateFormat: DateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    val fmt: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    const val MEMBER_ALREADY_EXIST = "MEMBER_ALREADY_EXIST"
    const val MEMBER_NOT_EXIST = "MEMBER_NOT_EXIST"
    const val WRONG_PSW = "WRONG_PSW"
    const val UNEXPECTED_ERROR = "UNEXPECTED_ERROR"


    fun generateUserAvatarFile(base64: String, userId: String): Pair<String, File> {
        val strings: List<String> = base64.split(",")

        val extension = when (strings[0]) {
            "data:image/jpeg;base64" -> "jpeg"
            "data:image/png;base64" -> "png"
            else -> "jpg"
        }

        val decoder: Base64.Decoder = Base64.getDecoder()
        val data = decoder.decode(strings[1])

        val file = File(userId + extension)

        try {
            BufferedOutputStream(FileOutputStream(file)).use { outputStream -> outputStream.write(data) }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return extension to file
    }

}