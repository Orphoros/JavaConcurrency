package nl.concurrency.assignment1;

import nl.concurrency.assignment1.model.Assignment;
import nl.concurrency.assignment1.model.Benchmark;
import nl.concurrency.assignment1.subassignments.*;

import java.util.ArrayList;

public class Main {

    /**
     * List of all sub-assignments
     */
    private static final ArrayList<Assignment> assignments = new ArrayList<>();

    /**
     * List of amounts to test
     */
    private static final int[] amountOfNumsToUse = {25_000, 50_000, 100_000, 200_000, 400_000};

    /**
     * List of threshold to use
     */
    private static final int[] thresholdsToUse = {15, 60, 200, 500, 2_000, 5_000};

    /**
     * Times to run the same test to get an average
     */
    private static final int timesToTest = 10;

    public static void main(String[] args) {
        for (int i: amountOfNumsToUse) {
            assignments.add(new SubAssignment1(i,"Assignment1-" + i + "nums", timesToTest));
            assignments.add(new SubAssignment2(i,"Assignment2-" + i + "nums", timesToTest));
            assignments.add(new SubAssignment3(i,"Assignment3-" + i + "nums", timesToTest));
            for(int t: thresholdsToUse) {
                assignments.add(new SubAssignment4(i,"Assignment4-" + i + "nums-threshold" + t, timesToTest, t));
                assignments.add(new SubAssignment5(i,"Assignment5-" + i + "nums-threshold" + t, timesToTest, t));
            }
        }
        Benchmark.runTimeMeasurement(assignments, Assignment::measureTime, Assignment::print);
    }
}
