package nl.concurrency.assignment1.subassignments;

import nl.concurrency.assignment1.Sort;

public class SubAssignment3 extends SubAssignment2 {
    public SubAssignment3(int numsToUse, String title, int timesToTest) {
        super(numsToUse, title, timesToTest);
        if(amount%2 != 0) throw new IllegalArgumentException("Amount to generate cannot an be odd number!");
    }

    @Override
    protected void run() {
        Thread t1 = new Thread(() -> runBubbleSort(numbersFirstHalf));
        Thread t2 = new Thread(() -> runBubbleSort(numbersSecondHalf));
        t1.start();
        t2.start();
        try{
            t1.join();
            t2.join();
        }catch (InterruptedException e){e.printStackTrace();}
        int[] result = Sort.merge(numbersFirstHalf, numbersSecondHalf);
        assert Sort.isSorted(result);
    }
}
