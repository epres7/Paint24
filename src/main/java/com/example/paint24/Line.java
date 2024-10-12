package com.example.paint24;

import javafx.scene.paint.Color;

import javafx.geometry.Point2D;
import java.util.List;

public class Line { //class that allows user to draw and edit lines

    //Instance variables
    private final double startX;
    private final double startY;
    private final double endX;
    private final double endY;
    private final List<Point2D> points;
    private final boolean isFreehand;
    private final boolean isDashed;
    private final double thickness;
    private final Color color;

    //Method to draw lines
    public Line(double startX, double startY, double endX, double endY, double thickness, Color color, boolean isDashed) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.thickness = thickness;
        this.color = color;
        this.points = null;
        this.isFreehand = false;
        this.isDashed = isDashed;
    }
    public Line(List<Point2D> points, double thickness, Color color, boolean isDashed) {
        this.points = points;
        this.thickness = thickness;
        this.color = color;
        this.startX = 0;
        this.startY = 0;
        this.endX = 0;
        this.endY = 0;
        this.isFreehand = true;
        this.isDashed = isDashed;
    }

    //Getters and setters
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
    public double getThickness() {
        return thickness;
    }
    public Color getColor() {
        return color;
    }
    public List<Point2D> getPoints() {
        return points;
    }
    public boolean isFreehand() {
        return isFreehand;
    }
    public boolean isDashed() {
        return isDashed;
    }
}