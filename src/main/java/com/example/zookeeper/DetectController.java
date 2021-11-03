package com.example.zookeeper;

import com.sun.jna.platform.win32.WinDef.RECT;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;

public class DetectController {
    @FXML
    private BorderPane matchBox;
    @FXML
    private Label status;
    @FXML
    private VBox vBox;


    private boolean draggable = false;
    private Stage stage;
    private MainController mainController;
    private InfoModel infoModel;

    @FXML
    public void initialize() {
        infoModel = InfoModel.getInstance();
        status.setText("Game window not detected!");
        matchBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.P) {
                this.makeBigger();
            }
            else if (event.getCode() == KeyCode.M) {
                this.makeSmaller();
            }
            else if (event.getCode() == KeyCode.D) {
                this.checkValid();
            }
        });
    }

    @FXML
    public void closeWindow() {
        stage.close();
    }
    @FXML
    public void saveCoords() {
        RECT matchArea = new RECT();
        matchArea.top = (int) stage.getY();
        matchArea.left = (int) stage.getX();
        matchArea.bottom = (int) (stage.getY() + stage.getHeight());
        matchArea.right = (int) (stage.getX() + stage.getWidth());
        infoModel.setBoxLoc(matchArea, true);
        mainController.boxStatus();
    }
    @FXML
    public void debugSolver() {
        RECT matchArea = new RECT();
        matchArea.top = (int) stage.getY();
        matchArea.left = (int) stage.getX();
        matchArea.bottom = (int) (stage.getY() + stage.getHeight());
        matchArea.right = (int) (stage.getX() + stage.getWidth());
        stage.setIconified(true);
        int[][] animalMat = Solver.detectAnimals(Utilities.takeScreenShot(matchArea));
        int[][] moves = Solver.getMoves(animalMat, 10);
        for (int ctr = 0; ctr < 10; ctr++) {
            System.out.println(moves[ctr][0] + " to " + moves[ctr][1]);
        }
    }


    public void getMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void enableDrag() {
        if (!draggable) {
            draggable = true;
            this.stage = (Stage) vBox.getScene().getWindow();
            vBox.setOnMousePressed(pressEvent -> vBox.setOnMouseDragged(dragEvent -> {
                stage.setX(dragEvent.getScreenX() - pressEvent.getSceneX());
                stage.setY(dragEvent.getScreenY() - pressEvent.getSceneY());
            }));
        }
    }

    private void makeBigger() {
        stage.setHeight(stage.getHeight() + 1);
        stage.setWidth(stage.getHeight() + 1);

    }
    private void makeSmaller() {
        stage.setHeight(stage.getHeight() - 1);
        stage.setWidth(stage.getHeight() - 1);
    }
    private void checkValid() {

        RECT matchArea = new RECT();
        matchArea.top = (int) stage.getY();
        matchArea.left = (int) stage.getX();
        matchArea.bottom = (int) (stage.getY() + stage.getHeight());
        matchArea.right = (int) (stage.getX() + stage.getWidth());
        stage.setIconified(true);
        int[][] animalMat = Solver.detectAnimals(Utilities.takeScreenShot(matchArea));
        String s = Solver.getAnimalNames(animalMat);
        stage.setIconified(false);
        mainController.printLog(s);
    }


}
