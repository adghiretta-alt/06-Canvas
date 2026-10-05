package com.project;

import java.net.URL;
import java.util.Calendar;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class CnvController {

    private double borderSize = 5;

    private String gameStatus = "playing";

    private int playerPoints = 0;
    private double playerX = Double.POSITIVE_INFINITY;
    private double playerY = Double.POSITIVE_INFINITY;
    private final double playerWidth = 200;
    private final double playerHalf = playerWidth / 2;
    private final double playerHeight = 5;
    private double playerSpeed = 250;
    private final double playerSpeedIncrement = 15;
    public String playerDirection = "none";

    private double ballX = Double.POSITIVE_INFINITY;
    private double ballY = Double.POSITIVE_INFINITY;
    private final double ballSize = 15;
    private final double ballHalf = ballSize / 2;
    private double ballSpeed = 100;
    private final double ballSpeedIncrement = 25;
    private String ballDirection = "upRight";

    public Canvas canvas;
    public GraphicsContext gc;
    public CnvTimer animationTimer;
    public boolean showFps = false; 

    public CnvController(Canvas canvas) {

        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();

        animationTimer = new CnvTimer(this::run, this::draw, 0);
        start();

        // Set initial positions
        ballX = canvas.getWidth() / 2;
        ballY = canvas.getHeight() / 2;
        playerX = canvas.getWidth() / 2;
    }

    // Start animation timer
    public void start() {
        animationTimer.start();
    }

    // Stop animation timer
    public void stop() {
        animationTimer.stop();
    }

    // Run game (and animations)
    private void run(double fps) {

        if (animationTimer.fps < 1) { return; }

        // Compute global attributes
        final double boardWidth = canvas.getWidth();
        final double boardHeight = canvas.getHeight();

        // Move player
        switch (playerDirection) {
            case "right":
                playerX = playerX + playerSpeed / fps; 
                break;
            case "left":
                playerX = playerX - playerSpeed / fps;
                break;
        }

        // Keep player in bounds
        final double playerMinX = playerHalf;
        final double playerMaxX = boardWidth - playerHalf;

        if (playerX < playerMinX) {

            playerX = playerMinX;

        } else if (playerX > playerMaxX) {

            playerX = playerMaxX;
        }

        // Move ball
        double ballNextX = ballX;
        double ballNextY = ballY;
        switch (ballDirection) {
            case "upRight": 
                ballNextX = ballX + ballSpeed / fps;
                ballNextY = ballY - ballSpeed / fps;
                break;
            case "upLeft": 
                ballNextX = ballX - ballSpeed / fps;
                ballNextY = ballY - ballSpeed / fps;
                break;
            case "downRight": 
                ballNextX = ballX + ballSpeed / fps;
                ballNextY = ballY + ballSpeed / fps;
                break;
            case "downLeft": 
                ballNextX = ballX - ballSpeed / fps;
                ballNextY = ballY + ballSpeed / fps;
                break;
        }

        // Check ball collision with board sides
        final double[][] lineBall = { {ballX, ballY}, {ballNextX, ballNextY} };

        final double[][] lineBoardLeft = { {borderSize, 0}, {borderSize, boardHeight} };
        final double[] intersectionLeft = findIntersection(lineBall, lineBoardLeft);

        final double boardMaxX = boardWidth - borderSize;
        final double[][] lineBoardRight = { {boardMaxX, 0}, {boardMaxX, boardHeight} };
        final double[] intersectionRight = findIntersection(lineBall, lineBoardRight);

        final double[][] lineBoardTop = { {0, borderSize}, {boardWidth, borderSize} };
        final double[] intersectionTop = findIntersection(lineBall, lineBoardTop);

        if (intersectionLeft != null) {
            switch (ballDirection) {
                case "upLeft": 
                    ballDirection = "upRight";
                    break;
                case "downLeft": 
                    ballDirection = "downRight";
                    break;
            }
            ballX = intersectionLeft[0] + 1;
            ballY = intersectionLeft[1];

        } else if (intersectionRight != null) {

            switch (ballDirection) {
                case "upRight": 
                    ballDirection = "upLeft";
                    break;
                case "downRight": 
                    ballDirection = "downLeft";
                    break;
            }
            ballX = intersectionRight[0] - 1;
            ballY = intersectionRight[1];

        } else if (intersectionTop != null) {

            switch (ballDirection) {
                case "upRight": 
                    ballDirection = "downRight"; 
                    break;
                case "upLeft": 
                    ballDirection = "downLeft"; 
                    break;
            }
            ballX = intersectionTop[0];
            ballY = intersectionTop[1] + 1;

        } else {
            if (ballNextY > boardHeight) {
                gameStatus = "gameOver";
            } else {
                ballX = ballNextX;
                ballY = ballNextY;
            }
        }

        // Check ball collision with player
        final double[][] linePlayer = { {playerX - playerHalf, playerY}, {playerX + playerHalf, playerY} };
        final double[] intersectionPlayer = findIntersection(lineBall, linePlayer);

        if (intersectionPlayer != null) {

            switch (ballDirection) {
                case "downRight": 
                    ballDirection = "upRight";
                    break;
                case "downLeft": 
                    ballDirection = "upLeft";
                    break;
            }
            ballX = intersectionPlayer[0];
            ballY = intersectionPlayer[1] - 1;
            playerPoints = playerPoints + 1;
            ballSpeed = ballSpeed + ballSpeedIncrement;
            playerSpeed = playerSpeed + playerSpeedIncrement;
        }

        // Set player Y position
        playerY = canvas.getHeight() - playerHeight - 10;
    }

    public void draw() {

        // Clean drawing area
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw board
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(borderSize);
        gc.strokeRect(0, 0, borderSize, canvas.getHeight());
        gc.strokeRect(0, 0, canvas.getWidth(), borderSize);
        gc.strokeRect(canvas.getWidth() - borderSize, 0, borderSize, canvas.getHeight());

        // Draw player
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(playerHeight);
        gc.strokeRect(playerX - playerHalf, playerY, playerWidth, playerHeight);

        // Draw ball
        gc.setFill(Color.BLACK);
        gc.fillArc(ballX - ballHalf, ballY - ballHalf, ballSize, ballSize, 0.0, 360, ArcType.ROUND);

        // Draw text with points
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 20));
        String pointsText = "Points: " + playerPoints;
        drawText(gc, pointsText, canvas.getWidth() - 20, 20, "right");

        // Draw game over text
        if (gameStatus.equals("gameOver")) {
            final double boardCenterX = canvas.getWidth() / 2;
            final double boardCenterY = canvas.getHeight() / 2;

            gc.setFont(new Font("Arial", 40));
            drawText(gc, "GAME OVER", boardCenterX, boardCenterY - 20, "center");

            gc.setFont(new Font("Arial", 20));
            drawText(gc, "You are a loser!", boardCenterX, boardCenterY + 20, "center");
        }

        // Draw FPS if needed
        if (showFps) { animationTimer.draw(gc); }   
    }

    public static void drawText(GraphicsContext gc, String text, double x, double y, String alignment) {
        Text tempText = new Text(text);
        tempText.setFont(gc.getFont());
        final double textWidth = tempText.getLayoutBounds().getWidth();
        final double textHeight = tempText.getLayoutBounds().getHeight();
        switch (alignment) {
            case "center":
                x = x - textWidth / 2;
                y = y + textHeight / 2;
                break;
            case "right":
                x = x - textWidth;
                y = y + textHeight / 2;
                break;
            case "left":
                y = y + textHeight / 2;
                break;
        }
        gc.fillText(text, x, y);
    }

    public static double[] findIntersection(double[][] lineA, double[][] lineB) {
        double[] result = new double[2];
    
        final double aX0 = lineA[0][0];
        final double aY0 = lineA[0][1];
        final double aX1 = lineA[1][0];
        final double aY1 = lineA[1][1];
    
        final double bX0 = lineB[0][0];
        final double bY0 = lineB[0][1];
        final double bX1 = lineB[1][0];
        final double bY1 = lineB[1][1];
    
        double x, y;
    
        if (aX1 == aX0) { // lineA is vertical
            if (bX1 == bX0) { // lineB is vertical too
                return null;
            }
            x = aX0;
            final double bM = (bY1 - bY0) / (bX1 - bX0);
            final double bB = bY0 - bM * bX0;
            y = bM * x + bB;
        } else if (bX1 == bX0) { // lineB is vertical
            x = bX0;
            final double aM = (aY1 - aY0) / (aX1 - aX0);
            final double aB = aY0 - aM * aX0;
            y = aM * x + aB;
        } else {
            final double aM = (aY1 - aY0) / (aX1 - aX0);
            final double aB = aY0 - aM * aX0;
    
            final double bM = (bY1 - bY0) / (bX1 - bX0);
            final double bB = bY0 - bM * bX0;

            final double tolerance = 1e-5;
            if (Math.abs(aM - bM) < tolerance) { 
                return null;
            }
    
            x = (bB - aB) / (aM - bM);
            y = aM * x + aB;
        }
    
        // Check if the intersection point is within the bounding boxes of both line segments
        final double boundingBoxTolerance = 1e-5;
        final boolean withinA = x >= Math.min(aX0, aX1) - boundingBoxTolerance &&
                                x <= Math.max(aX0, aX1) + boundingBoxTolerance &&
                                y >= Math.min(aY0, aY1) - boundingBoxTolerance &&
                                y <= Math.max(aY0, aY1) + boundingBoxTolerance;
        final boolean withinB = x >= Math.min(bX0, bX1) - boundingBoxTolerance &&
                                x <= Math.max(bX0, bX1) + boundingBoxTolerance &&
                                y >= Math.min(bY0, bY1) - boundingBoxTolerance &&
                                y <= Math.max(bY0, bY1) + boundingBoxTolerance;

        if (withinA && withinB) {
            result[0] = x;
            result[1] = y;
        } else {
            return null;
        }
    
        return result;
    }
}
