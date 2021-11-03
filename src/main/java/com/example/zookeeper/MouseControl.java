package com.example.zookeeper;


import com.sun.jna.platform.win32.WinDef;

import java.awt.*;
import java.awt.event.InputEvent;

public class MouseControl {
    private static void mouseAction(int x1, int y1, int x2, int y2) {
        try {
            Robot bot = new Robot();
            if (x1 == x2 && y1 == y2) {
                bot.mouseMove(x1, y1);
                bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            } else {
                bot.mouseMove(x1, y1);
                bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                Thread.sleep(100);
                bot.mouseMove(x2, y2);
                bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            }
        } catch (AWTException | InterruptedException e) {
            System.out.println(e);
        }

    }

    public static int[][] getMouseLoc(WinDef.RECT rect) {
        int[][] loc = new int[64][2];
        int windowLen = rect.right - rect.left;
        int[] divisions = new int[8];

        for (int i = 0; i < 8; i++) {
            divisions[i] = i * (windowLen/8) + windowLen/16;
        }

        int ctr = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                loc[ctr][0] = divisions[j] + rect.left;
                loc[ctr][1] = divisions[i] + rect.top;
                ctr++;
            }
        }
        return loc;
    }

    public static boolean performActions(int[][] moves, int[][] loc) {
        int move1 = 0;
        int move2 = 0;

        for (int[] move : moves) {
            if (move[0] == 0) {
                continue;
            }
            move1 = move[0];
            move2 = move[1];
            mouseAction(loc[move1 - 1][0], loc[move1 - 1][1], loc[move2 - 1][0], loc[move2 - 1][1]);
        }
        if (moves[0][0] == 0) {return false;}
        return true;
    }
}
