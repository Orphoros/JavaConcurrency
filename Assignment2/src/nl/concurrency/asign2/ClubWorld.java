package nl.concurrency.asign2;

import java.util.Scanner;

public class ClubWorld {
    private final int
            sizeOfTheClub               = 5,
            nrOfRepresentatives         = 10,
            nrOfVisitors                = 10,
            consecutiveRepresentatives  = 3;

    public static void main(String[] args) {
        new ClubWorld().startWorld();
    }

    public void startWorld() {

        ClubExtreem clubExtreem = new ClubExtreem(sizeOfTheClub, consecutiveRepresentatives);
        for (int i = 0; i < nrOfVisitors; i++) {
            new Thread(new Visitor(clubExtreem,"Visitor " + i),"Visitor" + i ).start();
        }
        for (int i = 0; i < nrOfRepresentatives; i++) {
            new Thread(new Representative(clubExtreem,"Representative " + i),"Representative" + i ).start();
        }

        Scanner scan = new Scanner(System.in);
        System.out.println("Press Enter to quit...");
        scan.nextLine();
        System.exit(0);
    }
}