package nl.concurrency.assignment1.model;

import nl.concurrency.assignment1.Sort;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

public abstract class Assignment {

    /**
     * Title of the assignment
     */
    protected String title;

    /**
     * Amount of numbers to put in a list
     */
    protected int amount;

    /**
     * List of measured execution times
     */
    private final ArrayList<Long> results;

    /**
     * Amount of times to run the same test
     */
    protected final int timesToTest;

    /**
     * One measured execution time
     */
    protected float timing;

    /**
     * File Writer
     */
    protected FileWriter csvWriter;

    /**
     * Name of the file
     */
    protected final String CSV_FILE_NAME = "results.csv";

    public Assignment(int numsToUse, String title, int timesToTest) {
        amount = numsToUse;
        this.title = title;
        if (timesToTest < 1) throw new IllegalArgumentException("Cannot test less than one times!");
        results = new ArrayList<>();
        this.timesToTest = timesToTest;
    }

    /**
     * Code to run as a setup before time measurement
     */
    protected abstract void start();

    /**
     * Execution time of the code to measure
     */
    protected abstract void run();

    /**
     * Generate an array of numbers without duplicates in a random order
     * @param amount Nums to generate
     * @return Nums from 0 up to Amount in random order
     */
    protected final int[] getRandomIntList(int amount){
        ArrayList<Integer> numList = new ArrayList<>();
        for (int i = 1; i <= amount; i++) {
            numList.add(i);
        }
        Collections.shuffle(numList);

        int[] result = new int[amount];
        for (int i = 0; i < amount; i++){
            result[i] = numList.get(i);
        }
        return result;
    }

    /**
     * Run the bubble sort algorithm on a list of numbers
     * @param list of sorted numbers using bubble sort
     */
    protected final void runBubbleSort(int[] list){
        Sort.bubbleSort(list);
    }

    /**
     * Measures the average execution time of a code
     */
    public final void measureTime(){
        System.out.println("> Starting benchmark: "+ title);
        for (int i = 0; i < timesToTest; i++){
            start();
            Instant timeStart = Instant.now();
            run();
            results.add(Duration.between(timeStart, Instant.now()).toMillis());
        }
        timing = 0f;

        results.remove(Collections.max(results));
        results.remove(Collections.min(results));

        results.forEach((e) -> timing += e);
        timing = timing / timesToTest;
        System.out.println("> Average time spent to finish after " + timesToTest + " occurrences: " + timing + "ms\n");
    }

    /**
     * Print the results to a file
     */
    public void print(){
        try {
            csvWriter = new FileWriter(CSV_FILE_NAME,true);

            csvWriter.append(title);
            csvWriter.append(",");
            csvWriter.append(Float.toString(timing));
            csvWriter.append(",");
            csvWriter.append(Integer.toString(timesToTest));
            csvWriter.append("\n");

            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
