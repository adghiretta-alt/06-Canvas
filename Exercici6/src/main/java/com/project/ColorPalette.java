package com.project;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class ColorPalette {

    private static final Color[] COLORS = { Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.PURPLE };
    private static final double RADIUS = 14;
    private static final double SPACING = 35;

    private final Canvas canvas;
    private final GraphicsContext gc;
    private Color selectedColor = COLORS[0];

    public ColorPalette(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();

        canvas.setOnMouseClicked(this::onMouseClicked);
        draw();
    }

    public Color getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(Color color) {
        this.selectedColor = color;
        draw();
    }

    private double getCenterX(int index) {
        return SPACING / 2 + index * SPACING + 10;
    }

    private double getCenterY(int index) {
        double baseCenterY = canvas.getHeight() / 2 + 5;
        // Si el color està seleccionat, es dibuixa més amunt verticalment
        if (COLORS[index].equals(selectedColor)) {
            return baseCenterY - 10;
        }
        return baseCenterY;
    }

    private void onMouseClicked(MouseEvent event) {
        for (int i = 0; i < COLORS.length; i++) {
            double dx = event.getX() - getCenterX(i);
            double dy = event.getY() - getCenterY(i);

            if (Math.sqrt(dx * dx + dy * dy) <= RADIUS) {
                selectedColor = COLORS[i];
                draw();
                return;
            }
        }
    }

    public void draw() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int i = 0; i < COLORS.length; i++) {
            double cx = getCenterX(i);
            double cy = getCenterY(i);

            gc.setFill(COLORS[i]);
            gc.fillOval(cx - RADIUS, cy - RADIUS, RADIUS * 2, RADIUS * 2);

            gc.setStroke(Color.GRAY);
            gc.setLineWidth(1);
            gc.strokeOval(cx - RADIUS, cy - RADIUS, RADIUS * 2, RADIUS * 2);

            if (COLORS[i].equals(selectedColor)) {
                double r = RADIUS + 3;
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(2);
                gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
            }
        }
    }
}