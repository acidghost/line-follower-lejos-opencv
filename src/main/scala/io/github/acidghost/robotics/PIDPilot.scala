package io.github.acidghost.robotics

import lejos.hardware.motor.EV3LargeRegulatedMotor
import lejos.hardware.port.MotorPort


class PIDPilot(Kp: Double = 1, Ki: Double = .1, Kd: Double = 0) extends Thread {

	private val leftMotor = new EV3LargeRegulatedMotor(MotorPort.B)
	private val rightMotor = new EV3LargeRegulatedMotor(MotorPort.C)

	private var error = 0.0
	private var errorTime = System.currentTimeMillis()
	private val baseSpeed = 80
	private var lastError = 0.0
	private var lastErrorTime = System.currentTimeMillis()
	private var integral = 0.0
	private var derivative = 0.0
	private var errorUpdated = false

	def drive() = start()

	def land() = {
		leftMotor.stop()
		rightMotor.stop()
		interrupt()
	}

	def setError(error: Double) = {
		this.error = (baseSpeed / 2) * error
		lastErrorTime = errorTime
		errorTime = System.currentTimeMillis()
		errorUpdated = true
	}

	override def run(): Unit = {
		while (!isInterrupted) {
			if (errorUpdated) {
				val iterationTime = errorTime - lastErrorTime

				integral = (error / iterationTime) + integral
				derivative = (error - lastError) / iterationTime

				val correction = Kp * error + Ki * integral + Kd * derivative

				val leftTurn = baseSpeed - correction
				val rightTurn = baseSpeed + correction
				println(s"PID: $leftTurn - $rightTurn")

				leftMotor.setSpeed(leftTurn.toInt)
				rightMotor.setSpeed(rightTurn.toInt)
				leftMotor.forward()
				rightMotor.forward()

				lastError = error
				errorUpdated = false
			}
		}
	}

}
