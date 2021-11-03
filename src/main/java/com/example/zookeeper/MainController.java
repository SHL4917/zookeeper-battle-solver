package com.example.zookeeper;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.sun.jna.platform.win32.WinDef.RECT;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class MainController {

    private InfoModel infoModel;
    private Boolean solverRunning = false;

    @FXML private ComboBox<String> windowList;
    @FXML private Label promptText;
    @FXML private Label gameCoordText;
    @FXML private javafx.scene.control.TextArea log;
    @FXML private RadioButton autoDetect;
    @FXML private RadioButton manualDetect;
    @FXML private ToggleGroup searchMode;
    @FXML private Spinner numMovesSpinner;

    @FXML
    public void initialize() throws IOException {
        //System.load("C:\\opencv\\build\\java\\x64\\opencv_java3414.dll");
        System.loadLibrary("opencv_java3414");

        infoModel = InfoModel.getInstance();
        infoModel.setBoxLoc(0, 0, 0, false);
        infoModel.setSelectedWindow("");
        boxStatus();
        numMovesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> infoModel.setMovesPerShot((int) newVal));
        autoDetect.setUserData("Auto Detection");
        manualDetect.setUserData("Manual Detection");
        try {
            GlobalScreen.registerNativeHook();
        }
        catch (NativeHookException e) {
            System.out.println("There was a problem registering the native hook.");
            System.out.println(e);
        }

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyTyped(NativeKeyEvent e) {
                Platform.runLater(() -> {
                    if (e.getKeyChar() == 'q' && !solverRunning) {
                        startSolver();
                    } else if (e.getKeyChar() == 'w' && solverRunning) {
                        stopSolver();
                    }
                });
            }
        });
    }
    @FXML
    protected void initDetectBox() {
        try{
            infoModel.setBoxLoc(0, 0, 0, false);
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("detectView.fxml"));

            Scene scene = new Scene(fxmlLoader.load(), 400, 400);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setOpacity(0.7);

            stage.setTitle("New Window");
            stage.setScene(scene);
            stage.show();

            DetectController detectController = fxmlLoader.getController();
            detectController.enableDrag();
            detectController.getMainController(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void getWindowList() {
        windowList.getItems().clear();
        windowList.getItems().addAll(Utilities.getOpenedWindows());
    }
    @FXML
    protected void setWindowChosen() {
        String windowChosen = windowList.getSelectionModel().getSelectedItem();
        infoModel.setSelectedWindow(windowChosen);
    }

    @FXML
    protected void resetBoxLoc() {
        infoModel.setBoxLoc(0, 0, 0, false);
        boxStatus();
    }
    @FXML
    protected void startSolver(){
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                String searchType = searchMode.getSelectedToggle().getUserData().toString();

                RECT windowRect = new RECT();
                RECT matchRect = new RECT();
                RECT matchRectGlobal = new RECT();
                int[][] animalMat = new int[8][8];
                int[][] mouseLoc = new int[64][2];
                boolean movesNotEmpty;
                int stopCount = 0;

                if (searchType == "Auto Detection" && infoModel.getSelectedWindow() == null) {
                    errMsg("No window selected!");
                } else if (searchType == "Manual Detection" && !infoModel.detectedGameArea()) {
                    errMsg("Match area coordinates set yet!");
                } else if (!infoModel.detectedGameArea()) {
                    try {
                        windowRect = Utilities.getWindowCoords(infoModel.getSelectedWindow());
                        BufferedImage bi = Utilities.takeScreenShot(windowRect);
                        matchRect = Utilities.detectPuzzleArea(bi);
                        if (matchRect == null) {
                            solverRunning = false;
                            errMsg("Unable to detect game window, reset location or try using manual detection instead!");
                        } else {
                            solverRunning = true;
                            matchRectGlobal = Utilities.getPuzzleCoords(windowRect, matchRect);
                            infoModel.setBoxLoc(matchRectGlobal, true);
                        }
                    }catch (Exception e) {
                        System.out.println(e);
                        errMsg("Something went wrong lol");
                    }
                } else {
                    solverRunning = true;
                    matchRectGlobal = infoModel.getBoxRect();
                    errMsg("Solving...");
                }

                searchMode.getToggles().forEach(toggle -> {
                    Node node = (Node) toggle ;
                    node.setDisable(true);
                });
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        boxStatus();
                    }
                });
                mouseLoc = MouseControl.getMouseLoc(matchRectGlobal);

                while (solverRunning) {
                    if (stopCount == 20) {
                        solverRunning = false;
                        errMsg("No possible moves found, if unintended reset location!");
                    }
                    BufferedImage biMatchArea = Utilities.takeScreenShot(matchRectGlobal);
                    animalMat = Solver.detectAnimals(biMatchArea);
                    int[][] moves = Solver.getMoves(animalMat, infoModel.getMovesPerShot());
                    movesNotEmpty = MouseControl.performActions(moves, mouseLoc);
                    if (!movesNotEmpty) {
                        stopCount++;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                stopSolver();
            }
        });
        t1.start();

    }

    @FXML
    protected void stopSolver(){
        searchMode.getToggles().forEach(toggle -> {
            Node node = (Node) toggle ;
            node.setDisable(false);
        });
        this.solverRunning = false;
    }

    protected void boxStatus() {
        if (infoModel.detectedGameArea()) {
            gameCoordText.setText("Screenshot Coordinates:\n" +
                    "Top left: (" + infoModel.boxLeft() + ", " + infoModel.boxTop() + ")\n" +
                    "Width: " + infoModel.boxLen());
        }
        else {
            gameCoordText.setText("Match area not detected!");
        }
    }
    public void printLog(String s) {
        log.setText(s);
    }
    public void errMsg(String e) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                promptText.setText(e);
            }
        });
    }
}