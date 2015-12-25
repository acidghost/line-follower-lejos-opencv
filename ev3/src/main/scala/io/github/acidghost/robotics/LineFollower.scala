package io.github.acidghost.robotics

import lejos.hardware.ev3.EV3
import lejos.hardware.{BrickFinder, Button}
import org.opencv.core.Core


object LineFollower extends App {

	System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

	lazy val ev3  = BrickFinder.getLocal.asInstanceOf[EV3]

	val eye = new CamEye
	println("Eye started")

	 val pilot = new PIDPilot
	 pilot.drive()
	 println("Pilot started")

	while (Button.ESCAPE.isUp) {
		val timer = System.currentTimeMillis()
		eye.getCenters match {
			case Some(centers) =>
				val error = eye.getError(centers)
				println(s"Found error: $error")
				pilot.setError(error)
				pilot.doPID()
				println(s"Time: ${System.currentTimeMillis() - timer}")
			case None =>
				println("Camera not ready...")
		}
	}

	pilot.land()
	eye.close()

	def doHello() = {
		val lcd  = ev3.getTextLCD
		lcd.drawString("Hello World", 4, 3)
		lcd.drawString("I'm a Scala app", 1, 4)
		Button.waitForAnyPress()
	}

}
