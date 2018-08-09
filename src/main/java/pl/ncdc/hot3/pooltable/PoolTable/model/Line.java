package pl.ncdc.hot3.pooltable.PoolTable.model;


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

    public void setBegin(Point begin) {
        this.begin = begin;
    }

    public void setEnd(Point end) {
        this.end = end;
    }

    public void setPoint(Point point){
        if (this.begin != null)
            this.end = point;
        else
            this.begin = point;
    }

    @Override
    public String toString() {
        return "Line{" +
                "begin=" + begin +
                ", end=" + end +
                '}';
    }
}
