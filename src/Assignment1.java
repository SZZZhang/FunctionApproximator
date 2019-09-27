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

    //constants
    int WINDOW_WIDTH = 800;
    int WINDOW_HEIGHT = 800;
    int START_FREQUENCY = -100;
    int END_FREQUENCY = 100;
    int DOT_RADIUS = 2; //radius of width of dot for drawing the original function

    //constants for generating the original function
    //domain of the original function
    int DOMAIN_START = -15;
    int DOMAIN_END = 15;

    //increment of x when generating original function
    double FUNCTION_INCREMENT = 0.1;

    //change in time calculated based on the number of coordinates in the original function
    double DELTA_T = 0.001;

    //groups
    Group linesGroup;
    Group originalGroup;
    Group pathGroup;

    @Override
    public void start(Stage primaryStage) {

        ArrayList coor = loadFunction("function.txt"); //gets list of coordinates
        DELTA_T = 1.0 / (coor.size() - 1); //calculates the change in time based on size of coordinates

        drawFunction(coor); //draws function

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
        ArrayList<Pair<Double, Double>> coor = new ArrayList();

        //generates list of coordinates from X = DOMAIN_START to X = DOMAIN_END
        for (double X = DOMAIN_START; X <= DOMAIN_END; X += FUNCTION_INCREMENT) {
            coor.add(new Pair((X), inputFunction((X))));
        }

        return coor;
    }

    //creates an arraylist of coordinates from a file
    public ArrayList loadFunction(String file) {

        ArrayList<Pair<Double, Double>> coor = new ArrayList();

        try {
            //reads file
            File input = new File(file);
            Scanner scan = new Scanner(input);

            while (scan.hasNextLine()) {
                //gets coordinates
                String line[] = scan.nextLine().split(",");
                coor.add(new Pair(Double.parseDouble(line[0]), -Double.parseDouble(line[1])));
            }

            scan.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found :(");
        }
        return coor;
    }

    //hard coded input function
    private double inputFunction(double x) {
        return x * x;
    }

    //gets all the x and y components for all the lines
    public ArrayList findCfxCfy(ArrayList<Pair<Double, Double>> coor) {

        ArrayList<Pair<Double, Double>> endCoor = new ArrayList<>();

        //loops through each line from START_FREQUENCY to END_FREQUENCY
        for (int freq = START_FREQUENCY; freq <= END_FREQUENCY; freq++) {

            double Cfx = 0; //x component
            double Cfy = 0; //y component

            int index = 0;

            for (double t = 0; index < coor.size(); t += DELTA_T) { ////////!fix this
                // X and Y of original function
                Double X = coor.get(index).getKey();
                Double Y = coor.get(index).getValue();

                //calculates components
                Cfx += DELTA_T * (X * Math.cos(2 * Math.PI * freq * t)
                        + Y * Math.sin(2 * Math.PI * freq * t));
                Cfy -= DELTA_T * (X * Math.sin(2 * Math.PI * freq * t)
                        - Y * Math.cos(2 * Math.PI * freq * t));
                index++;
            }
            //adds X and Y components
            endCoor.add(new Pair(Cfx, Cfy));
        }
        return endCoor;
    }

    public void drawFunction(ArrayList<Pair<Double, Double>> coor) {

        linesGroup = new Group(); //group for spinning lines
        originalGroup = new Group(); //group for original function
        pathGroup = new Group(); //group for path object

        //draws original function
        for (int i = 0; i < coor.size(); i++) {
            Circle dot = new Circle(coor.get(i).getKey(), coor.get(i).getValue(), DOT_RADIUS);
            dot.setFill(Color.RED);
            originalGroup.getChildren().add(dot);
        }

        //finds end coordinates of all lines when time = 0
        ArrayList<Pair<Double, Double>> endCoor = findCfxCfy(coor);

        double startingEndX = 0;
        double startingEndY = 0;

        for (int i = 0, lineNum = START_FREQUENCY; lineNum <= END_FREQUENCY; i++, lineNum++) {
            startingEndX += endCoor.get(i).getKey();
            startingEndY += endCoor.get(i).getValue();
        }

        Path path = new Path();
        path.getElements().add(new MoveTo(startingEndX, startingEndY));
        pathGroup.getChildren().add(path);

        AnimationTimer timer = new AnimationTimer() {
            double time = 0;

            @Override
            public void handle(long now) {
                linesGroup.getChildren().clear();

                double lastX = WINDOW_WIDTH / 2;
                double lastY = WINDOW_HEIGHT / 2;

                for (int i = 0, freq = START_FREQUENCY; freq <= END_FREQUENCY; i++, freq++) {
                    double updatedEndX = findX(endCoor.get(i).getKey(), endCoor.get(i).getValue(), freq, time);
                    double updatedEndY = findY(endCoor.get(i).getKey(), endCoor.get(i).getValue(), freq, time);

                    linesGroup.getChildren().add(
                            new Line(lastX, lastY, lastX + updatedEndX, lastY + updatedEndY));

                    lastX += updatedEndX;
                    lastY += updatedEndY;
                }
                path.getElements().add(new LineTo(lastX, lastY));
                time += DELTA_T;
                if (time > 1) this.stop();
            }
        };

        timer.start();

    }

    public double findX(double cfx, double cfy, double freq, double time) {
        return cfx * Math.cos(2 * Math.PI * freq * time) - cfy * Math.sin(2 * Math.PI * freq * time);
    }

    public double findY(double cfx, double cfy, double freq, double time) {
        return cfx * Math.sin(2 * Math.PI * freq * time) + cfy * Math.cos(2 * Math.PI * freq * time);
    }
}
