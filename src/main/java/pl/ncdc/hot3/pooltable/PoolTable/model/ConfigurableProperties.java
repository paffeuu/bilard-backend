package pl.ncdc.hot3.pooltable.PoolTable.model;

import org.springframework.stereotype.Component;

@Component
public class ConfigurableProperties {
    private boolean debugActive;
    private boolean showPreviousPosition;
    private int gameMode; // 0 - normal mode, 1 - prediction based on ball and Pocket position, 2 - ghost ball

    public ConfigurableProperties() {
        this.debugActive = true;
        this.showPreviousPosition = true;
        this.gameMode = 0;
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
}
