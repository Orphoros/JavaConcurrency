package nl.concurrency.assignment1.model;

/**
 * Node to build a tree
 */
public class Node {
    /**
     * Numbers that the node needs to sort
     */
    private int[] nums;

    /**
     * Thread that is responsible for the numbers
     */
    private Thread nodeThread;

    /**
     * Left child
     */
    private Node leftLeaf;

    /**
     * Right child
     */
    private Node rightLeaf;

    public Node(int[] nums, Node leftLeaf, Node rightLeaf, Thread nodeThread) {
        this.nums = nums;
        this.leftLeaf = leftLeaf;
        this.rightLeaf = rightLeaf;
        this.nodeThread = nodeThread;
    }

    public Node(int[] nums, Node leftLeaf, Node rightLeaf) {
        this(nums, leftLeaf, rightLeaf, null);
    }

    public int[] getNums() {
        return nums;
    }

    public Node getLeftLeaf() {
        return leftLeaf;
    }

    public Node getRightLeaf() {
        return rightLeaf;
    }

    public void setNums(int[] nums) {
        this.nums = nums;
    }

    public void setLeftLeaf(Node leftLeaf) {
        this.leftLeaf = leftLeaf;
    }

    public void setRightLeaf(Node rightLeaf) {
        this.rightLeaf = rightLeaf;
    }

    public Thread getNodeThread() {
        return nodeThread;
    }

    public void setNodeThread(Thread nodeThread) {
        this.nodeThread = nodeThread;
    }
}
