package pl.ncdc.hot3.pooltable.PoolTable.model;

import org.opencv.core.Point;
import org.springframework.stereotype.Component;

@Component
public class ConfigurableProperties {
    private boolean debugActive;
    private boolean showPreviousPosition;
    private int gameMode; // 0 - normal mode, 1 - prediction based on ball and Pocket position, 2 - ghost ball

    // Projector edges
    private Point projectorLeftTopCorner;
    private Point projectorRightTopCorner;
    private Point projectorRightBottomCorner;
    private Point projectorLeftBottomCorner;

    public ConfigurableProperties() {
        this.debugActive = true;
        this.showPreviousPosition = true;
        this.gameMode = 0;

        this.projectorLeftTopCorner = new Point(60, 67);
        this.projectorRightTopCorner = new Point(1214.0, 112.0);
        this.projectorRightBottomCorner = new Point(1210.0, 672.0);
        this.projectorLeftBottomCorner = new Point(56, 666);
    }

    public boolean isShowPreviousPosition() {
        return showPreviousPosition;
    }

    public void setShowPreviousPosition(boolean showPreviousPosition) {
        this.showPreviousPosition = showPreviousPosition;
    }

    public boolean isDebugActive() {
        return debugActive;
    }

    public void setDebugActive(boolean debugActive) {
        this.debugActive = debugActive;
    }

    public int getGameMode() {
        return gameMode;
    }

    public void setGameMode(int gameMode) {
        this.gameMode = gameMode;
    }

    public Point getProjectorLeftTopCorner() {
        return projectorLeftTopCorner;
    }

    public void setProjectorLeftTopCorner(Point projectorLeftTopCorner) {
        this.projectorLeftTopCorner = projectorLeftTopCorner;
    }

    public Point getProjectorRightTopCorner() {
        return projectorRightTopCorner;
    }

    public void setProjectorRightTopCorner(Point projectorRightTopCorner) {
        this.projectorRightTopCorner = projectorRightTopCorner;
    }

    public Point getProjectorRightBottomCorner() {
        return projectorRightBottomCorner;
    }

    public void setProjectorRightBottomCorner(Point projectorRightBottomCorner) {
        this.projectorRightBottomCorner = projectorRightBottomCorner;
    }

    public Point getProjectorLeftBottomCorner() {
        return projectorLeftBottomCorner;
    }

    public void setProjectorLeftBottomCorner(Point projectorLeftBottomCorner) {
        this.projectorLeftBottomCorner = projectorLeftBottomCorner;
    }
}
