package com.example.paint24;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import javafx.geometry.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaintController {
    @FXML
    private File currentFile;
    @FXML
    private Slider thicknessSlider;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Button cursorButton, selectButton, eraserButton, textButton, lineButton, freehandButton, rectangleButton, squareButton, circleButton, ellipseButton, triangleButton, rightTriangleButton, starButton, ngonButton, newTabButton, eyedropperButton, clearButton;
    @FXML
    private TabPane tabPane;
    @FXML
    private Label thicknessLabel;
    @FXML
    private RadioButton solidToggle;
    @FXML
    private Spinner ngonSidesInput;
    private ScrollPane scrollPane;
    private boolean saved = false;
    private boolean drawingMode = false;
    private boolean freehandMode = false;
    private boolean eyedropperMode = false;
    private boolean eraserMode = false;
    private boolean dashedLineMode = false;
    private boolean straightMode = true;
    private boolean shapeMode = false;
    private boolean textMode = false;
    private boolean selectionMode = false;
    private WritableImage copyBuffer = null;
    private double selectionStartX, selectionStartY, selectionEndX, selectionEndY;
    private static final double MIN_SELECTION_SIZE = 1;
    private boolean isDraggingSelection = false;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;
    private boolean selectionActive = false;
    private List<Point2D> freehandPoints;
    private double lineThickness = 5;
    private Color lineColor = Color.BLACK;
    private Shape.ShapeType currentShapeType = null;

    @FXML
    public void initialize() {
        //Sets cursor mode as the default mode when program is opened
        enableCursorMode();
        enableSolidLineMode();
        solidToggle.setSelected(true);
        //Sets up the thickness slider for lines
        thicknessSlider.valueProperty().addListener((obs, oldVal, newVal) -> lineThickness = newVal.doubleValue());
        //Sets up label for line/shape width (in pixels)
        thicknessLabel.setText(String.format("%.0f px", thicknessSlider.getValue()));
        thicknessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            lineThickness = newVal.doubleValue();
            thicknessLabel.setText(String.format("%.0f px", lineThickness));
        });
        //Sets up the color picker to affect the line color
        colorPicker.setValue(Color.BLACK); //Sets default color to black
        colorPicker.setOnAction(e -> lineColor = colorPicker.getValue());
        //Sets up the spinner for sides of an N-gon
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 999, 3);
        ngonSidesInput.setValueFactory(valueFactory);
        //Assign icons to buttons
        Font fontAwesome = Font.loadFont(getClass().getResourceAsStream("/fonts/fa-solid-900.ttf"), 16);
        Font materialIcons = Font.loadFont(getClass().getResourceAsStream("/fonts/MaterialIcons-Regular.ttf"), 16);
        cursorButton.setFont(fontAwesome);
        cursorButton.setText("\uf245"); //Cursor button icon (cursor)
        selectButton.setFont(materialIcons);
        selectButton.setText("\uef52"); //Select button icon (dashed square)
        eraserButton.setFont(fontAwesome);
        eraserButton.setText("\uf12d"); //Eraser button icon (eraser)
        textButton.setFont(materialIcons);
        textButton.setText("\ue262"); //Text button icon (Uppercase and lowercase "T")
        lineButton.setFont(fontAwesome);
        lineButton.setText("\uf040"); //Line button icon (pencil)
        freehandButton.setFont(fontAwesome);
        freehandButton.setText("\uf1fc"); // Freehand line button icon (paintbrush)
        rectangleButton.setFont(materialIcons);
        rectangleButton.setText("\ueb54"); //Rectangle button icon (rectangle)
        squareButton.setFont(materialIcons);
        squareButton.setText("\ueb36"); //Square button icon (square)
        circleButton.setFont(materialIcons);
        circleButton.setText("\uef4a"); //Circle button icon (circle)
        ellipseButton.setFont(fontAwesome);
        ellipseButton.setText("\uf7fb"); //Ellipse button icon (egg, I couldn't find an ellipse)
        triangleButton.setFont(materialIcons);
        triangleButton.setText("\ue86b"); //Triangle button icon (triangle)
        rightTriangleButton.setFont(materialIcons);
        rightTriangleButton.setText("\ue037"); //Right triangle button icon (it's not a right triangle... but I couldn't find one)
        starButton.setFont(materialIcons);
        starButton.setText("\ue838"); //Star button icon (star)
        ngonButton.setFont(materialIcons);
        ngonButton.setText("\ue574"); //Ngon button icon (collection of shapes)
        eyedropperButton.setFont(materialIcons);
        eyedropperButton.setText("\ue3b8"); //Eyedropper button icon (syringe)
        clearButton.setFont(materialIcons);
        clearButton.setText("\ue872"); //Clear canvas button icon (trash can)
        newTabButton.setFont(fontAwesome);
        newTabButton.setText("\uf067"); //New tab button icon (plus)
        //Sets up keyboard shortcuts
        tabPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupKeyboardShortcuts(newScene);
            }
        });
        createNewTab();
    }
    @FXML
    private void createNewTab() {
        Tab newTab = new Tab("Untitled");
        Pane canvasParent = new Pane();
        scrollPane = new ScrollPane(canvasParent);
        scrollPane.setPannable(true);
        newTab.setContent(scrollPane);
        ResizableCanvas newCanvas = new ResizableCanvas(800, 600);
        canvasParent.getChildren().add(newCanvas);
        newCanvas.setOnMousePressed(event -> onMousePressed(event, newCanvas));
        newCanvas.setOnMouseDragged(event -> onMouseDragged(event, newCanvas));
        newCanvas.setOnMouseReleased(event -> onMouseReleased(event, newCanvas));
        newTab.setOnCloseRequest(event -> {
            event.consume();
            handleCloseRequest(newTab);
        });
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
        saved = false;
    }
    @FXML
    private void clearCanvas() {
        ResizableCanvas currentCanvas = getCurrentCanvas();
        if (currentCanvas != null) {
            GraphicsContext gc = currentCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, currentCanvas.getWidth(), currentCanvas.getHeight());
            currentCanvas.clearEdits();
            saved = false;
        }
    }
    //File menu
    @FXML
    private void open() { //Opens file
        ResizableCanvas currentCanvas = getCurrentCanvas();
        if (currentCanvas == null) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );
        currentFile = fileChooser.showOpenDialog(null);
        if (currentFile != null) {
            try {
                Image image = new Image(currentFile.toURI().toString());
                GraphicsContext gc = currentCanvas.getGraphicsContext2D();
                gc.drawImage(image, 0, 0);
                currentCanvas.setImage(image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        saved = false;
    }
    @FXML
    private void save() { //Saves changes to current file
        ResizableCanvas currentCanvas = getCurrentCanvas();
        if (currentCanvas == null) return;
        if (saved) {
            write(currentFile, currentCanvas);
        } else {
            saveAs();
        }
    }
    @FXML
    private void saveAs() { //Saves file to disk
        ResizableCanvas currentCanvas = getCurrentCanvas();
        if (currentCanvas == null) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Bitmap", "*.bmp")
        );
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            write(file, currentCanvas);
            saved = true;
        }
    }
    private void write(File file, ResizableCanvas canvas) { //Writes the file to disk during Save and SaveAs methods
        try {
            WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, writableImage);
            ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //Edit menu
    @FXML
    private void resizeCanvas() {
        ResizableCanvas currentCanvas = getCurrentCanvas();
        if (currentCanvas == null) return;
        // Dialog to collect width and height input from the user
        TextInputDialog widthDialog = new TextInputDialog();
        widthDialog.setTitle("Resize Canvas");
        widthDialog.setHeaderText("Enter the new canvas width:");
        widthDialog.setContentText("Width:");
        Optional<String> widthResult = widthDialog.showAndWait();
        if (widthResult.isEmpty()) return;
        TextInputDialog heightDialog = new TextInputDialog();
        heightDialog.setTitle("Resize Canvas");
        heightDialog.setHeaderText("Enter the new canvas height:");
        heightDialog.setContentText("Height:");
        Optional<String> heightResult = heightDialog.showAndWait();
        if (heightResult.isEmpty()) return;
        try {
            int newWidth = Integer.parseInt(widthResult.get());
            int newHeight = Integer.parseInt(heightResult.get());
            currentCanvas.setWidth(newWidth);
            currentCanvas.setHeight(newHeight);
            currentCanvas.redraw();
        } catch (NumberFormatException e) {
            System.out.println("Invalid input, please enter valid positive numbers for width and height.");
        }
        saved = false;
    }
    private ResizableCanvas getCurrentCanvas() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof ScrollPane) {
            scrollPane = (ScrollPane) selectedTab.getContent();
            if (scrollPane.getContent() instanceof Pane) {
                Pane canvasParent = (Pane) scrollPane.getContent();
                if (!canvasParent.getChildren().isEmpty() && canvasParent.getChildren().get(0) instanceof ResizableCanvas) {
                    return (ResizableCanvas) canvasParent.getChildren().get(0);
                }
            }
        }
        return null;
    }
    //Help menu
    @FXML
    private void about() { //'About' page in the help menu
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "This is version 1.1.0 of Ethan Preston's Paint project for CS 250.\nLast updated on August 30th.", ButtonType.CLOSE);
        alert.setTitle("Paint - About");
        alert.setHeaderText("About");
        alert.showAndWait();
    }
    @FXML
    private void report() { //'Report an Issue' page in the help menu
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "If you need to report an issue with the program,\nplease email ethan.preston@valpo.edu with your concern.", ButtonType.OK);
        alert.setTitle("Paint - Report an Issue");
        alert.setHeaderText("Report an Issue");
        alert.showAndWait();
    }
    //Toolbar menu
    @FXML
    private void enableCursorMode() { //Cursor mode
        drawingMode = false;
        freehandMode = false;
        eyedropperMode = false;
        eraserMode = false;
        dashedLineMode = false;
        straightMode = false;
        shapeMode = false;
        currentShapeType = null;
        textMode = false;
        selectionMode = false;
    }
    @FXML
    private void enableSelectionMode() { //Selection mode
        drawingMode = false;
        freehandMode = false;
        eyedropperMode = false;
        eraserMode = false;
        dashedLineMode = false;
        straightMode = false;
        shapeMode = false;
        currentShapeType = null;
        textMode = false;
        selectionMode = true;
    }
    @FXML
    private void copySelection() {
        ResizableCanvas currentCanvas = getCurrentCanvas();
        if (currentCanvas != null && selectionStartX != selectionEndX && selectionStartY != selectionEndY) {
            copyBuffer = currentCanvas.captureSelection(selectionStartX, selectionStartY, selectionEndX, selectionEndY);
            System.out.println("Selection copied.");
        } else {
            System.out.println("No valid selection to copy.");
        }
    }
    @FXML
    private void cutSelection() {
        ResizableCanvas currentCanvas = getCurrentCanvas();
        if (currentCanvas != null) {
            if (copyBuffer != null){
                currentCanvas.clearSelectedArea(selectionStartX, selectionStartY, selectionEndX, selectionEndY);
                currentCanvas.setSelectionActive(false);
            }
            currentCanvas.pushToUndoStack("cut", selectionStartX, selectionStartY, selectionEndX, selectionEndY);
            System.out.println("Selection cut.");
        } else {
            System.out.println("Cut failed.");
        }
    }
    @FXML
    private void pasteSelection() {
        ResizableCanvas currentCanvas = getCurrentCanvas();
        if (currentCanvas != null && copyBuffer != null) {
            double pasteX = currentCanvas.getLastMouseX();
            double pasteY = currentCanvas.getLastMouseY();
            currentCanvas.pasteImage(copyBuffer, pasteX, pasteY);
            currentCanvas.addPastedImage(copyBuffer, pasteX, pasteY);
            currentCanvas.pushToUndoStack("paste", pasteX, pasteY, pasteX + copyBuffer.getWidth(), pasteY + copyBuffer.getHeight());
            System.out.println("Selection pasted at: (" + pasteX + ", " + pasteY + ")");
            copyBuffer = null;
            saved = false;
        } else { System.out.println("No selection available to paste."); }
    }
    @FXML
    private void deselect() {
        selectionMode = false;
        copyBuffer = null;
        ResizableCanvas currentCanvas = getCurrentCanvas();
        if (currentCanvas != null) {
            currentCanvas.setSelectionActive(false);
            currentCanvas.redraw();
        }
    }
    private boolean isWithinSelection(double x, double y) {
        return x >= selectionStartX && x <= selectionEndX && y >= selectionStartY && y <= selectionEndY;
    }

    @FXML
    private void enableTextMode() { //Text mode
        drawingMode = false;
        freehandMode = false;
        eyedropperMode = false;
        eraserMode = false;
        dashedLineMode = false;
        straightMode = false;
        shapeMode = false;
        currentShapeType = null;
        textMode = true;
        selectionMode = false;
    }
    private void createTextField(MouseEvent event, ResizableCanvas canvas) {
        TextField textField = new TextField();
        Pane canvasParent = (Pane) canvas.getParent();
        textField.setLayoutX(event.getX());
        textField.setLayoutY(event.getY());
        canvasParent.getChildren().add(textField);
        textField.setOnAction(e -> {
            String inputText = textField.getText();
            if (!inputText.isEmpty()) {
                PaintText text = new PaintText(event.getX(), event.getY(), inputText, 20, colorPicker.getValue());
                canvas.addText(text);
            }
            canvasParent.getChildren().remove(textField);
        });
        textField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                canvasParent.getChildren().remove(textField); //Cancel text input
            }
        });
    }
    @FXML
    private void enableEyedropperMode() {
        drawingMode = false;
        freehandMode = false;
        eyedropperMode = true;
        eraserMode = false;
        dashedLineMode = false;
        straightMode = false;
        shapeMode = false;
        currentShapeType = null;
        textMode = false;
        selectionMode = false;
    }
    private void pickColorFromCanvas(MouseEvent event, ResizableCanvas canvas) {
        WritableImage snapshot = canvas.snapshot(null, null);
        PixelReader pixelReader = snapshot.getPixelReader();
        int x = (int) event.getX();
        int y = (int) event.getY();
        Color color = pixelReader.getColor(x, y);
        colorPicker.setValue(color);
        lineColor = color;
        eyedropperMode = false;
        saved = false;
    }
    @FXML
    private void enableEraserMode() {
        drawingMode = true;
        freehandMode = true;
        eyedropperMode = false;
        eraserMode = true;
        dashedLineMode = false;
        straightMode = false;
        shapeMode = false;
        currentShapeType = null;
        textMode = false;
        selectionMode = false;
    }
    @FXML
    private void enableLineMode() { //Straight line mode
        drawingMode = true;
        freehandMode = false;
        eyedropperMode = false;
        eraserMode = false;
        dashedLineMode = false;
        straightMode = true;
        shapeMode = false;
        currentShapeType = null;
        textMode = false;
        selectionMode = false;
    }
    @FXML
    private void enableFreehandMode() { //Freehand line mode
        drawingMode = true;
        freehandMode = true;
        eyedropperMode = false;
        eraserMode = false;
        dashedLineMode = false;
        straightMode = false;
        shapeMode = false;
        currentShapeType = null;
        textMode = false;
        selectionMode = false;
    }
    @FXML
    private void enableSolidLineMode() { //Solid line mode
        if (drawingMode){
            dashedLineMode = false;
        }
    }
    @FXML
    private void enableDashedLineMode() { //Dashed line mode
        if (drawingMode) {
            dashedLineMode = true;
        }
    }
    @FXML
    private void enableShapeMode(Shape.ShapeType shapeType) { //Shape mode
        drawingMode = true;
        freehandMode = false;
        eyedropperMode = false;
        eraserMode = false;
        dashedLineMode = false;
        straightMode = false;
        shapeMode = true;
        currentShapeType = shapeType;
        textMode = false;
        selectionMode = false;
    }
    @FXML
    private void enableShapeModeRectangle() {
        enableShapeMode(Shape.ShapeType.RECTANGLE);
    }
    @FXML
    private void enableShapeModeSquare() {
        enableShapeMode(Shape.ShapeType.SQUARE);
    }
    @FXML
    private void enableShapeModeCircle() {
        enableShapeMode(Shape.ShapeType.CIRCLE);
    }
    @FXML
    private void enableShapeModeEllipse() {
        enableShapeMode(Shape.ShapeType.ELLIPSE);
    }
    @FXML
    private void enableShapeModeTriangle() {
        enableShapeMode(Shape.ShapeType.TRIANGLE);
    }
    @FXML
    private void enableShapeModeRightTriangle() {
        enableShapeMode(Shape.ShapeType.RIGHT_TRIANGLE);
    }
    @FXML
    private void enableShapeModeStar() {
        enableShapeMode(Shape.ShapeType.STAR);
    }
    @FXML
    private void enableNgonMode() {
        enableShapeMode(Shape.ShapeType.NGON);
    }
    @FXML
    private void undo() {
        ResizableCanvas currentCanvas = getCurrentCanvas();
        if (currentCanvas != null) {
            currentCanvas.undo();
        }
    }
    @FXML
    private void redo() {
        ResizableCanvas currentCanvas = getCurrentCanvas();
        if (currentCanvas != null) {
            currentCanvas.redo();
        }
    }
    private void onMousePressed(MouseEvent event, ResizableCanvas canvas) {
        canvas.updateMousePosition(event);
        if (selectionMode) {
            if (isWithinSelection(event.getX(), event.getY())) {
                isDraggingSelection = true;
                dragOffsetX = event.getX() - selectionStartX;
                dragOffsetY = event.getY() - selectionStartY;
                canvas.setCursor(Cursor.HAND);
                return;
            }
            selectionStartX = event.getX();
            selectionStartY = event.getY();
            isDraggingSelection = false;
        }
        if (eyedropperMode) {
            pickColorFromCanvas(event, canvas);
        }
        if (textMode) {
            createTextField(event, canvas);
            return;
        }
        if (drawingMode) {
            double x = event.getX();
            double y = event.getY();
            scrollPane.setPannable(false);
            if (freehandMode) {
                freehandPoints = new ArrayList<>();
                freehandPoints.add(new Point2D(x, y));
            } else {
                canvas.startX = x;
                canvas.startY = y;
            }
        }
    }
    private void onMouseDragged(MouseEvent event, ResizableCanvas canvas) {
        if (selectionMode) {
            selectionEndX = event.getX();
            selectionEndY = event.getY();
            canvas.redraw();
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setStroke(Color.BLACK);
            gc.setLineDashes(5);
            gc.setLineWidth(1);
            double x = Math.min(selectionStartX, selectionEndX);
            double y = Math.min(selectionStartY, selectionEndY);
            double width = Math.abs(selectionEndX - selectionStartX);
            double height = Math.abs(selectionEndY - selectionStartY);
            gc.strokeRect(x, y, width, height);
            return;
        }
        if (drawingMode) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setLineDashes(dashedLineMode ? new double[]{10, 5} : null);
            if (eraserMode) {
                gc.setStroke(Color.WHITE);
            } else {
                gc.setStroke(lineColor);
            }
            gc.setLineWidth(lineThickness);
            double x = event.getX();
            double y = event.getY();
            canvas.redraw();
            if (freehandMode) {
                // Freehand drawing
                freehandPoints.add(new Point2D(x, y));
                for (int i = 0; i < freehandPoints.size() - 1; i++) {
                    Point2D p1 = freehandPoints.get(i);
                    Point2D p2 = freehandPoints.get(i + 1);
                    gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                }
            } else if (shapeMode && currentShapeType != null) {
                // Drawing shapes
                int numSides = 0;
                if (currentShapeType == Shape.ShapeType.NGON) {
                    numSides = (int) ngonSidesInput.getValue();
                }
                Shape shape = new Shape(canvas.startX, canvas.startY, x, y, lineThickness, lineColor, currentShapeType, dashedLineMode, numSides);
                shape.draw(gc);
            } else if (straightMode) {
                // Drawing straight lines
                gc.strokeLine(canvas.startX, canvas.startY, x, y);
            }
        }
    }
    private void onMouseReleased(MouseEvent event, ResizableCanvas canvas) {
        if (isDraggingSelection && selectionMode) {
            isDraggingSelection = false;
            canvas.setCursor(Cursor.DEFAULT);
            canvas.setSelectionCoordinates(selectionStartX, selectionStartY, selectionEndX, selectionEndY);
            canvas.redraw();
            canvas.pasteImage(copyBuffer, selectionStartX, selectionStartY);
            canvas.pushToUndoStack("move", selectionStartX, selectionStartY, selectionEndX, selectionEndY);
            return;
        }
        if (selectionMode) {
            selectionEndX = event.getX();
            selectionEndY = event.getY();
            canvas.setSelectionCoordinates(selectionStartX, selectionStartY, selectionEndX, selectionEndY);
            copyBuffer = canvas.captureSelection(selectionStartX, selectionStartY, selectionEndX, selectionEndY);
            canvas.setSelectionActive(true);
            return;
        }
        if (drawingMode) {
            double endX = event.getX();
            double endY = event.getY();
            scrollPane.setPannable(true);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setLineDashes(0);
            if (freehandMode) {
                Line freehandLine = new Line(freehandPoints, lineThickness, eraserMode ? Color.WHITE : lineColor, dashedLineMode);
                canvas.addLine(freehandLine);
            } else if (shapeMode && currentShapeType != null) {
                int numSides = 0;
                if (currentShapeType == Shape.ShapeType.NGON) {
                    numSides = (int) ngonSidesInput.getValue();
                }
                Shape shape = new Shape(canvas.startX, canvas.startY, endX, endY, lineThickness, lineColor, currentShapeType, dashedLineMode, numSides);
                canvas.addShape(shape);
            } else if (straightMode) {
                Line line = new Line(canvas.startX, canvas.startY, endX, endY, lineThickness, lineColor, dashedLineMode);
                canvas.addLine(line);
            }
        }
        saved = false;
    }
    private void onMouseMoved(MouseEvent event, ResizableCanvas canvas) {
        if (selectionMode && isWithinSelection(event.getX(), event.getY())) {
            canvas.setCursor(Cursor.HAND);
        } else {
            canvas.setCursor(Cursor.DEFAULT);
        }
    }

    private void handleCloseRequest(Tab tab) {
        if (!saved) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unsaved Changes");
            alert.setHeaderText("You have unsaved changes in this tab.");
            alert.setContentText("Do you want to save your changes before closing this tab?");
            ButtonType saveButton = new ButtonType("Save");
            ButtonType deleteButton = new ButtonType("Delete");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(saveButton, deleteButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == saveButton) {
                save();
                tabPane.getTabs().remove(tab);
            } else if (result.isPresent() && result.get() == deleteButton) {
                tabPane.getTabs().remove(tab);
            } else {
                alert.close();
            }
        } else {
            tabPane.getTabs().remove(tab);
        }
    }

    private void setupKeyboardShortcuts (Scene scene) {
        scene.getAccelerators().put( //Save keyboard shortcut "Shortcut+S"
                new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN),
                this::save
        );
        scene.getAccelerators().put( //Open keyboard shortcut "Shortcut+O"
                new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN),
                this::open
        );
        scene.getAccelerators().put( //Undo keyboard shortcut "Shortcut+Z"
                new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN),
                this::undo
        );
        scene.getAccelerators().put( //Redo keyboard shortcut "Shortcut+Y"
                new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN),
                this::redo
        );
        scene.getAccelerators().put( //Pencil (straight line) keyboard shortcut "Shortcut+P"
                new KeyCodeCombination(KeyCode.P, KeyCodeCombination.SHORTCUT_DOWN),
                this::enableLineMode
        );
        scene.getAccelerators().put( //Draw (freehand line) keyboard shortcut "Shortcut+D"
                new KeyCodeCombination(KeyCode.D, KeyCodeCombination.SHORTCUT_DOWN),
                this::enableFreehandMode
        );
        scene.getAccelerators().put( //New tab keyboard shortcut "Shortcut+N"
                new KeyCodeCombination(KeyCode.N, KeyCodeCombination.SHORTCUT_DOWN),
                this::createNewTab
        );
        scene.getAccelerators().put( //Copy keyboard shortcut "Shortcut+C"
                new KeyCodeCombination(KeyCode.C, KeyCodeCombination.SHORTCUT_DOWN),
                this::copySelection
        );
        scene.getAccelerators().put( //Cut keyboard shortcut "Shortcut+X"
                new KeyCodeCombination(KeyCode.X, KeyCodeCombination.SHORTCUT_DOWN),
                this::cutSelection
        );
        scene.getAccelerators().put( //Paste keyboard shortcut "Shortcut+V"
                new KeyCodeCombination(KeyCode.V, KeyCodeCombination.SHORTCUT_DOWN),
                this::pasteSelection
        );
        scene.getAccelerators().put( //Deselect keyboard shortcut "Shortcut+D"
                new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN),
                this::deselect
        );
    }
}
