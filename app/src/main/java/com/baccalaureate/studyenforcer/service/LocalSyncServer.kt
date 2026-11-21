package com.baccalaureate.studyenforcer.service

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.baccalaureate.studyenforcer.R
import com.baccalaureate.studyenforcer.data.local.entity.QuestionEntity
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket

class LocalSyncServer(
    private val context: Context,
    private val studyRepository: StudyRepository,
    private val gson: Gson,
    private val port: Int = 8888
) {
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    fun start() {
        if (isRunning) return
        scope.launch {
            try {
                serverSocket = ServerSocket(port)
                isRunning = true

                while (isRunning && !serverSocket!!.isClosed) {
                    try {
                        val client = serverSocket!!.accept()
                        handleClient(client)
                    } catch (e: Exception) {
                        // socket closed or error
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleClient(socket: Socket) {
        scope.launch {
            try {
                socket.use { s ->
                    val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                    val writer = PrintWriter(s.getOutputStream(), true)

                    val requestLine = reader.readLine() ?: return@use
                    val parts = requestLine.split(" ")
                    val method = if (parts.isNotEmpty()) parts[0] else "GET"
                    val path = if (parts.size > 1) parts[1] else "/"

                    var contentLength = 0
                    var line: String? = reader.readLine()
                    while (!line.isNullOrEmpty()) {
                        if (line.startsWith("Content-Length:", ignoreCase = true)) {
                            contentLength = line.substringAfter(":").trim().toIntOrNull() ?: 0
                        }
                        line = reader.readLine()
                    }

                    val body = if (contentLength > 0) {
                        val charArray = CharArray(contentLength)
                        var read = 0
                        while (read < contentLength) {
                            val r = reader.read(charArray, read, contentLength - read)
                            if (r == -1) break
                            read += r
                        }
                        String(charArray, 0, read)
                    } else ""

                    when {
                        method == "GET" && path == "/status" -> {
                            val stats = studyRepository.getUserStatsFlow()
                            val responseJson = """{"status":"listening","port":$port,"active":true}"""
                            sendResponse(writer, 200, "application/json", responseJson)
                        }

                        method == "POST" && (path == "/push_question" || path == "/questions") -> {
                            try {
                                val addedCount = if (body.trim().startsWith("[")) {
                                    val type = object : TypeToken<List<QuestionEntity>>() {}.type
                                    val list: List<QuestionEntity> = gson.fromJson(body, type)
                                    list.forEach { studyRepository.addQuestionDirectly(it) }
                                    list.size
                                } else {
                                    val q: QuestionEntity = gson.fromJson(body, QuestionEntity::class.java)
                                    studyRepository.addQuestionDirectly(q)
                                    1
                                }
                                showHeadsUpNotification("بنك الأسئلة المباشر", "تم استلام $addedCount سؤالاً جديداً بنجاح!")
                                sendResponse(writer, 200, "application/json", """{"success":true,"added":$addedCount}""")
                            } catch (e: Exception) {
                                sendResponse(writer, 400, "application/json", """{"success":false,"error":"${e.message}"}""")
                            }
                        }

                        method == "POST" && path == "/push_notification" -> {
                            try {
                                val map: Map<String, String> = gson.fromJson(body, object : TypeToken<Map<String, String>>() {}.type)
                                val title = map["title"] ?: "تنبيه جديد"
                                val msg = map["message"] ?: "إشعار من الحاسوب"
                                showHeadsUpNotification(title, msg)
                                sendResponse(writer, 200, "application/json", """{"success":true}""")
                            } catch (e: Exception) {
                                sendResponse(writer, 400, "application/json", """{"success":false,"error":"${e.message}"}""")
                            }
                        }

                        method == "POST" && path == "/trigger_quiz" -> {
                            // Remotely triggers quiz
                            StudyEnforcementService.triggerImmediateQuiz(context)
                            sendResponse(writer, 200, "application/json", """{"success":true,"message":"Quiz triggered"}""")
                        }

                        else -> {
                            sendResponse(writer, 200, "text/plain", "Al-Bacc Reverse Listener Active on port $port")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendResponse(writer: PrintWriter, statusCode: Int, contentType: String, body: String) {
        val statusText = if (statusCode == 200) "OK" else "Bad Request"
        writer.println("HTTP/1.1 $statusCode $statusText")
        writer.println("Content-Type: $contentType; charset=UTF-8")
        writer.println("Access-Control-Allow-Origin: *")
        writer.println("Content-Length: ${body.toByteArray(Charsets.UTF_8).size}")
        writer.println()
        writer.print(body)
        writer.flush()
    }

    private fun showHeadsUpNotification(title: String, message: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notif = NotificationCompat.Builder(context, StudyEnforcementService.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify((System.currentTimeMillis() % 10000).toInt() + 2000, notif)
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun getLocalIpAddress(): String {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val networkInterface = interfaces.nextElement()
                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (!address.isLoopbackAddress && address is Inet4Address) {
                            return address.hostAddress ?: "127.0.0.1"
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return "127.0.0.1"
        }
    }
}
