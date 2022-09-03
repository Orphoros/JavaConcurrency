package nl.concurrency.asign2;

public class Visitor extends BaseVisitor {

    public Visitor(ClubExtreem club, String name) {
        super(club, name);
    }

    @Override
    protected void live() {
        clubExtreem.enter(this);

        //Time to spend in the club
        randomTime(30000,random.nextInt(30) * 100);

        clubExtreem.leave(this);

        //Time to wait till re-enter
        randomTime(60000,random.nextInt(60) * 100);
    }
}
