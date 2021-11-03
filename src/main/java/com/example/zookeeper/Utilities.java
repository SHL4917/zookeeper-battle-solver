package com.example.zookeeper;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.RECT;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;


public class Utilities {
    public static ArrayList<String> getOpenedWindows() {
        ArrayList<String> windowList = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hwnd, pointer) -> {
            char[] windowText = new char[512];
            if (User32.INSTANCE.IsWindowVisible(hwnd)){
                User32.INSTANCE.GetWindowText(hwnd, windowText, 512);
                windowList.add(Native.toString(windowText).trim());
            }
            return true;
        }, null);

        return (ArrayList<String>) windowList.stream()
                .filter(s -> s.trim().length() > 0)
                .sorted()
                .collect(Collectors.toList());
    }

    public static RECT getWindowCoords(String windowName) throws InterruptedException {
        HWND hwnd = User32.INSTANCE.FindWindow(null, windowName);
        if (hwnd != null) {
            RECT rect = new RECT();
            User32.INSTANCE.ShowWindow(hwnd, 1);
            User32.INSTANCE.BringWindowToTop(hwnd);
            User32.INSTANCE.GetWindowRect(hwnd, rect);
            TimeUnit.MILLISECONDS.sleep(300);
            return rect;
        }
        else {
            return null;
        }
    }

    public static BufferedImage takeScreenShot(RECT rect) {
        Robot imageRobot = null;
        try {
            imageRobot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        Rectangle area = new Rectangle(rect.left,
                rect.top,
                rect.right - rect.left,
                rect.bottom - rect.top);
        return imageRobot.createScreenCapture(area);
    }

    public static RECT detectPuzzleArea(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte r;
        byte g;
        byte b;
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgb = bi.getRGB(x, y);
                r = (byte) (rgb & 0xFF);
                g = (byte) ((rgb >> 8) & 0xFF);
                b = (byte) ((rgb >> 16) & 0xFF);
                byte[] pixelData = {r, g, b};

                mat.put(y, x, pixelData);
            }
        }

        Mat lines = new Mat();
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(mat, mat, 50, 200);
        Imgproc.HoughLinesP(mat, lines, 1, Math.PI/180, 100, 150, 10);

        ArrayList<Integer> filteredInd = new ArrayList<>();
        ArrayList<Integer> filteredInd2 = new ArrayList<>();

        int sq_len = 0;

        for (int i = 0; i < lines.size().height; i++) {
            double[] data = lines.get( i, 0);
            if (data[1] == data[3]) {
                filteredInd.add(i);
                if ((data[2] - data[0]) > sq_len) {
                    sq_len = (int) (data[2] - data[0]);
                }
            }
        }

        for (int idx : filteredInd) {
            double[] data = lines.get(idx, 0);
            if ((data[2] - data[0]) > (sq_len - 30)) {
                filteredInd2.add(idx);
            }
        }

        int[][] filteredLines = new int[filteredInd2.size()][4];

        for (int i = 0; i < filteredInd2.size(); i++) {
            for (int j = 0; j < 4; j++) {
                filteredLines[i][j] = (int) lines.get(filteredInd2.get(i), 0)[j];
            }
        }

        Arrays.sort(filteredLines, Comparator.comparingInt(o -> o[1]));

        int[] diff = new int[filteredInd2.size()];
        ArrayList<Integer> corIndx = new ArrayList<>();

        for (int i = 0; i < filteredInd2.size() - 1; i++) {
            diff[i] = filteredLines[i + 1][1] - filteredLines[i][1];
            if (diff[i] < sq_len + 50 && diff[i] > sq_len - 50) {
                corIndx.add(i);
            }
        }

        int[][] boundary = new int[2][4];
        if (corIndx.size() == 1) {
            for (int i = 0; i < 4; i++) {
                boundary[0][i] = filteredLines[corIndx.get(0)][i];
                boundary[1][i] = filteredLines[corIndx.get(0) + 1][i];
            }
        }
        else {
            System.out.println("Unable to find match area!");
            return null;
        }

        RECT matchArea = new RECT();
        matchArea.left = boundary[0][0];
        matchArea.right = boundary[0][2];

        int midPoint = boundary[0][1] + (boundary[1][1] - boundary[0][1])/2;
        matchArea.top = (midPoint - sq_len/2);
        matchArea.bottom = (midPoint + sq_len/2);
        return matchArea;
    }

    public static RECT getPuzzleCoords(RECT windowRect, RECT matchRect) {
        RECT matchRectGlobal = new RECT();
        matchRectGlobal.left = matchRect.left + windowRect.left;
        matchRectGlobal.right = matchRect.right + windowRect.left;
        matchRectGlobal.top = matchRect.top + windowRect.top;
        matchRectGlobal.bottom = matchRect.bottom + windowRect.top;

        return matchRectGlobal;
    }

    public static String rightPadding(String str, int num) {
        return String.format("%1$-" + num + "s", str);
    }
}
