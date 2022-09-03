package nl.concurrency.asign2;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ClubExtreem{

    private final int NR_OF_MAX_VISITORS, NR_OF_MAX_CONSECUTIVE_REPRESENTATIVES;

    private final ReentrantLock lock;

    private final Condition visitorCanEnter;
    private final Condition representativeCanEnter;
    private final Condition lowPriorityVisitorCanEnter;
    private final Condition visitorEntered;
    private final Condition lowPriorityVisitorLeftQueue;

    private boolean representativeInClub;
    private boolean lowPriorityVisitorHasToWait;
    private int nrOfRepresentativesWaiting;
    private int nrOfVisitorsInClub;
    private int nrOfConsecutiveRepresentatives;
    private int nrOfVisitorsWaiting;
    private int nrOfLowPriorityVisitorsWaiting;

    public ClubExtreem(int sizeOfTheClub, int consecutiveRepresentatives) {
        this.NR_OF_MAX_VISITORS = sizeOfTheClub;
        this.NR_OF_MAX_CONSECUTIVE_REPRESENTATIVES = consecutiveRepresentatives;

        this.lock = new ReentrantLock();

        this.visitorCanEnter = lock.newCondition();
        this.representativeCanEnter = lock.newCondition();
        this.lowPriorityVisitorCanEnter = lock.newCondition();
        this.visitorEntered = lock.newCondition();
        this.lowPriorityVisitorLeftQueue = lock.newCondition();

        representativeInClub = false;
        lowPriorityVisitorHasToWait = true;
        nrOfRepresentativesWaiting = 0;
        nrOfVisitorsInClub = 0;
        nrOfConsecutiveRepresentatives = 0;
        nrOfVisitorsWaiting = 0;
        nrOfLowPriorityVisitorsWaiting = 0;
    }

    /**
     * Enter the club
     * @param visitor Visitor to queue in
     */
    public void enter(Visitor visitor){
        lock.lock();
        try {
            if(nrOfConsecutiveRepresentatives == NR_OF_MAX_CONSECUTIVE_REPRESENTATIVES){
                System.out.printf("> Low priority %s is waiting to get into the club!\n", visitor.getName());
                nrOfLowPriorityVisitorsWaiting++;
                while (lowPriorityVisitorHasToWait){
                    lowPriorityVisitorCanEnter.await();
                }
                nrOfLowPriorityVisitorsWaiting--;
                lowPriorityVisitorLeftQueue.signal();
            }
            System.out.printf("> %s is waiting to get into the club!\n", visitor.getName());
            nrOfVisitorsWaiting++;
            while (nrOfVisitorsInClub == NR_OF_MAX_VISITORS || (nrOfRepresentativesWaiting > 0 && nrOfConsecutiveRepresentatives < NR_OF_MAX_CONSECUTIVE_REPRESENTATIVES) || representativeInClub){
                visitorCanEnter.await();
            }
            assert nrOfRepresentativesWaiting == 0
                    || (nrOfRepresentativesWaiting > 0 && nrOfConsecutiveRepresentatives == NR_OF_MAX_CONSECUTIVE_REPRESENTATIVES);
            assert nrOfVisitorsInClub < NR_OF_MAX_VISITORS;

            nrOfVisitorsWaiting--;
            nrOfVisitorsInClub++;
            visitorEntered.signal();
            System.out.printf("\033[0;32m> %s entered the club!\033[0m\n", visitor.getName());
            logClubStatus();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Enter the club
     * @param representative Representative to queue in
     */
    public void enter(Representative representative){
        lock.lock();
        try {
            System.out.printf("\u001B[35m> %s is waiting to get into the club!\033[0m\n", representative.getName());
            nrOfRepresentativesWaiting++;
            while (nrOfVisitorsInClub > (NR_OF_MAX_VISITORS/2) || representativeInClub || nrOfConsecutiveRepresentatives == NR_OF_MAX_CONSECUTIVE_REPRESENTATIVES){
                representativeCanEnter.await();
            }
            assert (nrOfVisitorsInClub <= (NR_OF_MAX_VISITORS / 2)) && nrOfConsecutiveRepresentatives < NR_OF_MAX_CONSECUTIVE_REPRESENTATIVES;
            nrOfRepresentativesWaiting--;
            representativeInClub = true;
            nrOfConsecutiveRepresentatives++;
            System.out.printf("\u001B[36m> %s entered the club!\033[0m\n", representative.getName());
            logClubStatus();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Leave the club
     * @param visitor Visitor to let out
     */
    public void leave(Visitor visitor){
        lock.lock();
        try{
            nrOfVisitorsInClub--;
            System.out.printf("\033[0;33m> %s left the club\033[0m\n", visitor.getName());
            logClubStatus();
            if(nrOfConsecutiveRepresentatives < NR_OF_MAX_CONSECUTIVE_REPRESENTATIVES && nrOfRepresentativesWaiting > 0) {
                representativeCanEnter.signal();
            }else visitorCanEnter.signal();
        }finally {
            lock.unlock();
        }
    }

    /**
     * Leave the club
     * @param representative Representative to let out
     */
    public void leave(Representative representative){
        lock.lock();
        try{
            representativeInClub = false;
            System.out.printf("\u001B[36m> %s left the club\033[0m\n", representative.getName());
            logClubStatus();
            if(nrOfConsecutiveRepresentatives < NR_OF_MAX_CONSECUTIVE_REPRESENTATIVES && nrOfRepresentativesWaiting > 0) {
                representativeCanEnter.signal();
            }else if(nrOfVisitorsWaiting > 0){
                assert !representativeInClub;
                System.out.println("\u001B[34m[LOG]: Last Representative left after reaching max consecutive representatives, letting in all visitors.\033[0m");
                while (nrOfVisitorsWaiting > 0) {
                    visitorCanEnter.signal();
                    visitorEntered.await();
                }

                assert nrOfVisitorsWaiting == 0;

                nrOfConsecutiveRepresentatives = 0;
                System.out.println("\u001B[34m[LOG]: All visitors entered. nrOfConsecutiveRepresentatives has been reset.\033[0m");
                System.out.println("\u001B[34m[LOG]: Signalling all 'low priority' visitors that they can become 'normal' visitors.\033[0m");
                lowPriorityVisitorHasToWait = false;
                while (nrOfLowPriorityVisitorsWaiting > 0) {
                    lowPriorityVisitorCanEnter.signal();
                    lowPriorityVisitorLeftQueue.await();
                }
                assert nrOfLowPriorityVisitorsWaiting == 0;
                lowPriorityVisitorHasToWait = true;
                System.out.println("\u001B[34m[LOG]: All 'low priority' visitors have left the low priority queue. Representatives can enter.\033[0m");
                representativeCanEnter.signal();
            } else {
                nrOfConsecutiveRepresentatives = 0;
                assert nrOfVisitorsWaiting == 0;
                representativeCanEnter.signal();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Print state variables to the screen to debug the code
     */
    private void logClubStatus(){
        System.out.printf("\u001B[34m[DEBUG]: nrOfVisitorsInClub=%d representativeWaits=%b nrConsecutiveRepresentatives=%d waitingVisitors=%d lpWaitingVisitors=%d \033[0m\n",nrOfVisitorsInClub, nrOfRepresentativesWaiting > 0, nrOfConsecutiveRepresentatives, nrOfVisitorsWaiting, nrOfLowPriorityVisitorsWaiting);
    }
}
