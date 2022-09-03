package nl.concurrency.asign2;

import java.util.Random;

public abstract class BaseVisitor extends Thread{
    final protected ClubExtreem clubExtreem;
    final protected Random random = new Random();

    public BaseVisitor(ClubExtreem clubExtreem, String name) {
        super(name);
        this.clubExtreem = clubExtreem;
    }

    @Override
    public void run() {
        while(true) {
            live();
        }
    }

    protected abstract void live();

    protected final void randomTime(int max, int min) {
        try {
            Thread.sleep(min + (random.nextInt((max - min) / 100) * 100) );
        } catch ( InterruptedException e) {
            e.printStackTrace();
        }
    }
}
