/**
 * This opens a command line and runs some other class in the jar
 *
 * @author Brandon Barajas
 */

import java.io.*;
import java.awt.GraphicsEnvironment;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Queue<Integer> x = new PriorityQueue<>();
        x.add(1);
        x.add(2);
        System.out.println(x);
    }
}