package io.github.acidghost.robotics

import lejos.hardware.BrickFinder
import lejos.hardware.ev3.EV3


/**
  * Created by acidghost on 22/12/15.
  */
object LineFollower extends App {

	println("Hello, World!")

	val ev3  = BrickFinder.getLocal.asInstanceOf[EV3]
	val lcd  = ev3.getTextLCD
	val keys = ev3.getKeys
	lcd.drawString("Hello World", 4, 3)
	lcd.drawString("I'm a Scala app", 1, 4)
	keys.waitForAnyPress

}
