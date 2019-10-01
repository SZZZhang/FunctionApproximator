/*
Program by: Shirley Zhang
Course code: ICS4U
Date: Sept 30th, 2019
Instructor: Radulovic
Assignment: Review Assignment
Description of Program:
This program approximates a given function and approximates it
using spinning lines of increasing frequencies.
*/

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Assignment1 extends Application {

    //CONSTANTS
    private int WINDOW_WIDTH = 800;
    private int WINDOW_HEIGHT = 800;
    private int START_FREQUENCY = -100;
    private int END_FREQUENCY = 100;
    private int MAX_TIME = 1; //maximum length of time for the program to run

    //domain of the original function
    private int DOMAIN_START = -15;
    private int DOMAIN_END = 15;

    //increment of x when generating original function
    private double FUNCTION_INCREMENT = 0.1;

    //radius of dots for drawing the original function
    private int DOT_RADIUS = 1;

    //change in time calculated based on the number of coordinates in the original function
    private double DELTA_T;

    //groups
    private Group linesGroup;
    private Group originalGroup;
    private Group pathGroup;

    @Override
    public void start(Stage primaryStage) {

        ArrayList coor = loadFunction(); //gets list of coordinates of original function
        DELTA_T = 1.0 / (coor.size() - 1); //calculated based on number of coordinates of original function

        drawFunction(coor); //draws original and approximated functions

        Group root = new Group();
        root.getChildren().addAll(linesGroup, originalGroup, pathGroup);

        primaryStage.setTitle("Function Approximation");
        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    //creates an ArrayList of coordinates from a mathematical function
    public ArrayList loadFunction() {
        ArrayList<Pair<Double, Double>> coordinates = new ArrayList();

        //generates list of coordinates from X = DOMAIN_START to X = DOMAIN_END
        for (double X = DOMAIN_START; X <= DOMAIN_END; X += FUNCTION_INCREMENT) {
            coordinates.add(new Pair((X), inputFunction((X))));
        }

        return coordinates;
    }

    //creates an arraylist of coordinates from a file
    public ArrayList loadFunction(String file) {

        ArrayList<Pair<Double, Double>> coordinates = new ArrayList();

        try {
            //reads file
            File input = new File(file);
            Scanner scan = new Scanner(input);

            while (scan.hasNextLine()) {
                //gets coordinates
                String line[] = scan.nextLine().split(",");
                coordinates.add(new Pair(Double.parseDouble(line[0]), Double.parseDouble(line[1])));
            }

            scan.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found :(");
        }
        return coordinates;
    }

    //hardcoded input function
    private double inputFunction(double x) {
        return x * x;
    }

    //gets all the x and y components for all the lines
    //takes in the original function coordinates as input
    public ArrayList findCfxCfy(ArrayList<Pair<Double, Double>> originalCoor) {

        ArrayList<Pair<Double, Double>> endCoor = new ArrayList<>();

        //loops through each line from START_FREQUENCY to END_FREQUENCY
        for (int freq = START_FREQUENCY; freq <= END_FREQUENCY; freq++) {

            double Cfx = 0; //x component
            double Cfy = 0; //y component

            double time = 0;

            for (int index = 0; index < originalCoor.size(); index++) {
                // x and y of original function
                Double X = originalCoor.get(index).getKey();
                Double Y = originalCoor.get(index).getValue();

                //calculates components
                Cfx += DELTA_T * (X * Math.cos(2 * Math.PI * freq * time)
                        + Y * Math.sin(2 * Math.PI * freq * time));
                Cfy -= DELTA_T * (X * Math.sin(2 * Math.PI * freq * time)
                        - Y * Math.cos(2 * Math.PI * freq * time));

                time += DELTA_T;
            }
            //appends coordinates to endCoor ArrayList
            endCoor.add(new Pair(Cfx, Cfy));
        }
        return endCoor;
    }

    //draws the original and approximated functions
    //takes in a list of coordinates from the original function
    public void drawFunction(ArrayList<Pair<Double, Double>> originalCoor) {

        linesGroup = new Group(); //group for spinning lines
        originalGroup = new Group(); //group for original function
        pathGroup = new Group(); //group for path object

        //draws original function
        for (int i = 0; i < originalCoor.size(); i++) {
            Circle dot = new Circle(originalCoor.get(i).getKey() + WINDOW_WIDTH / 2,
                    -originalCoor.get(i).getValue() + WINDOW_HEIGHT / 2, DOT_RADIUS);
            dot.setFill(Color.RED);
            originalGroup.getChildren().add(dot);
        }

        //finds end coordinates of each line when time = 0
        ArrayList<Pair<Double, Double>> endCoor = findCfxCfy(originalCoor);

        //finds the sum of the spinning line vectors when time = 0
        double startingEndX = 0;
        double startingEndY = 0;

        for (int i = 0, lineNum = START_FREQUENCY; lineNum <= END_FREQUENCY; i++, lineNum++) {
            startingEndX += endCoor.get(i).getKey();
            startingEndY += endCoor.get(i).getValue();
        }

        //sets the first point in the approximated function
        Path approximatedFunc = new Path();
        approximatedFunc.setStroke(Color.BLUE);
        approximatedFunc.getElements().add(new MoveTo(startingEndX + WINDOW_WIDTH / 2,
                -startingEndY + WINDOW_HEIGHT / 2));
        pathGroup.getChildren().add(approximatedFunc);

        AnimationTimer timer = new AnimationTimer() {
            double time = 0;

            @Override
            public void handle(long now) {
                //stops timer if time passed is more than MAX_TIME
                if (time > MAX_TIME) this.stop();

                //clears previously drawn spinning lines
                linesGroup.getChildren().clear();

                //represents the sum of the x and y components of all previous lines
                double lastX = WINDOW_WIDTH / 2;
                double lastY = WINDOW_HEIGHT / 2;

                //loops through all spinning lines
                for (int i = 0, freq = START_FREQUENCY; freq <= END_FREQUENCY; i++, freq++) {

                    //updates the end coordinates of this line
                    double updatedEndX = findX(endCoor.get(i).getKey(), endCoor.get(i).getValue(), freq, time);
                    double updatedEndY = -findY(endCoor.get(i).getKey(), endCoor.get(i).getValue(), freq, time);

                    linesGroup.getChildren().add(
                            new Line(lastX, lastY, lastX + updatedEndX, lastY + updatedEndY));

                    //adds the x and y components of this line to the sum of all previous x and y components of lines
                    lastX += updatedEndX;
                    lastY += updatedEndY;
                }
                //draws new point in approximated function
                approximatedFunc.getElements().add(new LineTo(lastX, lastY));


                //increments time
                time += DELTA_T;

            }
        };
        //starts timer
        timer.start();

    }

    //finds x of a line with x and y components of cfx and cfy at a certain frequency and time
    public double findX(double cfx, double cfy, double freq, double time) {
        return cfx * Math.cos(2 * Math.PI * freq * time) - cfy * Math.sin(2 * Math.PI * freq * time);
    }

    //finds y of a line with x and y components of cfx and cfy at a certain frequency and time
    public double findY(double cfx, double cfy, double freq, double time) {
        return cfx * Math.sin(2 * Math.PI * freq * time) + cfy * Math.cos(2 * Math.PI * freq * time);
    }
}
