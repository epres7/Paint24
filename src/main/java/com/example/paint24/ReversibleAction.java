package com.example.paint24;

import javafx.scene.image.WritableImage;

public class ReversibleAction {
    private final String actionType;
    private final double startX;
    private final double startY;
    private final double endX;
    private final double endY;
    private final WritableImage capturedImage;

    public ReversibleAction(String actionType, double startX, double startY, double endX, double endY, WritableImage capturedImage) {
        this.actionType = actionType;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.capturedImage = capturedImage;
    }

    public String getActionType() {
        return actionType;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }

    public WritableImage getCapturedImage() {
        return capturedImage;
    }
}

