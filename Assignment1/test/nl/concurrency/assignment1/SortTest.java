package nl.concurrency.assignment1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SortTest {


    @Test
    public void testMerge() {
        int left[] = {2,5,12,89};
        int right[] = {3,7,9,14,17,102};
        int result[] = {2,3,5,7,9,12,14,17,89,102};
        int[] merged = Sort.merge(left,right);
        assertArrayEquals(merged,result);
    }


    @Test
    public void testSort() {
        int[] unsorted =  {72,2,18,3,88,45,23};
        assertFalse(Sort.isSorted(unsorted));
        Sort.bubbleSort(unsorted);
        assertTrue(Sort.isSorted(unsorted));
    }


}