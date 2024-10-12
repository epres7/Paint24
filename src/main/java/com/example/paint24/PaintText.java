package com.example.paint24;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PaintText {
    private final double x;
    private final double y;
    private final String text;
    private final double fontSize;
    private final Color color;

    public PaintText(double x, double y, String text, double fontSize, Color color) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.fontSize = fontSize;
        this.color = color;
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        gc.setFont(new javafx.scene.text.Font(fontSize));
        gc.fillText(text, x, y);
    }
}
