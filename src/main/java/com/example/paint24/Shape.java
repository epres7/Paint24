package com.example.paint24;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Shape {
    public enum ShapeType {
        RECTANGLE, CIRCLE, ELLIPSE, SQUARE, TRIANGLE, STAR, RIGHT_TRIANGLE, NGON
    }

    private final double startX;
    private final double startY;
    private final double endX;
    private final double endY;
    private final double thickness;
    private final Color color;
    private final ShapeType shapeType;
    private final boolean dashed;
    private int numSides;

    public Shape(double startX, double startY, double endX, double endY, double thickness, Color color, ShapeType shapeType, boolean dashed, int numSides) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.thickness = thickness;
        this.color = color;
        this.shapeType = shapeType;
        this.dashed = dashed;
        this.numSides = numSides;
    }

    public void draw(GraphicsContext gc) {
        gc.setStroke(color);
        gc.setLineWidth(thickness);
        gc.setLineDashes(dashed ? new double[]{10, 5} : null);

        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);
        double topLeftX = Math.min(startX, endX);
        double topLeftY = Math.min(startY, endY);

        switch (shapeType) {
            case RECTANGLE:
                drawRectangle(gc, topLeftX, topLeftY, width, height);
                break;
            case CIRCLE:
                drawCircle(gc, topLeftX, topLeftY, width, height);
                break;
            case ELLIPSE:
                drawEllipse(gc, topLeftX, topLeftY, width, height);
                break;
            case SQUARE:
                drawSquare(gc, topLeftX, topLeftY, width, height);
                break;
            case TRIANGLE:
                drawEquilateralTriangle(gc);
                break;
            case RIGHT_TRIANGLE:
                drawRightTriangle(gc);
                break;
            case NGON:
                drawNgon(gc, numSides);
                break;
            case STAR:
                drawStar(gc);
                break;
        }
    }
    private void drawRectangle(GraphicsContext gc, double x, double y, double width, double height) {
        gc.strokeRect(x, y, width, height);
    }

    private void drawCircle(GraphicsContext gc, double x, double y, double width, double height) {
        double radius = Math.min(width, height);
        gc.strokeOval(x, y, radius, radius);
    }

    private void drawEllipse(GraphicsContext gc, double x, double y, double width, double height) {
        gc.strokeOval(x, y, width, height);
    }

    private void drawSquare(GraphicsContext gc, double x, double y, double width, double height) {
        double side = Math.min(width, height);
        gc.strokeRect(x, y, side, side);
    }

    private void drawEquilateralTriangle(GraphicsContext gc) {
        gc.strokePolygon(
                new double[]{startX, (startX + endX) / 2, endX},
                new double[]{endY, startY, endY},
                3
        );
    }

    private void drawRightTriangle(GraphicsContext gc) {
        gc.strokePolygon(
                new double[]{startX, endX, startX},
                new double[]{startY, endY, endY},
                3
        );
    }

    private void drawNgon(GraphicsContext gc, int sides) {
        if (sides < 3) return;
        double centerX = (startX + endX) / 2;
        double centerY = (startY + endY) / 2;
        double radius = Math.min(Math.abs(endX - startX), Math.abs(endY - startY)) / 2;
        double angleStep = 2 * Math.PI / sides;
        double[] xPoints = new double[sides];
        double[] yPoints = new double[sides];
        for (int i = 0; i < sides; i++) {
            double angle = i * angleStep - Math.PI / 2;
            xPoints[i] = centerX + radius * Math.cos(angle);
            yPoints[i] = centerY + radius * Math.sin(angle);
        }
        gc.strokePolygon(xPoints, yPoints, sides);
    }

    private void drawStar(GraphicsContext gc) {
        double centerX = (startX + endX) / 2;
        double centerY = (startY + endY) / 2;
        double outerRadius = Math.min(Math.abs(endX - startX), Math.abs(endY - startY)) / 2;
        double innerRadius = outerRadius * 0.5;
        double angleStep = Math.PI / 5;
        double[] xPoints = new double[5 * 2];
        double[] yPoints = new double[5 * 2];
        for (int i = 0; i < 5 * 2; i++) {
            double angle = i * angleStep - Math.PI / 2;
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            xPoints[i] = centerX + radius * Math.cos(angle);
            yPoints[i] = centerY + radius * Math.sin(angle);
        }
        gc.strokePolygon(xPoints, yPoints, 5 * 2);
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
    public ShapeType getShapeType() {
        return shapeType;
    }
}

