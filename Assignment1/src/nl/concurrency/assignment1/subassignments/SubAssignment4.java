package nl.concurrency.assignment1.subassignments;

import nl.concurrency.assignment1.Sort;
import nl.concurrency.assignment1.model.Node;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class SubAssignment4 extends SubAssignment2 {
    protected final int threshold;
    private Node root;

    public SubAssignment4(int numsToUse, String title, int timesToTest, int threshold) {
        super(numsToUse, title, timesToTest);
        this.threshold = threshold;
        if(amount%2 != 0) throw new IllegalArgumentException("Amount to generate cannot be an odd number!");
    }

    @Override
    protected void start() {
        root = new Node(getRandomIntList(amount), null, null);
    }

    @Override
    protected void run() {
        smartSplit(root);
        int[] result = root.getNums();
        assert Sort.isSorted(result);
    }

    private void smartSplit(Node node){
        if (node.getNums().length > threshold){
            node.setLeftLeaf(new Node(Arrays.copyOfRange(node.getNums(),0,node.getNums().length/2), null, null, new Thread(() -> smartSplit(node.getLeftLeaf()))));
            node.setRightLeaf(new Node(Arrays.copyOfRange(node.getNums(),node.getNums().length/2,node.getNums().length), null, null, new Thread(() -> smartSplit(node.getRightLeaf()))));

            node.getLeftLeaf().getNodeThread().start();
            node.getRightLeaf().getNodeThread().start();
            try{
                node.getLeftLeaf().getNodeThread().join();
                node.getRightLeaf().getNodeThread().join();
            }catch (InterruptedException e){e.printStackTrace();}
            node.setNums(Sort.merge(node.getLeftLeaf().getNums(), node.getRightLeaf().getNums()));
            node.setRightLeaf(null);
            node.setLeftLeaf(null);
        } else {
            runBubbleSort(node.getNums());
        }
    }

    @Override
    public void print() {
        try {
            csvWriter = new FileWriter(CSV_FILE_NAME,true);

            csvWriter.append(title);
            csvWriter.append(",");
            csvWriter.append(Float.toString(timing));
            csvWriter.append(",");
            csvWriter.append(Integer.toString(timesToTest));
            csvWriter.append(",");
            csvWriter.append(Integer.toString(threshold));
            csvWriter.append("\n");

            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
