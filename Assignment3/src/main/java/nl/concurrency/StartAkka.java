package nl.concurrency;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import nl.concurrency.actors.RentAgentRouter;
import nl.concurrency.messages.RentARoomMessage;

import java.time.Duration;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.CompletionStage;

public class StartAkka {

    private ActorSystem<RentARoomMessage> system;

    public static void main(String[] args) {
        new StartAkka().run();

    }

    private void run() {
        system = ActorSystem.create(RentAgentRouter.create(), "RentARoomSystem");
        commandLoop();
        system.terminate();
        system.getWhenTerminated().whenComplete((done, err) -> System.out.println("Akka has shut down!"));
    }


    public void commandLoop() {
        String help = "Commands:\n" +
                "\n" +
                "L: List hotels\n" +
                "B: Add agent\n" +
                "H: Add hotels\n" +
                "D: Delete hotels\n" +
                "R: Make a reservation\n" +
                "X: Cancel reservation\n" +
                "C: Confirm reservation\n" +
                "?: This menu\n" +
                "Q: Quit\n";
        System.out.println(help);
        System.out.print(">");
        Scanner s = new Scanner(System.in);
        String c = s.nextLine().toLowerCase();
        while (!c.equals("q")) {
            switch (c) {
                case "?":
                    System.out.println(help);
                    break;
                case "l":
                    requestListHotels();
                    break;
                case "h":
                    requestAddHotel();
                    break;
                case "b":
                    requestAddAgent();
                    break;
                case "r":
                    requestReservation();
                    break;
                case "x":
                    requestCancelReservation();
                    break;
                case "c":
                    requestConfirmReservation();
                    break;
                case "d":
                    requestDeleteHotel();
                    break;
                default:
                    System.out.println("Unknown input, please try again or type [?] to get help");
                    break;
            }
            System.out.print(">");
            c = s.nextLine().toLowerCase();
        }
    }

    private void requestAddAgent() {
        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(system,
                        RentARoomMessage.RequestAddAgentMessage::new,
                        Duration.ofSeconds(6),
                        system.scheduler()
                );
        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();
        if (!message.status) System.err.println("Something went wrong with adding an agent!");
        else System.out.println("Successfully added agent with ID: "+message.stringMessage);
    }

    private void requestReservation() {
        HashMap<String, Integer> reservationData = new HashMap<>();
        System.out.println("You can reserve multiple hotels at the same time. Just type [!] as the name to finish the reservation\n");
        while (true) {
            System.out.println("Enter name of the hotel you would like to reserve in:\n");
            System.out.print(">");
            Scanner s = new Scanner(System.in);
            String name = s.nextLine();
            if (name.equals("!")) break;
            reservationData.put(name, parseRooms());
        }
        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(system,
                        replyTo -> new RentARoomMessage.RequestCreateReservationMessage(replyTo,reservationData),
                        Duration.ofSeconds(6),
                        system.scheduler()
                );
        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();
        if (!message.status) System.err.println("Could not reserve: "+message.stringMessage);
        else System.out.println("Successfully created a reservation with ID: "+message.stringMessage);
    }

    private void requestCancelReservation() {
        manageReservation(true);
    }

    private void requestConfirmReservation() {
        manageReservation(false);
    }

    private void requestDeleteHotel() {
        System.out.println("Enter name of the hotel:");
        System.out.print(">");
        Scanner s = new Scanner(System.in);
        String name = s.nextLine();
        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(system,
                        replyTo -> new RentARoomMessage.RequestDeleteHotelMessage(replyTo,name),
                        Duration.ofSeconds(6),
                        system.scheduler()
                );
        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();
        if (!message.status) System.err.println("Could not delete hotel: "+message.stringMessage);
        else System.out.println("Successfully deleted the hotel!");
    }

    private void requestAddHotel() {
        System.out.println("Enter name of the hotel to create:");
        System.out.print(">");
        Scanner s = new Scanner(System.in);
        String name = s.nextLine();
        int finalRooms = parseRooms();
        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(system,
                        replyTo -> new RentARoomMessage.RequestCreateHotelMessage(replyTo,name, finalRooms),
                        Duration.ofSeconds(6),
                        system.scheduler()
                );
        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();
        if (!message.status) System.err.println("Could not create hotel: "+message.stringMessage);
        else System.out.println("Successfully added hotel with Name: "+message.stringMessage);
    }

    private void requestListHotels() {
        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(system,
                        RentARoomMessage.RequestListHotelsMessage::new,
                        Duration.ofSeconds(6),
                        system.scheduler());
        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();
        if (!message.status) System.out.println("No data, the hotel list might be empty!");
        else System.out.println(message.stringMessage);
    }

    private void manageReservation(boolean isCancellation){
        System.out.println("Enter your reservation ID (note that it MUST BEGIN WITH 'Reservation--'):\n");
        System.out.print(">");
        Scanner s = new Scanner(System.in);
        String id = s.nextLine();
        CompletionStage<RentARoomMessage> result;
        if (isCancellation){
            result =
                    AskPattern.ask(system,
                            replyTo -> new RentARoomMessage.RequestCancelReservationMessage(replyTo,id),
                            Duration.ofSeconds(6),
                            system.scheduler()
                    );
        } else {
            result =
                    AskPattern.ask(system,
                            replyTo -> new RentARoomMessage.RequestConfirmReservationMessage(replyTo,id),
                            Duration.ofSeconds(6),
                            system.scheduler()
                    );
        }
        assert result != null;
        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();
        if (!message.status) System.err.println("Could not update reservation: "+message.stringMessage);
        else System.out.println("Reservation successfully updated!");
    }

    private int parseRooms(){
        Scanner s = new Scanner(System.in);
        int rooms = -1;
        while (rooms == -1){
            System.out.println("Enter number of rooms:");
            System.out.print(">");
            String roomsString = s.nextLine();
            try {
                rooms = Integer.parseInt(roomsString);
            } catch (Exception e) {
                System.err.println("Invalid input");
            }
        }
        return rooms;
    }

}
