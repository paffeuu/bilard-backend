package pl.ncdc.hot3.pooltable.PoolTable.model;


import org.opencv.core.Point;
import pl.ncdc.hot3.pooltable.PoolTable.exceptions.LinesDetectorException;

public class Line {

    private Point begin;
    private Point end;

    public Line() {
    }

    public Line(Point start, Point end) {
        this.begin = start;
        this.end = end;
    }

    public Point getBegin() {
        return begin;
    }

    public Point getEnd() {
        return end;
    }

    public void setBegin(Point begin) {
        this.begin = begin;
    }

    public void setEnd(Point end) {
        this.end = end;
    }

    public void setPoint(Point point) {
        if (this.begin != null) {
            this.end = point;
        } else {
            this.begin = point;
        }
    }

    @Override
    public String toString() {
        return "Line{" +
                "begin=" + begin.toString() +
                ", end=" + end.toString() +
                '}';
    }

    public static Line getDirectedLine(Line a, Line b) {
        double distance1 = Line.calculateDistanceBetweenPoints(a.getBegin(), b.getBegin());
        double distance2 = Line.calculateDistanceBetweenPoints(a.getBegin(), b.getEnd());
        double distance3 = Line.calculateDistanceBetweenPoints(a.getEnd(), b.getBegin());
        double distance4 = Line.calculateDistanceBetweenPoints(a.getEnd(), b.getEnd());
        double[] distances = {distance1, distance2, distance3, distance4};
        double minDistance = distance1;

        for (double distance : distances) {
            if (minDistance > distance) {
                minDistance = distance;
            }
        }

        if (minDistance == distance1) {
            a = Line.switchPoints(a);
            b = Line.switchPoints(b);
        } else if(minDistance == distance2) {
            a = Line.switchPoints(a);
        } else if (minDistance == distance3) {
            b = Line.switchPoints(b);
        }

        Point newLineStart = new Point(
                (a.getBegin().x + b.getBegin().x) / 2,
                (a.getBegin().y + b.getBegin().y) / 2
        );

        Point newLineEnd = new Point(
                (a.getEnd().x + b.getEnd().x) / 2,
                (a.getEnd().y + b.getEnd().y) / 2
        );

        return new Line(newLineStart, newLineEnd);
    }

    public static double calculateDistanceBetweenPoints(Point a, Point b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    public static Line switchPoints(Line a) {
        Point tmp = a.getBegin();
        a.setBegin(a.getEnd());
        a.setEnd(tmp);

        return a;
    }

    public static Line predictTrajectoryAfterBump(Point bumpPoint, Line line) throws LinesDetectorException {
        double tableBandLeft = 165;
        double tableBandRight = 1948;
        double tableBandTop = 350;
        double tableBandBottom = 1236;

        Point halfDistance;

        if (tableBandLeft == bumpPoint.x) {
            halfDistance = new Point(line.getBegin().x, line.getEnd().y);
        } else if (tableBandRight == bumpPoint.x) {
            halfDistance = new Point(line.getEnd().x, line.getBegin().y);
        } else if (tableBandTop == bumpPoint.y) {
            halfDistance = new Point(line.getEnd().x, line.getBegin().y);
        } else if (tableBandBottom == bumpPoint.y) {
            halfDistance = new Point(line.getEnd().x, line.getBegin().y);
        } else {
            throw new LinesDetectorException("Band not found");
        }

        double distanceX = (halfDistance.x - line.getBegin().x);
        double distanceY = (halfDistance.y - line.getBegin().y);

        return new Line(
                bumpPoint,
                new Point(halfDistance.x + distanceX, halfDistance.y + distanceY)
        );
    }
}
