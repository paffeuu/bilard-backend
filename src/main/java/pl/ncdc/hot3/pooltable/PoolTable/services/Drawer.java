package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DrawerException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;

import java.util.ArrayList;
import java.util.List;


@Service
public class Drawer {

	public void drawBalls(Mat img, ArrayList<Ball> balls, Scalar scalar) throws DrawerException {
		if (img == null)
			throw new DrawerException("Cannot draw on an empty image source.");
		else if (null == balls || balls.isEmpty())
			return;

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

	private void drawLine(Mat img, Line line) {
		Imgproc.line(img, line.getBegin(), line.getEnd(), new Scalar(155, 155, 155), 4);
	}

	public void draw(Mat img, Line cue, ArrayList<Ball> listOfBalls, List<Line> predictions, Line targetLine) throws DrawerException {
		if (img == null)
			throw new DrawerException("Cannot draw line to null image.");

		if (cue != null)
			drawLine(img, cue);

		if (!listOfBalls.isEmpty())
			drawBalls(img, listOfBalls,  new Scalar(0, 0, 255));

		for (Line line : predictions) {
            drawLine(img, line);
        }

        if (null != targetLine) {
        	drawLine(img, targetLine);
		}
	}
}
