package io.github.acidghost.robotics;

import lejos.hardware.Button;
import org.opencv.core.Core;
import org.opencv.core.Point;

import java.util.List;

/**
 * Created by acidghost on 25/12/15.
 */
public class LineFollowerJ {

    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        System.out.println("Java implementation\n");

        CamEyeJ eye = new CamEyeJ();
        System.out.println("Eye started");

        PIDPilotJ pilot = new PIDPilotJ();
        pilot.drive();
        System.out.println("Pilot started");

        while (Button.ESCAPE.isUp()) {
            long timer = System.currentTimeMillis();
            List<Point> centers = eye.getCenters();
            if (centers != null) {
                double error = eye.getError(centers);
                System.out.println("Found error: " + error);
                pilot.setError(error);
                pilot.doPID();
                System.out.println("Time: " + (System.currentTimeMillis() - timer));
            } else {
                System.out.println("Camera not ready...");
            }
        }

        pilot.land();
        eye.close();

    }

}
