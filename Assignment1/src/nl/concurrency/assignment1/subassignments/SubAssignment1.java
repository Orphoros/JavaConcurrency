package nl.concurrency.assignment1.subassignments;

import nl.concurrency.assignment1.Sort;
import nl.concurrency.assignment1.model.Assignment;

public class SubAssignment1 extends Assignment {

    public SubAssignment1(int numsToUse, String title, int timesToTest) {
        super(numsToUse, title, timesToTest);
    }

    int[] numbers = new int[amount];

    @Override
    protected void start(){
        numbers = getRandomIntList(amount);
    }

    @Override
    protected void run() {
        runBubbleSort(numbers);
        assert Sort.isSorted(numbers);
    }


}
