package nl.concurrency.assignment1.model;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Benchmark {
    /**
     * Run the time measurements for each assignment
     * @param assignments List of all sub-assignments
     * @param codeToRunToTest Code to run to measure time
     * @param codeToRunAfterExecution Code to run after the time has been measured
     */
    public static void runTimeMeasurement(ArrayList<Assignment> assignments, Consumer<Assignment> codeToRunToTest, Consumer<Assignment> codeToRunAfterExecution){
        for (Assignment a : assignments) {
            codeToRunToTest.accept(a);
            codeToRunAfterExecution.accept(a);
        }
    }
}
