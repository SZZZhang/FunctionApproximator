import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
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
    int startFrequency = -3;
    int endFrequency = 3;
    double deltaT = 0.001;

    ArrayList<Double> lengths;
    Group linesGroup = new Group();
    Group circleGroup = new Group();

    @Override
    public void start(Stage primaryStage) throws Exception {

        drawFunction(loadFunction());

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

        for (double i = -1, j = 0; i < 1; i += deltaT, j++ ){
            coor.add(new Pair(i*15, inputFunction(i*15)));
            circleGroup.getChildren().add(new Circle(i*15 + WINDOW_WIDTH/2
                    , inputFunction(i*15) + WINDOW_HEIGHT/2, 2));
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
            coor.add(new Pair(Double.parseDouble(line[0]), Double.parseDouble(line[1])));
        }
        scan.close();
        return coor;
    }

    private double inputFunction(double x) {
        return x*x;
    }

    public ArrayList getStartCoor(ArrayList<Pair<Double, Double>> coor) {

        ArrayList<Pair<Double, Double>> endCoor = new ArrayList<>();
        lengths = new ArrayList<>();

        for (int frequency = startFrequency; frequency <= endFrequency; frequency++) {

            double startX = 0;
            double startY = 0;

            for (double t = 0, tIndex = 0; t <= 1; t += deltaT, tIndex++) {
                Double X = coor.get((int) tIndex).getKey();
                Double Y = coor.get((int) tIndex).getValue();
                startX += deltaT * (X * Math.cos(2 * Math.PI * frequency * t)
                        + Y * Math.sin(2 * Math.PI * frequency * t));
                startY -= deltaT * (Y * Math.sin(2 * Math.PI * frequency * t)
                        - Y * Math.cos(2 * Math.PI * frequency * t));

            }

            lengths.add(Math.sqrt(startX * startX + startY * startY));
            endCoor.add(new Pair(startX, startY));
        }

        return endCoor;
    }

    public void drawFunction(ArrayList coor) {
        //linesGroup = new Group();
        ArrayList<Pair<Double, Double>> endCoor = getStartCoor(coor);

        ArrayList<Pair<Integer, Pair<Double, Double>>> lines = new ArrayList<>();

        for (int i = 0, lineNum = startFrequency; lineNum <= endFrequency; i++, lineNum++) {
            lines.add(new Pair(lineNum, endCoor.get(i)));
        }

        Path path = new Path();
        path.getElements().add(new MoveTo(0,0));
        circleGroup.getChildren().add(path);

        AnimationTimer timer = new AnimationTimer() {
            double time = 0;

            @Override
            public void handle(long now) {
                linesGroup.getChildren().clear();

                double lastX = WINDOW_WIDTH / 2;
                double lastY = WINDOW_HEIGHT / 2;

                for (int i = 0, freq = startFrequency; i < lengths.size(); i++, freq++) {
                    linesGroup.getChildren().add(
                            new Line(lastX, lastY,
                                    lastX + cos(lengths.get(i), freq, time),
                                    lastY + sin(lengths.get(i), freq, time))
                    );
                    lastX += cos(lengths.get(i), freq, time);
                    lastY += sin(lengths.get(i), freq, time);
                }
                path.getElements().add(new LineTo(lastX, lastY));
                System.out.println(lastX + " " + lastY);
                time += deltaT;
            }
        };

        timer.start();

    }

    public double sin(double a, double f, double t) {
        return a * Math.sin(Math.toRadians(360 * f) * t);
    }

    public double cos(double a, double f, double t) {
        return a * Math.cos(Math.toRadians(360 * f) * t);
    }
}
