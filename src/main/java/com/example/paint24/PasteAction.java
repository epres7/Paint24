package com.example.paint24;

import javafx.scene.image.WritableImage;

public class PasteAction {
    private final WritableImage pastedImage;
    private final double x;
    private final double y;

    public PasteAction(WritableImage pastedImage, double x, double y) {
        this.pastedImage = pastedImage;
        this.x = x;
        this.y = y;
    }

    public WritableImage getPastedImage() {
        return pastedImage;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
