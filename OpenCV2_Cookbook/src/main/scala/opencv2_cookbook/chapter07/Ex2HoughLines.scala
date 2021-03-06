/*
 * Copyright (c) 2011-2014 Jarek Sacha. All Rights Reserved.
 *
 * Author's e-mail: jpsacha at gmail.com
 */

package opencv2_cookbook.chapter07

import java.io.File

import opencv2_cookbook.OpenCVUtils._
import org.bytedeco.javacpp.indexer.FloatBufferIndexer
import org.bytedeco.javacpp.opencv_core._
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacpp.opencv_imgproc._

import scala.math._


/**
 * Detect lines using standard Hough transform approach.
 * The example for section "Detecting lines in image with the Hough transform" in Chapter 7, page 167.
 *
 * @see JavaCV sample [https://code.google.com/p/javacv/source/browse/javacv/samples/HoughLines.java]
 */
object Ex2HoughLines extends App {

  // Read input image
  val src = loadAndShowOrExit(new File("data/road.jpg"), IMREAD_GRAYSCALE)

  // Canny contours
  val canny      = new Mat()
  val threshold1 = 125
  val threshold2 = 350
  val apertureSize = 3
  Canny(src, canny, threshold1, threshold2, apertureSize, false /*L2 gradient*/)
  show(canny, "Canny Contours")

  // Hough transform for line detection
  val lines                      = new Mat()
  val storage                    = cvCreateMemStorage(0)
  val method                     = HOUGH_STANDARD
  val distanceResolutionInPixels = 1
  val angleResolutionInRadians   = Pi / 180
  val minimumVotes               = 80
  HoughLines(
    canny,
    lines,
    distanceResolutionInPixels,
    angleResolutionInRadians,
    minimumVotes)

  // Draw lines on the canny contour image
  val indexer = lines.createIndexer().asInstanceOf[FloatBufferIndexer]
  val result  = new Mat()
  src.copyTo(result)
  cvtColor(src, result, COLOR_GRAY2BGR)
  for (i <- 0 until lines.rows()) {
    val rho = indexer.get(i, 0, 0)
    val theta = indexer.get(i, 0, 1)

    val (pt1, pt2) = if (theta < Pi / 4.0 || theta > 3.0 * Pi / 4.0) {
      // ~vertical line
      // point of intersection of the line with first row
      val p1 = new Point(round(rho / cos(theta)).toInt, 0)
      // point of intersection of the line with last row
      val p2 = new Point(round((rho - result.rows * sin(theta)) / cos(theta)).toInt, result.rows)
      (p1, p2)
    } else {
      // ~horizontal line
      // point of intersection of the line with first column
      val p1 = new Point(0, round(rho / sin(theta)).toInt)
      // point of intersection of the line with last column
      val p2 = new Point(result.cols, round((rho - result.cols * cos(theta)) / sin(theta)).toInt)
      (p1, p2)
    }

    // draw a white line
    line(result, pt1, pt2, new Scalar(0, 0, 255, 0), 1, LINE_8, 0)
  }

  save(new File("result.tif"), result)
  show(toMat8U(result), "Hough Lines")
}
