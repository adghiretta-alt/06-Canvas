package com.project;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class Controller implements Initializable {

    @FXML private Canvas canvas;
    @FXML private Canvas colorsCanvas;

    @FXML private RadioButton freehandRadio;
    @FXML private RadioButton lineRadio;
    @FXML private RadioButton rectRadio;
    @FXML private ToggleGroup toolGroup;

    @FXML private ColorPicker strokeColorPicker;
    @FXML private ColorPicker fillColorPicker;
    @FXML private Spinner<Double> widthSpinner;

    @FXML private ListView<Stroke> objectsListView;
    @FXML private Button deleteButton;

    private GraphicsContext gc;
    private ColorPalette colorPalette;

    private final ObservableList<Stroke> strokes = FXCollections.observableArrayList();
    private Stroke currentStroke = null;

    private Point2D lastMousePos = null;
    private boolean isDraggingObject = false;

    private int freehandCount = 1;
    private int lineCount = 1;
    private int rectCount = 1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        gc = canvas.getGraphicsContext2D();
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        if (widthSpinner != null) {
            widthSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 50.0, 2.0, 1.0));
        }

        if (objectsListView != null) {
            objectsListView.setItems(strokes);
            objectsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (deleteButton != null) deleteButton.setDisable(newVal == null);
                redraw();
            });
        }

        if (toolGroup != null) {
            toolGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (objectsListView != null) objectsListView.getSelectionModel().clearSelection();
                redraw();
            });
        }

        colorPalette = new ColorPalette(colorsCanvas);

        if (strokeColorPicker != null) {
            strokeColorPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                colorPalette.setSelectedColor(newVal);
            });
        }

        canvas.setOnMousePressed(this::onMousePressed);
        canvas.setOnMouseDragged(this::onMouseDragged);
        canvas.setOnMouseReleased(this::onMouseReleased);

        redraw();
    }

    private double getSelectedWidth() {
        return (widthSpinner != null && widthSpinner.getValue() != null) ? widthSpinner.getValue() : 2.0;
    }

    private Color getStrokeColor() {
        return (strokeColorPicker != null && strokeColorPicker.getValue() != null) 
            ? strokeColorPicker.getValue() : colorPalette.getSelectedColor();
    }

    private Color getFillColor() {
        return (fillColorPicker != null) ? fillColorPicker.getValue() : Color.TRANSPARENT;
    }

    private void onMousePressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) return;

        Point2D mousePos = new Point2D(event.getX(), event.getY());
        Stroke selectedStroke = (objectsListView != null) ? objectsListView.getSelectionModel().getSelectedItem() : null;

        // Mode Moure: si cliquem dins del requadre delimitador de l'objecte seleccionat
        if (selectedStroke != null) {
            Bounds bounds = selectedStroke.getBoundingBox();
            if (bounds.contains(mousePos)) {
                isDraggingObject = true;
                lastMousePos = mousePos;
                return;
            }
        }

        // Mode Dibuix: desmarquem la selecció actual i comencem un objecte nou
        isDraggingObject = false;
        if (objectsListView != null) objectsListView.getSelectionModel().clearSelection();

        if (lineRadio != null && lineRadio.isSelected()) {
            currentStroke = new Stroke(Stroke.Type.LINE, "Línia " + (lineCount++), getStrokeColor(), null, getSelectedWidth());
            currentStroke.addPoint(mousePos.getX(), mousePos.getY());
            currentStroke.addPoint(mousePos.getX(), mousePos.getY());
        } else if (rectRadio != null && rectRadio.isSelected()) {
            currentStroke = new Stroke(Stroke.Type.RECTANGLE, "Rectangle " + (rectCount++), getStrokeColor(), getFillColor(), getSelectedWidth());
            currentStroke.addPoint(mousePos.getX(), mousePos.getY());
            currentStroke.addPoint(mousePos.getX(), mousePos.getY());
        } else {
            currentStroke = new Stroke(Stroke.Type.FREEHAND, "Traç lliure " + (freehandCount++), getStrokeColor(), null, getSelectedWidth());
            currentStroke.addPoint(mousePos.getX(), mousePos.getY());
        }

        redraw();
    }

    private void onMouseDragged(MouseEvent event) {
        Point2D mousePos = new Point2D(event.getX(), event.getY());

        if (isDraggingObject) {
            Stroke selectedStroke = objectsListView.getSelectionModel().getSelectedItem();
            if (selectedStroke != null && lastMousePos != null) {
                double deltaX = mousePos.getX() - lastMousePos.getX();
                double deltaY = mousePos.getY() - lastMousePos.getY();
                selectedStroke.translate(deltaX, deltaY);
                lastMousePos = mousePos;
                redraw();
            }
            return;
        }

        if (currentStroke == null) return;

        if ((lineRadio != null && lineRadio.isSelected()) || (rectRadio != null && rectRadio.isSelected())) {
            currentStroke.setLastPoint(mousePos.getX(), mousePos.getY());
        } else {
            currentStroke.addPoint(mousePos.getX(), mousePos.getY());
        }

        redraw();
    }

    private void onMouseReleased(MouseEvent event) {
        if (isDraggingObject) {
            isDraggingObject = false;
            lastMousePos = null;
            return;
        }

        if (currentStroke == null) return;

        strokes.add(currentStroke);
        currentStroke = null;
        redraw();
    }

    @FXML
    private void onDeleteSelected() {
        if (objectsListView == null) return;
        Stroke selected = objectsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            strokes.remove(selected);
            objectsListView.getSelectionModel().clearSelection();
            redraw();
        }
    }

    private void redraw() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Dibuixa els objectes en ordre de creació
        for (Stroke s : strokes) {
            s.draw(gc);
        }

        // Previsualització de l'objecte que s'està dibuixant
        if (currentStroke != null) {
            currentStroke.draw(gc);
        }

        // Requadre discontinu de selecció
        if (objectsListView != null) {
            Stroke selected = objectsListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Bounds b = selected.getBoundingBox();
                gc.save();
                gc.setStroke(Color.BLUE);
                gc.setLineWidth(1.5);
                gc.setLineDashes(6.0, 4.0);
                gc.strokeRect(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
                gc.restore();
            }
        }
    }
}