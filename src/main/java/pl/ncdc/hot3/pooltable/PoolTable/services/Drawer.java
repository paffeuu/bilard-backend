package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.DrawerException;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;
import pl.ncdc.hot3.pooltable.PoolTable.model.Properties;

import java.util.ArrayList;
import java.util.List;


@Service
public class Drawer {

	private Properties properties;

	@Autowired
	public Drawer(
			Properties properties
	) {
		this.properties = properties;
	}

	public void drawBalls(Mat img, ArrayList<Ball> balls, Scalar scalar) throws DrawerException {
		if (img == null)
			throw new DrawerException("Cannot draw on an empty image source.");
		else if (null == balls || balls.isEmpty())
			return;

		int r;
		for (Ball ball : balls) {
			r = (int) ball.getRadius();
			Point center = new Point(ball.getX(), ball.getY());

			if(scalar == null){
				if(ball.getId() > 0 && ball.getId() < 8) {
					Imgproc.circle(img, center, r, properties.getSolidDrawColor(), 5);
				} else if(ball.getId() > 8 && ball.getId() < 16){
					Imgproc.circle(img, center, r, properties.getStripedDrawColor(), 5);
				} else if(ball.getId() == 8) {
					Imgproc.circle(img, center, r, new Scalar(0, 0, 0), 5);
				} else if(ball.getId() == 0) {
					Imgproc.circle(img, center, r, new Scalar(255, 255, 255), 5);
				}
			} else {
				Imgproc.circle(img, center, r, scalar, 5);
			}

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

		if (listOfBalls != null && !listOfBalls.isEmpty())
			drawBalls(img, listOfBalls,  null);

		if (predictions != null && !predictions.isEmpty()) {
			for (Line line : predictions) {
				drawLine(img, line);
			}
		}

        if (null != targetLine) {
        	drawLine(img, targetLine);
		}
	}
}
