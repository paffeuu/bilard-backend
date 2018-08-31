package pl.ncdc.hot3.pooltable.PoolTable.model;


public class BallPocket {
    private Ball ball;
    private int pocket;

    public int getPocket() {
        return pocket;
    }

    public void setPocket(int pocket) {
        this.pocket = pocket;
    }

    @Override
    public String toString() {
        return "Pocket " + pocket +"\nball x:" + ball.getX() + " y:" + ball.getY();
    }

    public Ball getBall() {
        return ball;
    }

    public void setBall(Ball ball) {
        this.ball = ball;
    }
}
