package nl.concurrency.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import nl.concurrency.domain.Hotel;
import nl.concurrency.messages.RentARoomMessage;

import java.util.*;

public class Agent extends AbstractBehavior<RentARoomMessage> {
    public static final ServiceKey<RentARoomMessage> CREATE_AGENT;

    /**
     * Key -> String: Name of hotel
     * Value -> ActorRef: Reference to the HotelManager
     */
    private final HashMap<String, ActorRef<RentARoomMessage>> managerList = new HashMap<>();

    /**
     * Key -> String: UUID of reservation
     * Value -> ActorRef: Reference to the Reservation
     */
    private final HashMap<String, ActorRef<RentARoomMessage>> reservationList = new HashMap<>();

    static {CREATE_AGENT = ServiceKey.create(RentARoomMessage.class, "create_agent");}

    public Agent(ActorContext<RentARoomMessage> context) {
        super(context);
        ActorRef<Receptionist.Listing> listingAdapter = context.messageAdapter(
                Receptionist.Listing.class,
                RentARoomMessage.ReceiveListing::new
        );

        context.getSystem().receptionist().tell(
                Receptionist.subscribe(
                        HotelManager.HOTELS_UPDATED, listingAdapter
                )
        );
        context.getSystem().receptionist().tell(
                Receptionist.subscribe(
                        Reservation.RESERVATIONS_UPDATED, listingAdapter
                )
        );

        context.getSystem().receptionist().tell(Receptionist.register(CREATE_AGENT, context.getSelf()));
    }

    public static Behavior<RentARoomMessage> create() {
        return Behaviors.setup(Agent::new);
    }

    @Override
    public Receive<RentARoomMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(RentARoomMessage.RequestListHotelsMessage.class, this::listHotels)
                .onMessage(RentARoomMessage.RequestCreateHotelMessage.class, this::createHotel)
                .onMessage(RentARoomMessage.RequestDeleteHotelMessage.class, this::deleteHotel)
                .onMessage(RentARoomMessage.RequestCreateReservationMessage.class, this::generateReservations)
                .onMessage(RentARoomMessage.RequestReservationActorCreationMessage.class, this::createReservationActor)
                .onMessage(RentARoomMessage.RequestCancelReservationMessage.class, this::cancelReservation)
                .onMessage(RentARoomMessage.RequestConfirmReservationMessage.class, this::confirmReservation)
                .onMessage(RentARoomMessage.ReceiveListing.class, this::parseUpdate)
                .onMessage(RentARoomMessage.RequestSelfStopMessage.class, this::executeChildStop)
                .build();
    }

    private Behavior<RentARoomMessage> listHotels(RentARoomMessage.RequestListHotelsMessage message){
        if (managerList.isEmpty()){
            message.sender.tell(new RentARoomMessage.ResponseToUser(false, null));
            return Behaviors.same();
        }
        String hotelList = "Hotels:\n"+managerList.keySet();
        message.sender.tell(new RentARoomMessage.ResponseToUser(true,hotelList));
        return Behaviors.same();
    }

    private Behavior<RentARoomMessage> createHotel(RentARoomMessage.RequestCreateHotelMessage message){
        if (message.roomCount <= 0){
            message.sender.tell(new RentARoomMessage.ResponseToUser(false, "A hotel cannot have < 1 room!"));
            return Behaviors.same();
        }
        if (managerList.containsKey(message.hotelName)) {
            message.sender.tell(new RentARoomMessage.ResponseToUser(false, "Hotel already exists!"));
            return Behaviors.same();
        }
        Hotel newHotel = new Hotel(message.hotelName, message.roomCount);
        ActorRef<RentARoomMessage> hotelManager = getContext().spawn(HotelManager.create(newHotel),newHotel.name);
        managerList.put(newHotel.name, hotelManager);
        message.sender.tell(new RentARoomMessage.ResponseToUser(true, newHotel.name));
        return Behaviors.same();
    }

    private Behavior<RentARoomMessage> deleteHotel(RentARoomMessage.RequestDeleteHotelMessage message){
        if (managerList.get(message.hotelID) == null){
            message.sender.tell(new RentARoomMessage.ResponseToUser(false, "Hotel does not exist"));
            return Behaviors.same();
        }
        managerList.get(message.hotelID).tell(new RentARoomMessage.Stop());
        managerList.remove(message.hotelID);
        message.sender.tell(new RentARoomMessage.ResponseToUser(true, null));
        return Behaviors.same();
    }

    private Behavior<RentARoomMessage> createReservationActor(RentARoomMessage.RequestReservationActorCreationMessage message){
        String reservationID = "Reservation--"+ UUID.randomUUID();
        ActorRef<RentARoomMessage> reservation = getContext().spawn(Reservation.create(getContext().getSelf(),message.reservedRooms), reservationID);
        reservationList.put(reservationID, reservation);
        message.originalRequestSource.tell(new RentARoomMessage.ResponseToUser(true,reservationID));
        return Behaviors.same();
    }

    private Behavior<RentARoomMessage> generateReservations(RentARoomMessage.RequestCreateReservationMessage message){
        ActorRef<RentARoomMessage> aggregator = getContext().spawn(ReservationAggregator.create(message.sender,getContext().getSelf(),message.requestRooms.size()),"TempAggr--"+UUID.randomUUID());
        for (Map.Entry<String, Integer> entry : message.requestRooms.entrySet()){
            ActorRef<RentARoomMessage> selectManager = managerList.get(entry.getKey());
            if (selectManager == null){
                message.sender.tell(new RentARoomMessage.ResponseToUser(false, "Incorrect hotel name: "+entry.getKey()));
                aggregator.tell(new RentARoomMessage.RequestAbortAggregationMessage());
                return Behaviors.same();
            }
            selectManager.tell(new RentARoomMessage.RequestCreateHotelRoomReservationMessage(aggregator, entry.getValue()));
        }
        return Behaviors.same();
    }

    private Behavior<RentARoomMessage> cancelReservation(RentARoomMessage.RequestCancelReservationMessage message){
        ActorRef<RentARoomMessage> selectReservation = reservationList.get(message.reservationID);
        if (selectReservation == null){
            message.sender.tell(new RentARoomMessage.ResponseToUser(false, "The reservation does not exist!"));
            return Behaviors.same();
        }
        selectReservation.tell(new RentARoomMessage.RequestCancelReservationMessage(message.sender,null));
        reservationList.remove(message.reservationID);
        return Behaviors.same();
    }

    private Behavior<RentARoomMessage> confirmReservation(RentARoomMessage.RequestConfirmReservationMessage message){
        ActorRef<RentARoomMessage> selectReservation = reservationList.get(message.reservationID);
        if (selectReservation == null){
            message.sender.tell(new RentARoomMessage.ResponseToUser(false, "The reservation does not exist!"));
            return Behaviors.same();
        }
        selectReservation.tell(new RentARoomMessage.RequestConfirmReservationMessage(message.sender, null));
        return Behaviors.same();
    }

    private Behavior<RentARoomMessage> parseUpdate(RentARoomMessage.ReceiveListing message){
        Set<ActorRef<RentARoomMessage>> newData;
        if (message.listingActors.isForKey(Reservation.RESERVATIONS_UPDATED)) {
            newData = message.listingActors.getServiceInstances(Reservation.RESERVATIONS_UPDATED);

            if (newData.isEmpty()) reservationList.clear();
            else updateData(newData, reservationList);
        }
        else {
            newData = message.listingActors.getServiceInstances(HotelManager.HOTELS_UPDATED);

            if (newData.isEmpty()){
                managerList.clear();
                reservationList.clear();
            }
            else updateData(newData, managerList);
        }
        return Behaviors.same();
    }

    private Behavior<RentARoomMessage> executeChildStop(RentARoomMessage.RequestSelfStopMessage message){
        if (reservationList.containsValue(message.sender)) reservationList.remove(message.sender.path().name());
        if (managerList.containsValue(message.sender)) managerList.remove(message.sender.path().name());
        getContext().stop(message.sender);
        return Behaviors.same();
    }

    private void updateData(Set<ActorRef<RentARoomMessage>> newData, HashMap<String, ActorRef<RentARoomMessage>> oldData){
        for (ActorRef<RentARoomMessage> actor : newData){
            if (!oldData.containsKey(actor.path().name())) oldData.put(actor.path().name(),actor);
        }
        for (Map.Entry<String, ActorRef<RentARoomMessage>> entry : new HashSet<>(oldData.entrySet())){
            if (!newData.contains(entry.getValue())) oldData.remove(entry.getKey());
        }
    }
}
