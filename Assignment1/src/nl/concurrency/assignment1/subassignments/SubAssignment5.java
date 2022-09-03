package nl.concurrency.assignment1.subassignments;

import nl.concurrency.assignment1.Sort;
import nl.concurrency.assignment1.model.RecursiveSplit;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class SubAssignment5 extends SubAssignment4 {
    private final ForkJoinPool pool;
    private RecursiveSplit recursiveSplit;

    public SubAssignment5(int numsToUse, String title, int timesToTest, int threshold) {
        super(numsToUse, title, timesToTest, threshold);
        if(amount%2 != 0) throw new IllegalArgumentException("Amount to generate cannot be an odd number!");
        pool = ForkJoinPool.commonPool();
    }

    @Override
    protected void start() {recursiveSplit = new RecursiveSplit(getRandomIntList(amount),threshold);}

    @Override
    protected void run() {
        pool.execute(recursiveSplit);
        try {
            int[] result = recursiveSplit.get();
            assert Sort.isSorted(result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
