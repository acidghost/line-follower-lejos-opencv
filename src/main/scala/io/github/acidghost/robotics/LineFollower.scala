package io.github.acidghost.robotics

import lejos.hardware.ev3.EV3
import lejos.hardware.{BrickFinder, Button}
import org.opencv.core.Core


object LineFollower extends App {

	System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

	val ev3  = BrickFinder.getLocal.asInstanceOf[EV3]
	val eye = new CamEye
	val pilot = new PIDPilot

	pilot.drive()

	while (Button.ESCAPE.isUp) {
		eye.getCenters match {
			case Some(centers) =>
				val error = eye.getError(centers)
				println(s"Found error: $error")
				pilot.setError(error)
			case None =>
				println("Camera not ready...")
		}
		println("\n\n")
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
