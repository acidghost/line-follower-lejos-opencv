package io.github.acidghost.robotics;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by acidghost on 25/12/15.
 */
public class CamEyeJ {

    private static final int WIDTH = 160;
    private static final int HEIGHT = 120;
    private final VideoCapture capture;
    private final Mat image = new Mat();
    private final Mat erodeElmt = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
    private final Mat dilateElmt = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));

    public CamEyeJ() {
        capture = new VideoCapture(0);
        capture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, WIDTH);
        capture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, HEIGHT);
    }

    public List<Point> getCenters() {
        if (!capture.isOpened()) {
            return null;
        }

        capture.read(image);
        Mat roi = new Mat(image, new Rect(0, (HEIGHT / 2) - 40, WIDTH, 30));
        System.out.println("ROI: " + roi.size());

        Imgproc.cvtColor(roi, roi, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(roi, roi, new Size(9, 9), 2, 2);
        Imgproc.threshold(roi, roi, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);

        Imgproc.erode(roi, roi, erodeElmt);
        Imgproc.dilate(roi, roi, dilateElmt);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(roi, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Point> centers = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Moments mu = Imgproc.moments(contour);
            if (mu.get_m00() > 100) {
                int cx = (int) (mu.get_m10() / mu.get_m00());
                int cy = (int) (mu.get_m01() / mu.get_m00());
                centers.add(new Point(cx, cy));
            }
        }

        return centers;
    }

    public double getError(List<Point> centers) {
        if (centers.size() > 0) {
            double cx = centers.get(0).x;
            return 1.0f - 2.0f * cx / WIDTH;
        } else {
            return 0;
        }
    }

    public double getError() {
        final List<Point> centers = getCenters();
        return centers == null ? 0.0 : getError(centers);
    }

    public void close() {
        capture.release();
    }

}
