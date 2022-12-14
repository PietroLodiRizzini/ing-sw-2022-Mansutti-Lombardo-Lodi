package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.model.Color;

import java.net.PortUnreachableException;
import java.util.stream.IntStream;

public enum ColorCli {
    ANSI_RED("\u001B[31m"),
    ANSI_GREEN("\u001B[32m"),
    ANSI_YELLOW("\u001B[33m"),
    ANSI_BLUE("\u001B[34m"),
    ANSI_PINK("\u001B[35m");

    private String escape;
    static final String RESET = "\u001B[0m";

    ColorCli(String escape)
    {
        this.escape = escape;
    }

    public String getEscape() {
        return escape;
    }

    @Override
    public String toString() {
        return escape;
    }

    /**
     * Prints circle
     * @param color color
     * @param quantity number of circle
     * @return string
     */
    public static String circles(Color color, int quantity) {
        final String[] circles = {""};

        ColorCli colorCli = ColorCli.valueOf("ANSI_"  + color);

        IntStream.rangeClosed(1, quantity).forEach(value -> circles[0] = circles[0].concat(colorCli + "⬤ " + ColorCli.RESET));

        return circles[0];
    }

    public static void printCircles(Color color, int quantity) {
        System.out.print(circles(color, quantity));
    }

    /**
     * Prints quantity of coins
     * @param quantity number of coins
     */
    public static void printCoins(int quantity) {
        IntStream.rangeClosed(1, quantity).forEach(i -> System.out.print("\uD83D\uDCB5 "));
    }

    /**
     * If the no entry tiles CC is extracted, prints quantity of available tiles
     * @param quantity number of available tiles
     * @return a string
     */
    public static String noEntryTiles(int quantity) {
        StringBuilder res = new StringBuilder("");

        for (int i = 0; i < quantity; i++) {
            res.append("🚫");
//            if(i != quantity - 1)
//                res.append(" ");
        }

        return res.toString();
    }
}
