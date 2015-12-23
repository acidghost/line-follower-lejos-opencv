package io.github.acidghost.robotics

import java.util

import org.opencv.core._
import org.opencv.highgui.VideoCapture
import org.opencv.imgproc.Imgproc

import scala.collection.JavaConversions._


class CamEye {

	val WIDTH = 320
	val HEIGHT = 240

	private val capture: VideoCapture = new VideoCapture(0)
	private val image = new Mat()

	// FIXME
	capture.set(3, WIDTH)
	capture.set(4, HEIGHT)
	// println(s"Set camera capture: ${capture.get(3)}x${capture.get(4)}\n")

	def getCenters: Option[Seq[Point]] = {
		if (!capture.isOpened)
			return None

		capture.read(image)
		Imgproc.resize(image, image, new Size(WIDTH, HEIGHT))
		val roi = new Mat(image, new Rect(10, 2 * image.rows() / 3, image.cols() - 20, image.rows() / 12))

		Imgproc.cvtColor(roi, roi, Imgproc.COLOR_BGR2GRAY)
		Imgproc.GaussianBlur(roi, roi, new Size(9, 9), 2, 2)
		Imgproc.threshold(roi, roi, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU)
		val erodeElmt = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3))
		Imgproc.erode(roi, roi, erodeElmt)
		val dilateElmt = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5))
		Imgproc.dilate(roi, roi, dilateElmt)

		val contours = new util.ArrayList[MatOfPoint]()
		Imgproc.findContours(roi, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

		def getCenter(contour: MatOfPoint) = {
			val mu = Imgproc.moments(contour)
			if (mu.get_m00() > 100) {
				val cx = (mu.get_m10() / mu.get_m00()).toInt
				val cy = (mu.get_m01() / mu.get_m00()).toInt
				Some(new Point(cx, cy))
			} else {
				None
			}
		}

		Some(contours flatMap getCenter)
	}

	def getError(centers: Seq[Point] = getCenters.getOrElse(Seq())): Double = {
		if (centers.nonEmpty) {
			val cx = centers.head.x
			1.0f - 2.0f * cx / WIDTH
		} else {
			0.0
		}
	}

	def close() = capture.release()

}
