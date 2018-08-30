package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
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

	private final Scalar SOLID_DRAW_COLOR = new Scalar(0,0,255);
	private final Scalar STRIPED_DRAW_COLOR = new Scalar(0,255,0);
	private final Scalar WHITE_BALL_COLOR = new Scalar(0,0,0);
	private final Scalar BLACK_BALL_COLOR = new Scalar(255,255,255);
	private final Scalar PREDICTION_LINE_COLOR = new Scalar(155, 155, 155);
	private final Scalar CUE_COLOR = new Scalar(155, 155, 155);
	private final Scalar TARGET_LINE_COLOR = new Scalar(0, 0, 255);
	private final Scalar GHOST_BALL_COLOR = new Scalar(0, 255, 255);
	private final Scalar TARGET_TUNNEL_COLOR = new Scalar(94, 12, 3);

    private Properties properties;
	private BandsService bandsService;
	private TargetLineService targetLineService;

	List<MatOfPoint> tunnelPollyPoints;

	@Autowired
    public Drawer(
            Properties properties,
			BandsService bandsService,
			TargetLineService targetLineService
    ) {
        this.properties = properties;
        this.bandsService = bandsService;
        this.targetLineService = targetLineService;

		tunnelPollyPoints = new ArrayList<>();
    }

	public void draw(Mat img, Line cue, List<Ball> listOfBalls, List<Line> predictions, Line targetLine) throws DrawerException {
		if (img == null) {
			throw new DrawerException("Cannot draw line to null image.");
		}

		if (cue != null) {
			drawLine(img, cue, CUE_COLOR, 8);
		}

		if (listOfBalls != null && !listOfBalls.isEmpty()) {
			drawBalls(img, listOfBalls, null);
		}

		if (predictions != null && !predictions.isEmpty()) {
			for (Line line : predictions) {
				drawLine(img, line, PREDICTION_LINE_COLOR, 8);
			}
			if (targetLine == null) {
				drawPocketForLine(img, predictions.get(predictions.size() - 1));
			}
		}

		Line minTargetLine = targetLineService.getMinSideLine();
		if (minTargetLine != null) {
			drawLine(img, minTargetLine, TARGET_TUNNEL_COLOR, 7);
		}

		Line maxTargetLine = targetLineService.getMaxSideLine();
		if (maxTargetLine != null) {
			drawLine(img, maxTargetLine, TARGET_TUNNEL_COLOR, 7);
		}

		if (maxTargetLine != null && minTargetLine != null && targetLineService.getAverageLine() != null){
			Point pocketPoint = null;
			String firstBand = bandsService.getClosestBandName(bandsService.getDistsToBands(maxTargetLine.getEnd()));
			String secondBand = bandsService.getClosestBandName(bandsService.getDistsToBands(minTargetLine.getEnd()));

			if (firstBand != secondBand) {
				pocketPoint = bandsService.getPointForPocketEnum(bandsService.getPocketForPoint(targetLineService.getAverageLine().getEnd()));
			}

			Point[] tunnelPoints = {
					maxTargetLine.getBegin(),
					maxTargetLine.getEnd(),
					pocketPoint == null ? targetLineService.getAverageLine().getEnd() : pocketPoint,
					minTargetLine.getEnd(),
					minTargetLine.getBegin()
			};
			MatOfPoint matOfPoint = new MatOfPoint();
			matOfPoint.fromArray(tunnelPoints);

			tunnelPollyPoints.add(matOfPoint);

			Imgproc.fillPoly(img, tunnelPollyPoints, new Scalar(94, 12, 3, 0.5));
		}
		tunnelPollyPoints.clear();

		if (null != targetLine) {
			drawLine(img, targetLine, TARGET_TUNNEL_COLOR, 8);
			drawCircle(img, targetLine.getBegin(), properties.getBallExpectedRadius(), GHOST_BALL_COLOR, 4);
			drawPocketForLine(img, targetLine);
		}


	}

	public void drawBalls(Mat img, List<Ball> balls, Scalar color) throws DrawerException {

		if (img == null) {
			throw new DrawerException("Cannot draw on an empty image source.");
		} else if (null == balls || balls.isEmpty()) {
			throw new DrawerException("Cannot draw empty ball list.");
		}


		for (Ball ball : balls) {
			Point center = new Point(ball.getX(), ball.getY());

			if(color == null){
				if(ball.getId() >= properties.getFirstSolidBallId() && ball.getId() < properties.getFirstStripedBallId()) {
					Imgproc.circle(img, center, properties.getBallExpectedRadius(), SOLID_DRAW_COLOR, properties.getBallThickness());
				} else if(ball.getId() >= properties.getFirstStripedBallId()){
					Imgproc.circle(img, center, properties.getBallExpectedRadius(), STRIPED_DRAW_COLOR, properties.getBallThickness());
				} else if(ball.getId() == properties.getBlackBallId()) {
					Imgproc.circle(img, center, properties.getBallExpectedRadius(), BLACK_BALL_COLOR, properties.getBallThickness());
				} else if(ball.getId() == properties.getWhiteBallId()) {
					Imgproc.circle(img, center, properties.getBallExpectedRadius(), WHITE_BALL_COLOR, properties.getBallThickness());
				}
			} else {
				Imgproc.circle(img, center, properties.getBallExpectedRadius(), color, properties.getBallThickness());
			}
		}
	}

	public void drawLines(Mat img, List <Line> lines, Scalar scalar, int thickness) {
		if (lines != null && !lines.isEmpty()) {
			for (Line line : lines) {
				Imgproc.line(img, line.getBegin(), line.getEnd(), scalar, thickness);
			}
		}
	}

	public void drawLine(Mat img, Line line, Scalar scalar, int thickness) {
        Imgproc.line(img, line.getBegin(), line.getEnd(), scalar, thickness);
    }

    public void drawCircle(Mat img, Point point, int radius, Scalar scalar, int thickness) {
        Imgproc.circle(img, point, radius, scalar, thickness);
    }

	public void drawPoint(Mat img, Point point) {
		Imgproc.circle(img, point, properties.getTablePocketRadius(), new Scalar(0, 0, 255), 5);
	}

	public void drawPoint(Mat img, Point point, int radius) {
		Imgproc.circle(img, point, radius, new Scalar(0, 0, 255), 5);
	}

	public void drawPoint(Mat img, Point point, Scalar color, int thickness) {
		Imgproc.circle(img, point, 14, color, thickness);
	}
	public void drawPoint(Mat img, Point point, Scalar color, int thickness, int radius) {
		Imgproc.circle(img, point, radius, color, thickness);
	}

	private void drawPocketForLine(Mat img, Line line) {
		BandsService.PocketPosition pocket = bandsService.getPocketForPoint(line.getEnd());
		if (pocket != BandsService.PocketPosition.NONE) {
			Point pocketPoint = bandsService.getPointForPocketEnum(pocket);
			if (pocketPoint != null) {
				drawPoint(img, pocketPoint, new Scalar(255, 227, 170), 7, properties.getTablePocketRadius());
			}
		}
	}


}
