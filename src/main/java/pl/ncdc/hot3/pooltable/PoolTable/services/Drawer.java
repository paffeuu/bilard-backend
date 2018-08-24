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
	// tymczasowo
	private final Scalar SOLID_DRAW_COLOR = new Scalar(0,0,255);
	private final Scalar STRIPED_DRAW_COLOR = new Scalar(0,255,0);

    private Properties properties;

    @Autowired
    public Drawer(
            Properties properties
    ) {
        this.properties = properties;
    }

	public void drawBalls(Mat img, List<Ball> balls, Scalar scalar) throws DrawerException {
		if (img == null)
			throw new DrawerException("Cannot draw on an empty image source.");
		else if (null == balls || balls.isEmpty())
            throw new DrawerException("Cannot draw empty ball list.");


        int r;
		for (Ball ball : balls) {
			r = (int) ball.getRadius();
			Point center = new Point(ball.getX(), ball.getY());

			if(scalar == null){
				if(ball.getId() >= 10 && ball.getId() < 30) {
					Imgproc.circle(img, center, r, SOLID_DRAW_COLOR, 5);
				} else if(ball.getId() >= 30 && ball.getId() < 50){
					Imgproc.circle(img, center, r, STRIPED_DRAW_COLOR, 5);
				} else if(ball.getId() == 8) {
					Imgproc.circle(img, center, r, new Scalar(255, 255, 255), 5);
				} else if(ball.getId() == 0) {
					Imgproc.circle(img, center, r, new Scalar(0, 0, 0), 5);
				}
			} else {
				Imgproc.circle(img, center, r, scalar, 5);
			}

		}
	}

    private void drawLine(Mat img, Line line, Scalar scalar, int thickness) {
        Imgproc.line(img, line.getBegin(), line.getEnd(), scalar, thickness);
    }

    private void drawCircle(Mat img, Point point, int radius, Scalar scalar, int thickness) {
        Imgproc.circle(img, point, radius, scalar, thickness);
    }

    public void draw(Mat img, Line cue, List<Ball> listOfBalls, List<Line> predictions, Line targetLine) throws DrawerException {
        if (img == null) {
            throw new DrawerException("Cannot draw line to null image.");
        }

		if (cue != null)
			drawLine(img, cue, new Scalar(155, 155, 155), 8);

		if (listOfBalls != null && !listOfBalls.isEmpty())
			drawBalls(img, listOfBalls,  null);

		if (predictions != null && !predictions.isEmpty()) {
			for (Line line : predictions) {
				drawLine(img, line, new Scalar(155, 155, 155), 8);
			}
		}

        if (null != targetLine) {
        	drawLine(img, targetLine, new Scalar(0, 0, 255), 8);
        	drawCircle(img, targetLine.getBegin(), properties.getBallExpectedRadius(), new Scalar(0, 255, 255), 4);
		}
	}

	public void drawPoint(Mat img, Point point) {
		Imgproc.circle(img, point, properties.getTablePocketRadius(), new Scalar(0, 0, 255), 5);
	}
}
