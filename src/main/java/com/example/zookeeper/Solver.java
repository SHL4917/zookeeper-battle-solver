package com.example.zookeeper;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static java.util.Map.entry;

public class Solver {
    private static int[] uniqColorInt = {55424, 98133, 4721760, 13660328, 8440056, 532600, 16277574, 5246976, 16295936,
            4197376, 4771840, 79904, 16316420, 4724736, 16382200, 1579032, 16306312, 3145736, 16744448, 12077057,
            16304360, 16311794};

    private static int[] nameLabel = {10, 10, 5, 5, 2, 2, 6, 6, 9, 9, 3, 3, 4, 4, 7, 7, 1, 1, 11, 11, 8, 8};

    private static Map<Integer, String> nameMap = Map.ofEntries(
            entry(1, "boss"),
            entry(2, "elpt"),
            entry(3, "gatr"),
            entry(4, "grfe"),
            entry(5, "hppo"),
            entry(6, "mnky"),
            entry(7, "pnda"),
            entry(8, "rbbt"),
            entry(9, "lion"),
            entry(10, "ersr"),
            entry(11, "pwer"),
            entry(0, "NULL")
    );

    public static String getAnimalNames(int[][] animalMat) {
        String s = new String();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (j == 7) {
                    s += nameMap.get(animalMat[i][j]);
                }
                else {
                    s += Utilities.rightPadding(nameMap.get(animalMat[i][j]), 6);
                }
            }
            s += "\n";
        }
        return s;
    }

    public static int[][] detectAnimals(BufferedImage matchArea) {
        int w = matchArea.getWidth();

        int[][] animalMat = new int[8][8];
        int[] splitArray = new int[9];

        IntStream.range(0, 9).forEach(i -> {
            double val = ((w)/8.0) * i;
            splitArray[i] = (int) val;
        });
        double padDouble = splitArray[1] * 0.2;
        int pad = (int) padDouble;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                BufferedImage tile = matchArea.getSubimage(splitArray[j] + pad, splitArray[i] + pad,
                        (splitArray[j + 1] - splitArray[j] - 2 * pad), (splitArray[i + 1] - splitArray[i] - 2 * pad));

                int[] colorArr = new int[tile.getWidth() * tile.getHeight()];
                tile.getRGB(0, 0, tile.getWidth(), tile.getHeight(), colorArr, 0, tile.getWidth());

                for (int k = 0; k < tile.getWidth() * tile.getHeight(); k++) {
                    colorArr[k] = colorArr[k] & 0x00FFFFFF;
                }
                Set<Integer> colorSet = Arrays.stream(colorArr).boxed().collect(Collectors.toSet());

                for (int k = 0; k < uniqColorInt.length; k++) {
                    if (colorSet.contains(uniqColorInt[k])) {
                        animalMat[i][j] = nameLabel[k];
                        break;
                    }
                }
            }
        }
        return animalMat;
    }

    private static boolean cmpValues(int[][] animalMat, int val, int i, int j) {
        try {
            if (val == animalMat[i][j] && val != 0) {
                return true;
            }
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        return false;
    }

    private static void zeroIfMatch(int[][] animalMat, int val, int i, int j) {
        // Sets the matrix value to zero if value of animalMat[i][j] are the same as val
        if (cmpValues(animalMat, val, i, j)) {
            animalMat[i][j] = 0;
        }
    }

    private static void zeroIfMatch(int[][] animalMat, int val, int i, int j, boolean isHorizontal) {
        // Checks if the values above and below animalMat[i][j] are the same and sets it to zero
        // If isHorizontal flag is negative, checks for left and right instead

        if (isHorizontal) {
            if (cmpValues(animalMat, val, i + 1, j) && cmpValues(animalMat, val, i - 1, j)) {
                animalMat[i + 1][j] = 0;
                animalMat[i - 1][j] = 0;
                if (cmpValues(animalMat, val, i + 2, j)) {
                    animalMat[i + 2][j] = 0;
                }
                if (cmpValues(animalMat, val, i - 2, j)) {
                    animalMat[i - 2][j] = 0;
                }
            }
            if (cmpValues(animalMat, val, i - 1, j) && cmpValues(animalMat, val, i - 2, j)) {
                animalMat[i - 1][j] = 0;
                animalMat[i - 2][j] = 0;
            }
            if (cmpValues(animalMat, val, i + 1, j) && cmpValues(animalMat, val, i + 2, j)) {
                animalMat[i + 1][j] = 0;
                animalMat[i + 2][j] = 0;
            }
        } else {
            if (cmpValues(animalMat, val, i, j + 1) && cmpValues(animalMat, val, i, j - 1)) {
                animalMat[i][j + 1] = 0;
                animalMat[i][j - 1] = 0;
                if (cmpValues(animalMat, val, i, j + 2)) {
                    animalMat[i][j + 2] = 0;
                }
                if (cmpValues(animalMat, val, i, j + 2)) {
                    animalMat[i][j - 2] = 0;
                }
            }
            if (cmpValues(animalMat, val, i, j - 1) && cmpValues(animalMat, val, i, j - 2)) {
                animalMat[i][j - 1] = 0;
                animalMat[i][j - 2] = 0;
            }
            if (cmpValues(animalMat, val, i, j + 1) && cmpValues(animalMat, val, i, j + 2)) {
                animalMat[i][j + 1] = 0;
                animalMat[i][j + 2] = 0;
            }
        }
    }

    private static int gridToNum(int i, int j, boolean isHorizontal) {
        if (isHorizontal) {
            return (8 * (i)) + (j + 1);
        } else {
            return (8 * (j)) + (i + 1);
        }

    }

    private static void swapValues(int[][] animalMat, int i1, int j1, int i2, int j2) {
        int i = animalMat[i1][j1];
        animalMat[i1][j1] = animalMat[i2][j2];
        animalMat[i2][j2] = i;
    }

    public static int[][] getMoves(int[][] animalMat, int movesPerShot) {
        int[][] moves = new int[movesPerShot][2];
        int ctr = 0;
        int blk1 = 0;
        int blk2 = 0;
        int blk3 = 0;

        boolean isHorizontal = true;
        for (int round = 0; round < 2; round++) {
            if (round == 1) {
                isHorizontal = false;
                for(int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        if (j > i) {
                            swapValues(animalMat, i, j, j, i);
                        }
                    }
                }
            }
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 6; j++) {
                    if (ctr == movesPerShot) {
                        return moves;
                    }

                    blk1 = animalMat[i][j];
                    blk2 = animalMat[i][j + 1];
                    blk3 = animalMat[i][j + 2];

                    if (blk1 == 0) {
                        continue;
                    } else if (blk1 == blk2 && blk1 == blk3) {
                        animalMat[i][j] = 0;
                        animalMat[i][j + 1] = 0;
                        animalMat[i][j + 2] = 0;
                        zeroIfMatch(animalMat, blk1, i, j + 3);
                        zeroIfMatch(animalMat, blk1, i, j + 4);
                        zeroIfMatch(animalMat, blk1, i, j, true);
                        zeroIfMatch(animalMat, blk1, i, j + 1, true);
                        zeroIfMatch(animalMat, blk1, i, j + 2, true);
                        continue;
                    }

                    if (blk1 == blk2 && blk3 != 0) {
                        if (cmpValues(animalMat, blk1, i, j + 3)) {
                            setMat(animalMat, blk1, blk3, i, j, 0, 3);
                            moves[ctr][1] = gridToNum(i, j + 2, isHorizontal);
                            moves[ctr][0] = gridToNum(i, j + 3, isHorizontal);
                            ctr++;
                        } else if (cmpValues(animalMat, blk1, i - 1, j + 2)) {
                            setMat(animalMat, blk1, blk3, i, j, -1, 2);
                            moves[ctr][1] = gridToNum(i, j + 2, isHorizontal);
                            moves[ctr][0] = gridToNum(i - 1, j + 2, isHorizontal);
                            ctr++;
                        } else if (cmpValues(animalMat, blk1, i + 1, j + 2)) {
                            setMat(animalMat, blk1, blk3, i, j, 1, 2);
                            moves[ctr][1] = gridToNum(i, j + 2, isHorizontal);
                            moves[ctr][0] = gridToNum(i + 1, j + 2, isHorizontal);
                            ctr++;
                        }
                    } else if (blk2 == blk3) {
                        if (cmpValues(animalMat, blk2, i, j + 3)) {
                            animalMat[i][j + 1] = 0;
                            animalMat[i][j + 2] = 0;
                            animalMat[i][j + 3] = 0;
                            zeroIfMatch(animalMat, blk1, i, j + 4);
                            zeroIfMatch(animalMat, blk1, i, j + 5);
                            zeroIfMatch(animalMat, blk1, i, j + 1, true);
                            zeroIfMatch(animalMat, blk1, i, j + 2, true);
                            zeroIfMatch(animalMat, blk1, i, j + 3, true);
                        } else if (cmpValues(animalMat, blk2, i, j + 4)) {
                            setMat(animalMat, blk2, blk3, i, j + 1, 0, 3);
                            moves[ctr][1] = gridToNum(i, j + 3, isHorizontal);
                            moves[ctr][0] = gridToNum(i, j + 4, isHorizontal);
                            ctr++;
                        } else if (cmpValues(animalMat, blk2, i - 1, j)) {
                            setMat(animalMat, blk2, blk1, i, j, -1, 0);
                            moves[ctr][1] = gridToNum(i, j, isHorizontal);
                            moves[ctr][0] = gridToNum(i - 1, j, isHorizontal);
                            ctr++;
                        } else if (cmpValues(animalMat, blk2, i + 1, j)) {
                            setMat(animalMat, blk2, blk1, i, j, 1, 0);
                            moves[ctr][1] = gridToNum(i, j, isHorizontal);
                            moves[ctr][0] = gridToNum(i + 1, j, isHorizontal);
                            ctr++;
                        } else if (cmpValues(animalMat, blk2, i, j - 1)) {
                            setMat(animalMat, blk2, blk1, i, j, 0, -1);
                            moves[ctr][1] = gridToNum(i, j, isHorizontal);
                            moves[ctr][0] = gridToNum(i, j - 1, isHorizontal);
                            ctr++;
                        }
                    } else if (blk1 == blk3) {
                        if (cmpValues(animalMat, blk1, i - 1, j + 1)) {
                            setMat(animalMat, blk1, blk2, i, j, -1, 1);
                            zeroIfMatch(animalMat, blk1, i, j, false);
                            moves[ctr][1] = gridToNum(i, j + 1, isHorizontal);
                            moves[ctr][0] = gridToNum(i - 1, j + 1, isHorizontal);
                            ctr++;
                        } else if (cmpValues(animalMat, blk1, i + 1, j + 1)) {
                            setMat(animalMat, blk1, blk2, i, j, 1, 1);
                            moves[ctr][1] = gridToNum(i, j + 1, isHorizontal);
                            moves[ctr][0] = gridToNum(i + 1, j + 1, isHorizontal);
                            ctr++;
                        }
                    }
                }
            }
        }
        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (ctr == movesPerShot) {
                    return moves;
                }

                if (animalMat[i][j] == 10 || animalMat[i][j] == 11) {
                    moves[ctr][0] = gridToNum(i, j, isHorizontal);
                    moves[ctr][1] = gridToNum(i, j, isHorizontal);
                    ctr++;
                }
            }
        }

        return moves;
    }

    private static void setMat(int[][] animalMat, int val, int blk,int i, int j, int offI, int offJ) {

        animalMat[i + offI][j + offJ] = blk;

        if (offJ == 3) {
            zeroIfMatch(animalMat, val, i, j + 2, true);
            zeroIfMatch(animalMat, val, i, j + 2, false);
        } else if (offJ == -1 && offI == 0) {
            zeroIfMatch(animalMat, val, i, j, true);
            zeroIfMatch(animalMat, val, i, j, false);
        } else {
            zeroIfMatch(animalMat, val, i, j + offJ, true);
            zeroIfMatch(animalMat, val, i, j + offJ, false);
        }

        animalMat[i][j] = 0;
        animalMat[i][j + 1] = 0;
        animalMat[i][j + 2] = 0;
    }

}
