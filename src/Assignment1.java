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

    int WINDOW_WIDTH = 800;
    int WINDOW_HEIGHT = 800;
    int startFrequency = -100;
    int endFrequency = 100;
    double deltaT = 0.001;

    ArrayList<Double> lengths;
    ArrayList<Double> startAngles;
    Group linesGroup = new Group();
    Group circleGroup = new Group();

    @Override
    public void start(Stage primaryStage) throws Exception {

        //gets list of coordinates
        ArrayList coor = loadFunction();
        //calculates the change in time
        deltaT = 1.0 / (coor.size() - 1);

        //draws function
        drawFunction(coor);

        Group root = new Group();
        root.getChildren().addAll(linesGroup, circleGroup);

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


    //creates an arraylist of coordinates from a mathematical function
    public ArrayList loadFunction() {
        ArrayList<Pair<Double, Double>> coor = new ArrayList();

        double maxJ = 15;
        for (double i = 0, j = -15; j < maxJ; i += deltaT, j += 0.1) {
            coor.add(new Pair((j), inputFunction((j))));

            Circle circle = new Circle((j) + WINDOW_WIDTH / 2
                    , inputFunction((j)) + WINDOW_HEIGHT / 2, 2);
            circle.setFill(Color.RED);
            circleGroup.getChildren().add(circle);
        }
        return coor;
    }

    //creates an arraylist of coordinates from a file
    public ArrayList loadFunction(String file) throws FileNotFoundException {
        ArrayList<Pair<Double, Double>> coor = new ArrayList();

        //reads file
        File input = new File(file);
        Scanner scan = new Scanner(input);

        while (scan.hasNextLine()) {
            //adds coordinates
            String line[] = scan.nextLine().split(",");
            coor.add(new Pair(Double.parseDouble(line[0]), -Double.parseDouble(line[1])));

            Circle circle = new Circle(Double.parseDouble(line[0]) + WINDOW_WIDTH / 2,
                    -Double.parseDouble(line[1]) + WINDOW_HEIGHT / 2, 1);

            circle.setFill(Color.RED);
            circleGroup.getChildren().add(circle);
        }
        scan.close();
        return coor;
    }

    private double inputFunction(double x) {
        return x * x;
    }

    public ArrayList findCfxCfy(ArrayList<Pair<Double, Double>> coor) {

        ArrayList<Pair<Double, Double>> endCoor = new ArrayList<>();
        lengths = new ArrayList<>();

        for (int frequency = startFrequency; frequency <= endFrequency; frequency++) {

            double startX = 0;
            double startY = 0;

            int tIndex = 0;

            for (double t = 0; tIndex < coor.size(); t += deltaT) {
                Double X = coor.get(tIndex).getKey();
                Double Y = coor.get(tIndex).getValue();
                startX += deltaT * (X * Math.cos(2 * Math.PI * frequency * t)
                        + Y * Math.sin(2 * Math.PI * frequency * t));
                startY -= deltaT * (X * Math.sin(2 * Math.PI * frequency * t)
                        - Y * Math.cos(2 * Math.PI * frequency * t));
                tIndex++;
            }
            lengths.add(Math.sqrt(startX * startX + startY * startY));
            endCoor.add(new Pair(startX, startY));
        }
        return endCoor;
    }

    public void drawFunction(ArrayList coor) {

        //linesGroup = new Group();
        ArrayList<Pair<Double, Double>> endCoor = findCfxCfy(coor);

        ArrayList<Pair<Integer, Pair<Double, Double>>> lines = new ArrayList<>();

        double startingEndX = 0;
        double startingEndY = 0;
        for (int i = 0, lineNum = startFrequency; lineNum <= endFrequency; i++, lineNum++) {
            lines.add(new Pair(lineNum, endCoor.get(i)));
            startingEndX += endCoor.get(i).getKey();
            startingEndY += endCoor.get(i).getValue();
        }

        Path path = new Path();
        path.getElements().add(new MoveTo(startingEndX, startingEndY));
        circleGroup.getChildren().add(path);

        AnimationTimer timer = new AnimationTimer() {
            double time = 0;

            @Override
            public void handle(long now) {
                linesGroup.getChildren().clear();

                double lastX = WINDOW_WIDTH / 2;
                double lastY = WINDOW_HEIGHT / 2;

                for (int i = 0, freq = startFrequency; freq <= endFrequency; i++, freq++) {
                    double updatedEndX = findX(endCoor.get(i).getKey(), endCoor.get(i).getValue(), freq, time);
                    double updatedEndY = findY(endCoor.get(i).getKey(), endCoor.get(i).getValue(), freq, time);

                    linesGroup.getChildren().add(
                            new Line(lastX, lastY,
                                    lastX + updatedEndX,
                                    lastY + updatedEndY)
                    );
                    lastX += updatedEndX;
                    lastY += updatedEndY;
                }
                path.getElements().add(new LineTo(lastX, lastY));
                time += deltaT;
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
