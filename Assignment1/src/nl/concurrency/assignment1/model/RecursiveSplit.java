package nl.concurrency.assignment1.model;

import nl.concurrency.assignment1.Sort;

import java.util.Arrays;
import java.util.concurrent.RecursiveTask;

public class RecursiveSplit extends RecursiveTask<int[]> {

    private final int[] numbers;
    private final int threshold;

    public RecursiveSplit(int[] numbers, int threshold) {
        this.numbers = numbers;
        this.threshold = threshold;
    }

    @Override
    protected int[] compute() {
        if (numbers.length > threshold){
            int[] left = Arrays.copyOfRange(numbers,0,numbers.length/2);
            int[] right = Arrays.copyOfRange(numbers,numbers.length/2,numbers.length);
            RecursiveSplit leftSplit = new RecursiveSplit(left,threshold);
            RecursiveSplit rightSplit = new RecursiveSplit(right,threshold);
            leftSplit.fork();
            rightSplit.fork();
            int[] leftAnswer = Arrays.stream(leftSplit.join()).toArray();
            int[] rightAnswer = Arrays.stream(rightSplit.join()).toArray();
            return (Arrays.stream(Sort.merge(leftAnswer, rightAnswer)).toArray());
        }else{
            Sort.bubbleSort(numbers);
            return Arrays.stream(numbers).toArray();
        }
    }
}
