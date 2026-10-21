package com.project;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class ColorPalette {

    // Llista de colors principals
    private static final Color[] COLORS = {
        Color.BLACK, Color.RED, Color.ORANGE, Color.YELLOW, 
        Color.GREEN, Color.CYAN, Color.BLUE, Color.PURPLE
    };

    private static final double PENCIL_WIDTH = 16;
    private static final double BODY_HEIGHT = 45;
    private static final double TIP_HEIGHT = 15;
    private static final double SPACING = 20;

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

    private double getPencilX(int index) {
        return 5 + index * SPACING;
    }

    private double getPencilY(int index) {
        double baseY = canvas.getHeight() - BODY_HEIGHT - TIP_HEIGHT - 5;
        // Si és el color seleccionat, s'eleva cap amunt
        if (COLORS[index].equals(selectedColor)) {
            return baseY - 15;
        }
        return baseY;
    }

    private void onMouseClicked(MouseEvent event) {
        double clickX = event.getX();
        double clickY = event.getY();

        for (int i = 0; i < COLORS.length; i++) {
            double px = getPencilX(i);
            double py = getPencilY(i);
            double totalHeight = BODY_HEIGHT + TIP_HEIGHT;

            // Comprovar si el clic cau dins de l'àrea del llapis
            if (clickX >= px && clickX <= px + PENCIL_WIDTH &&
                clickY >= py && clickY <= py + totalHeight) {
                selectedColor = COLORS[i];
                draw();
                return;
            }
        }
    }

    public void draw() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        Color woodColor = Color.web("#d7a15c"); // Color fusta de la punta

        for (int i = 0; i < COLORS.length; i++) {
            double x = getPencilX(i);
            double y = getPencilY(i);
            Color color = COLORS[i];

            // 1. Punta de fusta (triangle)
            gc.setFill(woodColor);
            gc.fillPolygon(
                new double[]{ x, x + PENCIL_WIDTH / 2.0, x + PENCIL_WIDTH },
                new double[]{ y + TIP_HEIGHT, y, y + TIP_HEIGHT },
                3
            );

            // 2. Mina del llapis (triangle petit superior)
            gc.setFill(color);
            gc.fillPolygon(
                new double[]{ x + 4, x + PENCIL_WIDTH / 2.0, x + PENCIL_WIDTH - 4 },
                new double[]{ y + TIP_HEIGHT - 4, y, y + TIP_HEIGHT - 4 },
                3
            );

            // 3. Cos del llapis (rectangle de color)
            gc.setFill(color);
            gc.fillRect(x, y + TIP_HEIGHT, PENCIL_WIDTH, BODY_HEIGHT);

            // Contorn general per definir el llapis
            gc.setStroke(Color.DARKGRAY);
            gc.setLineWidth(1);
            gc.strokeRect(x, y + TIP_HEIGHT, PENCIL_WIDTH, BODY_HEIGHT);
            gc.strokePolygon(
                new double[]{ x, x + PENCIL_WIDTH / 2.0, x + PENCIL_WIDTH },
                new double[]{ y + TIP_HEIGHT, y, y + TIP_HEIGHT },
                3
            );
        }
    }
}