package io.github.acidghost.robotics;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;

/**
 * Created by acidghost on 25/12/15.
 */
public class PIDPilotJ {

    private double Kp = 1;
    private double Ki = .1;
    private double Kd = 0;

    private final EV3LargeRegulatedMotor leftMotor;
    private final EV3LargeRegulatedMotor rightMotor;

    private double error = 0;
    private long errorTime = System.currentTimeMillis();
    private final double baseSpeed = 80;
    private double lastError = 0.0;
    private long lastErrorTime = 0;

    private double integral = 0.0;
    private double derivative = 0.0;
    private boolean errorUpdated = false;

    public PIDPilotJ() {
        leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);
        rightMotor = new EV3LargeRegulatedMotor(MotorPort.C);
    }

    public PIDPilotJ(double Kp, double Ki, double Kd) {
        this();

        this.Kp = Kp;
        this.Ki = Ki;
        this.Kd = Kd;
    }

    public void drive() {
        leftMotor.forward();
        rightMotor.forward();
    }

    public void land() {
        leftMotor.stop();
        rightMotor.stop();
    }

    public void setError(double error) {
        this.error = (baseSpeed / 2) * error;
        lastErrorTime = errorTime;
        errorTime = System.currentTimeMillis();
    }

    public void doPID() {
        int iterationTime = (int) (errorTime - lastErrorTime);

        integral = (error / iterationTime) + integral;
        derivative = (error - lastError) / iterationTime;

        double correction = Kp * error + Ki * integral + Kd * derivative;

        double leftTurn = baseSpeed - correction;
        double rightTurn = baseSpeed + correction;
        // System.out.println(s"PID: $leftTurn - $rightTurn")

        leftMotor.setSpeed((int) leftTurn);
        rightMotor.setSpeed((int) rightTurn);

        lastError = error;
    }

}
