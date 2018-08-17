package pl.ncdc.hot3.pooltable.PoolTable.services;

import org.springframework.stereotype.Service;
import pl.ncdc.hot3.pooltable.PoolTable.model.Ball;
import pl.ncdc.hot3.pooltable.PoolTable.model.Line;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Service
public class PreviousPositionService {
    private ArrayList<ArrayList<Ball>> positionsHistory;
    private ArrayList<Ball> previousPosition;
    private ArrayList<Ball> playerSavedPosition;
    private Line playerSavedStick;
    private boolean showPrevious = true;

    public PreviousPositionService() {
        this.positionsHistory = new ArrayList<ArrayList<Ball>>();
    }

    private void updatePreviousBallPosition(Collection<Ball> position) {
        this.previousPosition.clear();
        this.previousPosition.addAll(position);
    }
    public void addPosition(ArrayList<Ball> position) {
        this.positionsHistory.add(position);
    }

    private boolean comparePositions(ArrayList<Ball> col1, ArrayList<Ball> col2) {
        return col1.containsAll(col2) && col2.containsAll(col1);

    }

    public void updatePreviousBallPosition() {
        ArrayList<Ball> currentPosition = this.positionsHistory.get(this.positionsHistory.size()-1);
        if (this.previousPosition == null) {
            this.previousPosition = this.positionsHistory.get(this.positionsHistory.size()-1);
            System.out.println("position first set");
            return;
        }

        for (int i=positionsHistory.size()-1; i>0; i--) {
            if (this.comparePositions(this.positionsHistory.get(i), this.positionsHistory.get(i - 1))) {
               // System.out.println("position found");
                if (comparePositions(this.positionsHistory.get(i), currentPosition)) {
                        continue;
                    }
                if (!comparePositions(this.positionsHistory.get(i), this.previousPosition)) {
                    System.out.println("new position found");
                    this.previousPosition = this.positionsHistory.get(i);
                    this.positionsHistory.subList(0, i).clear();
                    return;
                } else {
                    return;
                }
            }
        }
    }

    public ArrayList<Ball> getPreviousPosition() {
        if (this.playerSavedPosition != null) {
            return this.playerSavedPosition;
        }
        return this.previousPosition;
    }

    public boolean isShowPrevious() {
        return showPrevious;
    }

    public void setPlayerSavedPosition(ArrayList<Ball> playerPosition) {
        this.playerSavedPosition = playerPosition;
    }

    public void clearPlayerSavedPosition() {
        this.playerSavedPosition = null;
    }

    public void setPlayerSavedStick(Line stick) {
        this.playerSavedStick = stick;
    }

    public void clearPlayerSavedStick() {
        this.playerSavedStick = null;
    }

    public void setShowPrevious(boolean showPrevious) {
        this.showPrevious = showPrevious;
    }
}
