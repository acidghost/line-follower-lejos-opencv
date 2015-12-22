package io.github.acidghost.robotics

import lejos.hardware.{Button, BrickFinder}
import lejos.hardware.ev3.EV3
import org.opencv.core._
import org.opencv.highgui.VideoCapture
import org.opencv.imgproc.Imgproc


class LineFollower {

	val WIDTH = 320
	val HEIGHT = 240

	val ev3  = BrickFinder.getLocal.asInstanceOf[EV3]
	val capture: VideoCapture = new VideoCapture(0)

	// FIXME
	capture.set(3, WIDTH)
	capture.set(4, HEIGHT)

	def start() = {
		val image = new Mat()

		while (Button.UP.isUp) {
			if (capture.isOpened) {
				capture.read(image)
				Imgproc.resize(image, image, new Size(WIDTH, HEIGHT))

				val roi = new Mat(image, new Rect(10, 2 * image.rows() / 3, image.cols() - 20, image.rows() / 12))
				println(s"Found ROI of size ${roi.size}")

				Imgproc.cvtColor(roi, roi, Imgproc.COLOR_BGR2GRAY)
				Imgproc.GaussianBlur(roi, roi, new Size(9, 9), 2, 2)
				Imgproc.threshold(roi, roi, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU)
				val erodeElmt = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3))
				Imgproc.erode(roi, roi, erodeElmt)
				val dilateElmt = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5))
				Imgproc.dilate(roi, roi, dilateElmt)

				val contours =  new java.util.ArrayList[MatOfPoint]()
				Imgproc.findContours(roi, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
				for (i <- 0 until contours.size) {
					val contour = contours.get(i)
					val mu = Imgproc.moments(contour)
					if (mu.get_m00() > 100) {
						val area = Imgproc.contourArea(contour)
						println(s"Found area of $area")

						val cx = (mu.get_m10() / mu.get_m00()).toInt
						val cy = (mu.get_m01() / mu.get_m00()).toInt
						println(s"Found center at ($cx, $cy)")
					}
				}
				println("\n\n")
			}
		}

		capture.release()
	}

	def doHello() = {
		val lcd  = ev3.getTextLCD
		lcd.drawString("Hello World", 4, 3)
		lcd.drawString("I'm a Scala app", 1, 4)
		Button.waitForAnyPress()
	}

}


object LineFollower extends App {

	System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

	val lineFollower = new LineFollower
	lineFollower.start()

}
