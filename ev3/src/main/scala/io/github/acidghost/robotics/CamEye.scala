package io.github.acidghost.robotics

import java.io.OutputStream
import java.net.{ServerSocket, Socket}
import java.util

import org.opencv.core._
import org.opencv.highgui.{Highgui, VideoCapture}
import org.opencv.imgproc.Imgproc

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class CamEye(withStream: Boolean = true) {

	val WIDTH = 160
	val HEIGHT = 120

	private val roiRect = new Rect(0, (HEIGHT / 2) - 15, WIDTH, 30)
	private val erodeElmt = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3))
	private val dilateElmt = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5))

	var server: Option[ServerSocket] = None
	var socket: Option[Socket] = None
	if (withStream) {
		server = Some(new ServerSocket(1337))
		Future {
			server.get.accept()
		}.onSuccess { case sock =>
			writeHeader(sock.getOutputStream)
			socket = Some(sock)
		}
	}

	abstract class CameraThread extends Thread with Runnable { def close(); def getImage: Option[Mat] }

	private val cameraThread = new CameraThread {
		private val capture: VideoCapture = new VideoCapture(0)
		private val image = new Mat()
		private var done = false

		capture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, WIDTH)
		capture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, HEIGHT)
		// capture.set(Highgui.CV_CAP_PROP_BUFFERSIZE, 1)

		override def run(): Unit = {
			while (true) {
				if (capture.isOpened) {
					for (i <- 0 to 6) capture.grab()
					capture.retrieve(image)
					done = true
				}
			}
		}

		def getImage: Option[Mat] = {
			if (done) {
				done = false
				Some(image)
			} else None
		}

		def close() = capture.release()
	}
	cameraThread.start()

	def getCenters: Option[Seq[Point]] = {
		val image: Mat = cameraThread.getImage match {
			case Some(img) => img
			case None => return None
		}

		if (!image.size.equals(new Size(WIDTH, HEIGHT))) {
			// println(s"Resizing! Was ${image.size}")
			Imgproc.resize(image, image, new Size(WIDTH, HEIGHT))
		}

		val roi = image.submat(roiRect)

		if (withStream && !image.empty() && socket.isDefined) {
			writeJPEG(socket.get.getOutputStream, roi)
		}

		Imgproc.cvtColor(roi, roi, Imgproc.COLOR_BGR2GRAY)
		// Imgproc.threshold(roi, roi, 0, 255, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU)
		Imgproc.threshold(roi, roi, 60, 255, Imgproc.THRESH_BINARY)
		Core.bitwise_not(roi, roi)

		Imgproc.erode(roi, roi, erodeElmt)
		Imgproc.dilate(roi, roi, dilateElmt)

		val contours = new util.ArrayList[MatOfPoint]()
		Imgproc.findContours(roi, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

		def getCenter(contour: MatOfPoint): Option[Point] = {
			val area = Imgproc.contourArea(contour)

			if (area > 800) {
				val mu = Imgproc.moments(contour)
				if (mu.get_m00() != 0.0 && mu.get_m01() != 0.0) {
					val cx = (mu.get_m10() / mu.get_m00()).toInt
					val cy = (mu.get_m01() / mu.get_m00()).toInt
					return Some(new Point(cx, cy))
				}
			}
			None
		}

		Some(contours flatMap getCenter)
	}

	def getError: Option[Double] = {
		getCenters match {
			case Some(centers) => Some(getError(centers))
			case None => None
		}
	}

	def getError(centers: Seq[Point]): Double = {
		if (centers.nonEmpty) {
			val cx = centers.head.x
			1.0f - 2.0f * cx / WIDTH
		} else 0.0
	}

	def close() = {
		cameraThread.close()

		if (server.isDefined)
			server.get.close()

		if (socket.isDefined)
			socket.get.close()
	}

	def writeHeader(stream: OutputStream, boundary: String = "1337") =
		stream.write(("HTTP/1.0 200 OK\r\n" +
			"Connection: close\r\n" +
			"Max-Age: 0\r\n" +
			"Expires: 0\r\n" +
			"Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" +
			"Pragma: no-cache\r\n" +
			"Content-Type: multipart/x-mixed-replace; " +
			"boundary=" + boundary + "\r\n" +
			"\r\n" +
			"--" + boundary + "\r\n").getBytes)

	def writeJPEG(stream: OutputStream, image: Mat, boundary: String = "1337") = {
		val buf = new MatOfByte()
		Highgui.imencode(".jpg", image, buf)
		val imageBytes = buf.toArray
		stream.write(("Content-type: image/jpeg\r\n" +
			"Content-Length: " + imageBytes.length + "\r\n" +
			"\r\n").getBytes)
		stream.write(imageBytes)
		stream.write(("\r\n--" + boundary + "\r\n").getBytes)
	}

}
