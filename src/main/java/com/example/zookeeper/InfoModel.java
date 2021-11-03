package com.example.zookeeper;

import com.sun.jna.platform.win32.WinDef.RECT;

public class InfoModel {
    // Singleton class to store settings and state of the program
    private String selectedWindow;
    private boolean gameAreaDetected;
    private int boxX;
    private int boxY;
    private int boxLen;
    private int movesPerShot = 5;
    public static InfoModel INSTANCE = new InfoModel();

    private InfoModel(){}
    public static InfoModel getInstance() {
        return INSTANCE;
    }

    public void setSelectedWindow(String selWindow) {
        this.selectedWindow = selWindow;
    }
    public String getSelectedWindow() {
        if (this.selectedWindow != null && this.selectedWindow.trim().length() > 0) {
            return this.selectedWindow;
        }
        else {
            return null;
        }
    }

    public void setBoxLoc(int boxX, int boxY, int boxLen, boolean gameAreaDetected) {
        this.boxX = boxX;
        this.boxY = boxY;
        this.boxLen = boxLen;
        this.gameAreaDetected = gameAreaDetected;
    }
    public void setBoxLoc(RECT matchRect, boolean gameAreaDetected) {
        this.boxX = matchRect.left;
        this.boxY = matchRect.top;
        this.boxLen = matchRect.right - matchRect.left;
        this.gameAreaDetected = gameAreaDetected;
    }
    public int boxLeft() {
        return this.boxX;
    }
    public int boxTop() {
        return this.boxY;
    }
    public int boxLen() {
        return this.boxLen;
    }
    public RECT getBoxRect() {
        RECT rect = new RECT();
        rect.top = this.boxY;
        rect.left = this.boxX;
        rect.bottom = this.boxY + this.boxLen;
        rect.right = this.boxX + this.boxLen;
        return rect;
    }
    public boolean detectedGameArea() {return this.gameAreaDetected;}
    public void setMovesPerShot(int movesPerShot) {this.movesPerShot = movesPerShot;}
    public int getMovesPerShot() {return this.movesPerShot;}

}
