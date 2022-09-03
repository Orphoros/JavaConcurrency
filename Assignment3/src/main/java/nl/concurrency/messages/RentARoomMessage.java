package nl.concurrency.messages;

import akka.actor.typed.ActorRef;
import akka.actor.typed.receptionist.Receptionist;

import java.io.Serializable;
import java.util.HashMap;

public interface RentARoomMessage extends Serializable {

    /**
     * Class for a general status responses to the user
     */
    class ResponseToUser implements RentARoomMessage {
        public final Boolean status;
        public final String stringMessage;


        public ResponseToUser(Boolean status, String stringMessage) {
            this.status = status;
            this.stringMessage = stringMessage;
        }
    }

    /**
     *  Ask for a list of hotels in the system
     */
    class RequestListHotelsMessage implements RentARoomMessage {

        public final ActorRef<RentARoomMessage> sender;

        public RequestListHotelsMessage(ActorRef<RentARoomMessage> sender) {
            this.sender = sender;
        }
    }


    /**
     * User requests room(s)
     */
    class RequestCreateReservationMessage implements RentARoomMessage {

        public final ActorRef<RentARoomMessage> sender;
        public final HashMap<String, Integer> requestRooms;


        public RequestCreateReservationMessage(ActorRef<RentARoomMessage> sender, HashMap<String, Integer> requestRooms) {
            this.sender = sender;
            this.requestRooms = requestRooms;
        }
    }

    /**
     * Confirm a reservation
     */
    class RequestConfirmReservationMessage implements RentARoomMessage {

        public final ActorRef<RentARoomMessage> sender;
        public final String reservationID;

        public RequestConfirmReservationMessage(ActorRef<RentARoomMessage> sender, String reservationID) {
            this.sender = sender;
            this.reservationID = reservationID;
        }
    }



    /**
     * Cancel a reservation
     */
    class RequestCancelReservationMessage implements RentARoomMessage {

        public final ActorRef<RentARoomMessage> sender;
        public final String reservationID;

        public RequestCancelReservationMessage(ActorRef<RentARoomMessage> sender, String reservationID) {
            this.sender = sender;
            this.reservationID = reservationID;
        }
    }

    /**
     * Create a reservation for room(s) from a hotel
     */
    class RequestCreateHotelRoomReservationMessage implements RentARoomMessage {

        public final ActorRef<RentARoomMessage> sendTo;
        public final int rooms;

        public RequestCreateHotelRoomReservationMessage(ActorRef<RentARoomMessage> sendTo, int rooms) {
            this.sendTo = sendTo;
            this.rooms = rooms;
        }
    }

    /**
     * Cancel a reservation for room(s) from a hotel
     */
    class RequestCancelHotelRoomReservationMessage implements RentARoomMessage {

        public final Integer[] rooms;

        public RequestCancelHotelRoomReservationMessage(Integer[] rooms) {this.rooms = rooms;}
    }

    /**
     *  Request self-stop
     */
    class RequestSelfStopMessage implements RentARoomMessage {

        public final ActorRef<RentARoomMessage> sender;

        public RequestSelfStopMessage(ActorRef<RentARoomMessage> sender) {
            this.sender = sender;
        }
    }

    /**
     * Create a hotel
     */
    class RequestCreateHotelMessage implements RentARoomMessage {

        public final ActorRef<RentARoomMessage> sender;
        public final String hotelName;
        public final int roomCount;

        public RequestCreateHotelMessage(ActorRef<RentARoomMessage> sender, String hotelName, int roomCount) {
            this.sender = sender;
            this.hotelName = hotelName;
            this.roomCount = roomCount;
        }
    }

    /**
     * Request abort reservation aggregation
     */
    class RequestAbortAggregationMessage implements RentARoomMessage {}

    /**
     * Delete a hotel
     */
    class RequestDeleteHotelMessage implements RentARoomMessage {

        public final ActorRef<RentARoomMessage> sender;
        public final String hotelID;

        public RequestDeleteHotelMessage(ActorRef<RentARoomMessage> sender, String hotelID) {
            this.sender = sender;
            this.hotelID = hotelID;
        }
    }

    /**
     * Send reservation data to aggregator
     */
    class SendHotelReservationData implements RentARoomMessage {

        public final Boolean status;
        public final Integer[] rooms;
        public final ActorRef<RentARoomMessage> sender;

        public SendHotelReservationData(Boolean status, Integer[] rooms, ActorRef<RentARoomMessage> sender) {
            this.status = status;
            this.rooms = rooms;
            this.sender = sender;
        }
    }

    /**
     * Request reservation actor creation
     */
    class RequestReservationActorCreationMessage implements RentARoomMessage {

        public final HashMap<ActorRef<RentARoomMessage>, Integer[]> reservedRooms;
        public final ActorRef<RentARoomMessage> originalRequestSource;

        public RequestReservationActorCreationMessage(HashMap<ActorRef<RentARoomMessage>, Integer[]> reservedRooms, ActorRef<RentARoomMessage> originalRequestSource) {
            this.reservedRooms = reservedRooms;
            this.originalRequestSource = originalRequestSource;
        }
    }

    /**
     * (RECEPTIONIST MESSAGE) Get reservation actor listings
     */
    class ReceiveListing implements RentARoomMessage{
        public final Receptionist.Listing listingActors;

        public ReceiveListing(Receptionist.Listing listingActors) {
            this.listingActors = listingActors;
        }
    }


    /**
     * Stop an actor
     */
    class Stop implements RentARoomMessage {}

    /**
     * Request to add Agent to the router
     */
    class RequestAddAgentMessage implements RentARoomMessage {

        public final ActorRef<RentARoomMessage> sender;

        public RequestAddAgentMessage(ActorRef<RentARoomMessage> sender) {this.sender = sender;}
    }

}
