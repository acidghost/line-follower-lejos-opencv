package io.github.acidghost.robotics

import lejos.hardware.motor.EV3LargeRegulatedMotor
import lejos.hardware.port.MotorPort


class PIDPilot(Kp: Double = 50, Ki: Double = 3, Kd: Double = 0) {

	private val leftMotor = new EV3LargeRegulatedMotor(MotorPort.B)
	private val rightMotor = new EV3LargeRegulatedMotor(MotorPort.C)

	private var error = 0.0
	private var errorTime = System.currentTimeMillis()
	private val baseSpeed = 80
	private var lastError = 0.0
	private var lastErrorTime = System.currentTimeMillis()
	private var integral = 0.0
	private var derivative = 0.0

	def drive() = {
		leftMotor.forward()
		rightMotor.forward()
		leftMotor.setSpeed(1)
		rightMotor.setSpeed(1)
	}

	def land() = {
		leftMotor.stop()
		rightMotor.stop()
	}

	def setError(error: Double) = {
		this.error = error
		lastErrorTime = errorTime
		errorTime = System.currentTimeMillis()
	}

	def doPID() = {
		val iterationTime = errorTime - lastErrorTime

		integral = (error / iterationTime) + integral
		derivative = (error - lastError) / iterationTime

		val correction = Kp * error + Ki * integral + Kd * derivative

		val leftTurn = baseSpeed - correction
		val rightTurn = baseSpeed + correction
		// println(s"PID: $leftTurn - $rightTurn")

		leftMotor.setSpeed(leftTurn.toInt)
		rightMotor.setSpeed(rightTurn.toInt)

		lastError = error
	}

}
