package com.project;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Stroke {

    public enum Type { FREEHAND, LINE, RECTANGLE }

    private final Type type;
    private final String name;
    private final List<Point2D> points = new ArrayList<>();
    private final Color strokeColor;
    private final Color fillColor;
    private final double width;

    public Stroke(Type type, String name, Color strokeColor, Color fillColor, double width) {
        this.type = type;
        this.name = name;
        this.strokeColor = strokeColor;
        this.fillColor = fillColor;
        this.width = width;
    }

    public void addPoint(double x, double y) {
        points.add(new Point2D(x, y));
    }

    public void setLastPoint(double x, double y) {
        if (!points.isEmpty()) {
            points.set(points.size() - 1, new Point2D(x, y));
        }
    }

    public void draw(GraphicsContext gc) {
        if (points.isEmpty()) return;

        gc.setLineWidth(width);

        if (type == Type.FREEHAND) {
            gc.setStroke(strokeColor);
            gc.beginPath();
            gc.moveTo(points.get(0).getX(), points.get(0).getY());
            for (Point2D p : points) {
                gc.lineTo(p.getX(), p.getY());
            }
            gc.stroke();
        } else if (type == Type.LINE && points.size() >= 2) {
            gc.setStroke(strokeColor);
            Point2D p1 = points.get(0);
            Point2D p2 = points.get(1);
            gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        } else if (type == Type.RECTANGLE && points.size() >= 2) {
            Point2D p1 = points.get(0);
            Point2D p2 = points.get(1);

            double x = Math.min(p1.getX(), p2.getX());
            double y = Math.min(p1.getY(), p2.getY());
            double w = Math.abs(p2.getX() - p1.getX());
            double h = Math.abs(p2.getY() - p1.getY());

            if (fillColor != null && !fillColor.equals(Color.TRANSPARENT)) {
                gc.setFill(fillColor);
                gc.fillRect(x, y, w, h);
            }
            if (strokeColor != null) {
                gc.setStroke(strokeColor);
                gc.strokeRect(x, y, w, h);
            }
        }
    }

    // Calcula el requadre delimitador incloent el marge de l'ample de línia
    public Bounds getBoundingBox() {
        if (points.isEmpty()) return new BoundingBox(0, 0, 0, 0);

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (Point2D p : points) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
        }

        double margin = width / 2.0 + 4;
        return new BoundingBox(
            minX - margin,
            minY - margin,
            (maxX - minX) + margin * 2,
            (maxY - minY) + margin * 2
        );
    }

    // Trasllada tots els punts de l'objecte sense deformar-lo
    public void translate(double deltaX, double deltaY) {
        for (int i = 0; i < points.size(); i++) {
            Point2D p = points.get(i);
            points.set(i, new Point2D(p.getX() + deltaX, p.getY() + deltaY));
        }
    }

    @Override
    public String toString() {
        return name;
    }
}