package nl.concurrency.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import nl.concurrency.domain.Hotel;
import nl.concurrency.messages.RentARoomMessage;

import java.util.HashSet;
import java.util.Map;

public class HotelManager extends AbstractBehavior<RentARoomMessage> {
    private final Hotel managedHotel;
    public static final ServiceKey<RentARoomMessage> HOTELS_UPDATED;

    static {
        HOTELS_UPDATED = ServiceKey.create(RentARoomMessage.class, "hotels_updated");}

    public HotelManager(ActorContext<RentARoomMessage> context, Hotel hotel) {
        super(context);
        managedHotel = hotel;

        context.getSystem().receptionist().tell(Receptionist.register(HOTELS_UPDATED, context.getSelf()));
    }

    public static Behavior<RentARoomMessage> create(Hotel hotel) {
        return Behaviors.setup(context -> new HotelManager(context, hotel));
    }

    @Override
    public Receive<RentARoomMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(RentARoomMessage.RequestCreateHotelRoomReservationMessage.class, this::createReservation)
                .onMessage(RentARoomMessage.RequestCancelHotelRoomReservationMessage.class, this::cancelReservation)
                .onMessage(RentARoomMessage.Stop.class, s -> Behaviors.stopped())
                .build();
    }

    private Behavior<RentARoomMessage> createReservation(RentARoomMessage.RequestCreateHotelRoomReservationMessage message){
        HashSet<Integer> reservedRooms = new HashSet<>();
        for (int i = 1; i <= message.rooms; i++){
            for (Map.Entry<Integer, Boolean> entry : managedHotel.rooms.entrySet()){
                if (!entry.getValue()){
                    managedHotel.rooms.put(entry.getKey(),true);
                    reservedRooms.add(entry.getKey());
                    break;
                }
            }
        }
        Integer[] finalRooms = new Integer[reservedRooms.size()];
        reservedRooms.toArray(finalRooms);
        if (reservedRooms.size() != message.rooms){
            cancelRooms(finalRooms);
            message.sendTo.tell(new RentARoomMessage.SendHotelReservationData(false,null,getContext().getSelf()));
            return Behaviors.same();
        }
        message.sendTo.tell(new RentARoomMessage.SendHotelReservationData(true, finalRooms,getContext().getSelf()));
        return Behaviors.same();
    }

    private Behavior<RentARoomMessage> cancelReservation(RentARoomMessage.RequestCancelHotelRoomReservationMessage message){
        cancelRooms(message.rooms);
        return Behaviors.same();
    }

    private void cancelRooms(Integer[] roomsToCancel){
        for (int room : roomsToCancel){
            if (!managedHotel.rooms.get(room)) continue;
            managedHotel.rooms.put(room, false);
        }
    }
}
