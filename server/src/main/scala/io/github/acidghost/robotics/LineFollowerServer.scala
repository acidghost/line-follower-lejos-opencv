package io.github.acidghost.robotics

import java.io.PrintStream
import java.net.ServerSocket

import scala.io.BufferedSource


object LineFollowerServer extends App {

    val server = new ServerSocket(1337)

    while (true) {
        val s = server.accept
        val in = new BufferedSource(s.getInputStream).getLines
        val out = new PrintStream(s.getOutputStream)

        out println in.next
        out.flush()
        s.close()
    }

}
