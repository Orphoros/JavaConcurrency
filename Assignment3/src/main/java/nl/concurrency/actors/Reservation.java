package nl.concurrency.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import nl.concurrency.messages.RentARoomMessage;

import java.util.HashMap;
import java.util.Map;

public class Reservation extends AbstractBehavior<RentARoomMessage> {
    public static final ServiceKey<RentARoomMessage> RESERVATIONS_UPDATED;

    private final ActorRef<RentARoomMessage> parent;

    /**
     * Key -> ActorRef: Reference to hotel
     * Value -> HashSet(Integer): Reserved room numbers
     */
    private final HashMap<ActorRef<RentARoomMessage>, Integer[]> reservationData;

    private boolean isConfirmed;

    static {
        RESERVATIONS_UPDATED = ServiceKey.create(RentARoomMessage.class, "reservations_updated");}

    public Reservation(ActorContext<RentARoomMessage> context, ActorRef<RentARoomMessage> parent, HashMap<ActorRef<RentARoomMessage>, Integer[]> reservationData) {
        super(context);
        this.parent = parent;
        this.reservationData = reservationData;
        this.isConfirmed = false;

        for (ActorRef<RentARoomMessage> hotel : reservationData.keySet()) getContext().watch(hotel);

        if(!parent.path().name().contains("Temp")) context.getSystem().receptionist().tell(Receptionist.register(RESERVATIONS_UPDATED, context.getSelf()));
    }

    public static Behavior<RentARoomMessage> create(ActorRef<RentARoomMessage> parent, HashMap<ActorRef<RentARoomMessage>, Integer[]> reservationData) {
        return Behaviors.setup(context -> new Reservation(context, parent, reservationData));
    }

    @Override
    public Receive<RentARoomMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(RentARoomMessage.RequestConfirmReservationMessage.class, this::confirmReservation)
                .onMessage(RentARoomMessage.RequestCancelReservationMessage.class, this::cancelReservation)
                .onMessage(RentARoomMessage.Stop.class, s -> Behaviors.stopped())
                .onSignal(Terminated.class, this::selfValidate)
                .build();
    }

    private Behavior<RentARoomMessage> confirmReservation(RentARoomMessage.RequestConfirmReservationMessage message){
        isConfirmed = true;
        if (message.sender != null) message.sender.tell(new RentARoomMessage.ResponseToUser(true,null));
        return Behaviors.same();
    }

    private Behavior<RentARoomMessage> cancelReservation(RentARoomMessage.RequestCancelReservationMessage message){
        executeReservationCancel(reservationData);
        if (message.sender != null) message.sender.tell(new RentARoomMessage.ResponseToUser(true,null));
        parent.tell(new RentARoomMessage.RequestSelfStopMessage(getContext().getSelf()));
        return Behaviors.same();
    }

    private Behavior<RentARoomMessage> selfValidate(Terminated terminated){
        reservationData.remove(terminated.getRef());
        executeReservationCancel(reservationData);
        parent.tell(new RentARoomMessage.RequestSelfStopMessage(getContext().getSelf()));
        return Behaviors.same();
    }

    private void executeReservationCancel(HashMap<ActorRef<RentARoomMessage>, Integer[]> data){
        for (Map.Entry<ActorRef<RentARoomMessage>, Integer[]> entry : data.entrySet()) {
            entry.getKey().tell(new RentARoomMessage.RequestCancelHotelRoomReservationMessage(entry.getValue()));
        }
    }
}
