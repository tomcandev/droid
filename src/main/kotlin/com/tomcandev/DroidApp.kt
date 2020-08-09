package com.tomcandev

import com.slack.api.bolt.App
import com.slack.api.bolt.jetty.SlackAppServer

fun main() {
  val app = App()

  // Write some code here

  val server = SlackAppServer(app)
  server.start() // http://localhost:3000/slack/events
}