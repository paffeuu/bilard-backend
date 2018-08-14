package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DetectorException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;

import java.util.ArrayList;


@Service
public class Drawer {

	public void drawBalls(Mat img, ArrayList<Ball> balls, Scalar scalar) {
		double x, y;
		int r;
		for (int i = 0; i < balls.size(); i++) {
			// read ball coordinates
			Ball ball = balls.get(i);

			x = ball.getX();
			y = ball.getY();
			r = (int) ball.getRadius();

			Point center = new Point(x, y);

			// draw circle center
			Imgproc.circle(img, center, 3, new Scalar(0, 255, 0), 3);

			// draw circle outline
			Imgproc.circle(img, center, r, scalar, 5);

		}
	}

	public void drawExtendedCue(Mat img, Line line) {
		Imgproc.line(img, line.getBegin(), line.getEnd(), new Scalar(155, 155, 155), 4);
	}

	public void draw(Mat img, Line line, ArrayList<Ball> listOfBalls) {
		drawBalls(img, listOfBalls,  new Scalar(0, 0, 255));
		drawExtendedCue(img, line);
	}
}
