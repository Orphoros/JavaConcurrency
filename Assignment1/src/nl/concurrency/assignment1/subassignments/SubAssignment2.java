package nl.concurrency.assignment1.subassignments;

import nl.concurrency.assignment1.Sort;
import nl.concurrency.assignment1.model.Assignment;

public class SubAssignment2 extends Assignment {
    public SubAssignment2(int numsToUse, String title, int timesToTest) {
        super(numsToUse, title, timesToTest);
        if(amount%2 != 0) throw new IllegalArgumentException("Amount to generate cannot an be odd number!");
    }

    protected int[] numbersFirstHalf = new int[amount/2];
    protected int[] numbersSecondHalf = new int[amount/2];

    @Override
    protected void start() {
        int[] allNums = getRandomIntList(amount);
        for (int i = 0; i < amount; i++) {
            if (i < (amount/2) ){
                numbersFirstHalf[i] = allNums[i];
            }else {
                numbersSecondHalf[i-amount/2] = allNums[i];
            }
        }
    }

    @Override
    protected void run() {
        runBubbleSort(numbersFirstHalf);
        runBubbleSort(numbersSecondHalf);
        int[] result = Sort.merge(numbersFirstHalf, numbersSecondHalf);
        assert Sort.isSorted(result);
    }
}
