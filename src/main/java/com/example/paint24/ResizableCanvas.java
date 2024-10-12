package com.example.paint24;

import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ResizableCanvas extends Canvas {
    private Image loadedImage = null;
    private final List<Line> lines = new ArrayList<>();
    private final List<Shape> shapes = new ArrayList<>();
    private final List<PaintText> texts = new ArrayList<>();
    private final List<PasteAction> pastedImages = new ArrayList<>();
    public double startX;
    public double startY;
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private double selectionStartX, selectionStartY, selectionEndX, selectionEndY;
    private WritableImage copiedImage = null;
    private final Stack<Object> undoStack = new Stack<>();
    private final Stack<Object> redoStack = new Stack<>();
    private boolean selectionActive = false;
    @Override
    public boolean isResizable() { return true; }

    public ResizableCanvas() {
        this(800, 600);
    }
    public ResizableCanvas(double width, double height) {
        super(width, height);
    }
    public void setImage(Image image) {
        this.loadedImage = image;
        redraw();
    }
    public void addPastedImage(WritableImage image, double x, double y) {
        PasteAction pasteAction = new PasteAction(image, x, y);
        undoStack.push(pasteAction);
        redoStack.clear();
        redraw();
    }
    public WritableImage getCopiedImage() {
        return copiedImage;
    }

    public Image getImage() {
        return loadedImage;
    }
    public double getLastMouseX() {
        return lastMouseX;
    }
    public double getLastMouseY() {
        return lastMouseY;
    }
    public void addLine(Line line) { //Adds line to array for redrawing purposes
        lines.add(line);
        undoStack.push(line);
        redoStack.clear();
        redraw();
    }
    public void addShape(Shape shape) {//Adds shape to array for redrawing purposes
        shapes.add(shape);
        undoStack.push(shape);
        redoStack.clear();
        redraw();
    }
    public void addText(PaintText text) {//Adds text to array for redrawing purposes
        texts.add(text);
        undoStack.push(text);
        redoStack.clear();
        redraw();
    }
    public void redraw() { //Redraws the content of the canvas
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillRect(0, 0, getWidth(), getHeight());
        if (loadedImage != null) {
            double canvasWidth = getWidth();
            double canvasHeight = getHeight();
            double imageWidth = loadedImage.getWidth();
            double imageHeight = loadedImage.getHeight();
            double scaleX = canvasWidth / imageWidth;
            double scaleY = canvasHeight / imageHeight;
            double scale = Math.min(scaleX, scaleY);
            double newWidth = imageWidth * scale;
            double newHeight = imageHeight * scale;
            double x = (canvasWidth - newWidth) / 2;
            double y = (canvasHeight - newHeight) / 2;
            gc.drawImage(loadedImage, x, y, newWidth, newHeight);
        }
        for (PasteAction pasteAction : pastedImages) {
            gc.drawImage(pasteAction.getPastedImage(), pasteAction.getX(), pasteAction.getY());
        }
        for (Line line : lines) { //If there are lines, redraws lines onto canvas after resizing
            gc.setStroke(line.getColor());
            gc.setLineWidth(line.getThickness());
            if (line.isDashed()) {
                gc.setLineDashes(10, 5);
            } else {
                gc.setLineDashes(0);
            }
            if (line.isFreehand()) {
                List<Point2D> points = line.getPoints();
                for (int i = 0; i < points.size() - 1; i++) {
                    Point2D p1 = points.get(i);
                    Point2D p2 = points.get(i + 1);
                    gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                }
            } else {
                gc.strokeLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
            }
        }
        for (Shape shape : shapes) { //If there are shapes, redraws shapes onto canvas after resizing
            shape.draw(gc);
        }
        for (PaintText text : texts) { //If there is text, redraws text after resizing
            text.draw(gc);
        }
        for (PasteAction pasteAction : pastedImages) { //If there's a pasted image, redraws after resizing
            gc.drawImage(pasteAction.getPastedImage(), pasteAction.getX(), pasteAction.getY());
        }
        if (selectionActive) {
            gc.setStroke(Color.BLACK);
            gc.setLineDashes(5);
            gc.setLineWidth(1);
            double x = Math.min(selectionStartX, selectionEndX);
            double y = Math.min(selectionStartY, selectionEndY);
            double width = Math.abs(selectionEndX - selectionStartX);
            double height = Math.abs(selectionEndY - selectionStartY);
            gc.strokeRect(x, y, width, height);
        }
    }
    public void clearEdits() {
        this.shapes.clear();
        this.lines.clear();
        this.texts.clear();
        redraw();
    }
    public void undo() {
        if (!undoStack.isEmpty()) {
            ReversibleAction lastAction = (ReversibleAction) undoStack.pop();
            redoStack.push(lastAction);
            handleReversibleAction(lastAction, false); //false == undo
        }
    }
    public void pushToUndoStack(String actionType, double startX, double startY, double endX, double endY) {
        ReversibleAction action = new ReversibleAction(actionType, startX, startY, endX, endY, captureSelection(startX, startY, endX, endY));
        undoStack.push(action);
        redoStack.clear();
    }
    public void redo() {
        if (!redoStack.isEmpty()) {
            ReversibleAction lastAction = (ReversibleAction) redoStack.pop();
            undoStack.push(lastAction);
            handleReversibleAction(lastAction, true); //true == redo
        }
    }
    private void handleReversibleAction(ReversibleAction action, boolean isRedo) {
        GraphicsContext gc = getGraphicsContext2D();
        if ("cut".equals(action.getActionType())) {
            if (isRedo) {
                clearSelectedArea(action.getStartX(), action.getStartY(), action.getEndX(), action.getEndY());
            } else {
                gc.drawImage(action.getCapturedImage(), action.getStartX(), action.getStartY());
            }
        } else if ("paste".equals(action.getActionType())) {
            if (isRedo) {
                gc.drawImage(action.getCapturedImage(), action.getStartX(), action.getStartY());
            } else {
                clearSelectedArea(action.getStartX(), action.getStartY(), action.getEndX(), action.getEndY());
            }
        }
        redraw();
    }
    public WritableImage captureSelection(double startX, double startY, double endX, double endY) {
        double x = Math.min(startX, endX);
        double y = Math.min(startY, endY);
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);
        if (width <= 0 || height <= 0) {
            System.out.println("Invalid selection.");
            return null;
        }
        WritableImage selection = new WritableImage((int) width, (int) height);
        SnapshotParameters params = new SnapshotParameters();
        params.setViewport(new javafx.geometry.Rectangle2D(x, y, width, height));
        this.snapshot(params, selection);
        return selection;
    }
    public void clearSelectedArea(double startX, double startY, double endX, double endY) {
        GraphicsContext gc = getGraphicsContext2D();
        double x = Math.min(startX, endX);
        double y = Math.min(startY, endY);
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);
        gc.clearRect(x, y, width, height);
        lines.removeIf(line -> isWithinSelection(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY(), x, y, width, height));
        shapes.removeIf(shape -> isWithinSelection(shape.getStartX(), shape.getStartY(), shape.getEndX(), shape.getEndY(), x, y, width, height));
        redraw();
    }
    public void pasteImage(WritableImage image, double x, double y) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.drawImage(image, x, y);
        pastedImages.add(new PasteAction(image, x, y));
        redraw();
    }
    public void setSelectionActive(boolean isActive) {
        this.selectionActive = isActive;
        redraw();
    }
    public boolean isSelectionActive() {
        return selectionActive;
    }
    private boolean isWithinSelection(double objStartX, double objStartY, double objEndX, double objEndY, double selX, double selY, double selWidth, double selHeight) {
        return objStartX >= selX && objEndX <= selX + selWidth &&
                objStartY >= selY && objEndY <= selY + selHeight;
    }
    public void setSelectionCoordinates(double startX, double startY, double endX, double endY) {
        this.selectionStartX = startX;
        this.selectionStartY = startY;
        this.selectionEndX = endX;
        this.selectionEndY = endY;
        redraw();
    }
    public void updateMousePosition(MouseEvent event) {
        lastMouseX = event.getX();
        lastMouseY = event.getY();
    }
}