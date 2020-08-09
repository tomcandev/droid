package com.tomcandev.droid

import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse
import com.slack.api.bolt.App
import com.slack.api.bolt.WebEndpoint
import com.slack.api.bolt.handler.WebEndpointHandler
import com.slack.api.bolt.jetty.SlackAppServer
import com.slack.api.bolt.response.Response
import com.slack.api.methods.MethodsClient
import com.slack.api.methods.request.conversations.ConversationsListRequest
import com.slack.api.model.ConversationType
import com.slack.api.model.event.AppMentionEvent
import java.util.*
import java.util.logging.Logger

fun main() {
    val app = App()
    val menu = "```" +

            "beer\n" +
            "takeleave 2020/09/09-2020/09/11 // From 2020/09/09 00:00:01 to 2020/09/11 23:59:59\n" +
            "random\n" +
            "members" +

            "```"

    // Hello World on Web
    app.endpoint(WebEndpoint.Method.GET, "/", WebEndpointHandler(function = { request, context ->
        return@WebEndpointHandler Response.json(
                200,
                SlashCommandResponse.builder().text("Hi! My name is Droid").build()
        )
    }))

    // When droid mentioned
    app.event(AppMentionEvent::class.java) { event, ctx ->
        val text = event.event.text
        Logger.getGlobal().info(text)
        val commands = text.split(" ")
        println(commands)
        if (commands.size <= 1) {
            ctx.say(menu)
        } else {
            when (commands[1]) {
                "beer" -> ctx.say(":beer:")
                else -> ctx.say(menu)
            }
        }
        ctx.ack()
    }

    // Config for command
//    app.command("/droid") { req, ctx ->
//        val commands = req.payload.text.split(" ")
//        if (commands.isEmpty()) {
//            ctx.say(menu)
//        } else {
//            when (commands[0]) {
//                "beer" -> ctx.say(":beer:")
//                else -> ctx.say(menu)
//            }
//        }
//
//        ctx.ack()
//    }

    // Config for standup
//    val myTask = MyTask(app)
//    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Singapore"))
//    calendar[Calendar.HOUR_OF_DAY] = 5
//    calendar[Calendar.MINUTE] = 30
//    calendar[Calendar.SECOND] = 0
//    calendar[Calendar.MILLISECOND] = 0
//    val dateSchedule = calendar.time
//    val period = 24 * 60 * 60 * 1000.toLong()
//    val timer = Timer()
//    timer.schedule(myTask, dateSchedule, period)

    // Start server
    val server = SlackAppServer(app, System.getenv("PORT")?.toInt() ?: 3000)
    server.start()
}

class MyTask(val app: App) : TimerTask() {
    val token = app.config().singleTeamBotToken

    override fun run() {
        app.config().slack.methods().chatPostMessage { req ->
            req
                    .token(token)
                    .channel(toConversationId(app.config().slack.methods(), "#general")) // Channel ID
                    .text(":wave: Stand up. Please!")
        }
    }

    // requires channels:read scope
    private fun toConversationId(client: MethodsClient, where: String): String {
        val name = where.replaceFirst("#", "").trim()
        val cursor: String? = null
        var conversationId: String? = null
        while (conversationId == null && cursor != "") {
            val rb =
                    ConversationsListRequest.builder().token(token).limit(1000).types(listOf(ConversationType.PUBLIC_CHANNEL))
            val req = if (cursor != null) rb.cursor(cursor).build() else rb.build()
            val list = client.conversationsList(req)
            for (c in list.channels) {
                if (c.name == name) {
                    conversationId = c.id
                    break
                }
            }
        }
        return conversationId ?: where
    }
}