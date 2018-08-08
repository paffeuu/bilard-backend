package pooltable.PoolTable;


import org.opencv.core.Point;

public class Line {

    private Point begin;
    private Point end;

    public Line(){};

    public Line(Point start, Point end){
        this.begin = start;
        this.end = end;
    };

    public Point getBegin() {
        return begin;
    }

    public Point getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "Line{" +
                "begin=" + begin +
                ", end=" + end +
                '}';
    }
}
