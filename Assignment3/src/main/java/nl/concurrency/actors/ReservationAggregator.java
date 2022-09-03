package nl.concurrency.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import nl.concurrency.messages.RentARoomMessage;

import java.util.HashMap;
import java.util.UUID;

public class ReservationAggregator extends AbstractBehavior<RentARoomMessage> {
    private final ActorRef<RentARoomMessage> requestSource, parent;
    private final int expectedReservations;
    HashMap<ActorRef<RentARoomMessage>, Integer[]> reservedRooms = new HashMap<>();

    public ReservationAggregator(ActorContext<RentARoomMessage> context, ActorRef<RentARoomMessage> requestSource, ActorRef<RentARoomMessage> parent, int expectedReservations) {
        super(context);

        this.requestSource = requestSource;
        this.parent = parent;
        this.expectedReservations = expectedReservations;
    }

    public static Behavior<RentARoomMessage> create(ActorRef<RentARoomMessage> requestSource, ActorRef<RentARoomMessage> parent, int expectedReservations) {
        return Behaviors.setup(context -> new ReservationAggregator(context, requestSource, parent, expectedReservations));
    }

    @Override
    public Receive<RentARoomMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(RentARoomMessage.SendHotelReservationData.class, this::addReservation)
                .onMessage(RentARoomMessage.RequestAbortAggregationMessage.class, this::abortAggregation)
                .onMessage(RentARoomMessage.RequestSelfStopMessage.class, this::cancelledReservationStop)
                .build();
    }

    private Behavior<RentARoomMessage> addReservation(RentARoomMessage.SendHotelReservationData message){
        if (!message.status){
            if (!reservedRooms.isEmpty()) {
                ActorRef<RentARoomMessage> canceledReservation = getContext().spawn(Reservation.create(getContext().getSelf(),reservedRooms), UUID.randomUUID().toString());
                canceledReservation.tell(new RentARoomMessage.RequestCancelReservationMessage(getContext().getSelf(), null));
            }
            requestSource.tell(new RentARoomMessage.ResponseToUser(false,"Reservation declined by hotel"));
            return Behaviors.same();
        }
        reservedRooms.put(message.sender, message.rooms);
        if (reservedRooms.size() == expectedReservations){
            parent.tell(new RentARoomMessage.RequestReservationActorCreationMessage(reservedRooms,requestSource));
            parent.tell(new RentARoomMessage.RequestSelfStopMessage(getContext().getSelf()));
        }
        return Behaviors.same();
    }

    private Behavior<RentARoomMessage> abortAggregation(RentARoomMessage.RequestAbortAggregationMessage message){
        ActorRef<RentARoomMessage> canceledReservation = getContext().spawn(Reservation.create(getContext().getSelf(),reservedRooms), UUID.randomUUID().toString());
        canceledReservation.tell(new RentARoomMessage.RequestCancelReservationMessage(null,null));
        return Behaviors.same();
    }

    private Behavior<RentARoomMessage> cancelledReservationStop(RentARoomMessage.RequestSelfStopMessage message){
        getContext().stop(message.sender);
        parent.tell(new RentARoomMessage.RequestSelfStopMessage(getContext().getSelf()));
        return Behaviors.same();
    }


}
