package nl.concurrency;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import nl.concurrency.actors.RentAgentRouter;
import nl.concurrency.messages.RentARoomMessage;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AkkaTest {

    static private ActorSystem<RentARoomMessage> testSystem;
    static private String reservationUUIDToConfirm, reservationUUIDToCancel;

    @BeforeAll
    static void setup() throws InterruptedException {
        testSystem = ActorSystem.create(RentAgentRouter.create(), "RentARoomTestSystem");
        Thread.sleep(100); //Sleeping to allow Akka to start up
        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestCreateHotelMessage(replyTo,"Hotel-A", 10),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );

        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();
        assertTrue(message.status);
    }

    @AfterAll
    static void close(){
        testSystem.terminate();
        testSystem.getWhenTerminated().whenComplete((done, err) -> System.out.println("Akka test system has shut down!"));
    }

    @DisplayName("Add 5 new agents")
    @Order(1)
    @Test
    void testAddAnAgent() {
        for (int i = 0; i < 5; i++){
            CompletionStage<RentARoomMessage> result =
                    AskPattern.ask(testSystem,
                            RentARoomMessage.RequestAddAgentMessage::new,
                            Duration.ofSeconds(6),
                            testSystem.scheduler()
                    );
            RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();
            assertTrue(message.status);
        }
    }

    @DisplayName("Create hotel")
    @Order(2)
    @Test
    void testCreateHotel() {
        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestCreateHotelMessage(replyTo,"Hotel-B", 10),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );

        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();

        assertTrue(message.status);
    }

    @DisplayName("Create hotel with the same name")
    @Order(3)
    @Test
    void testCreateSameHotel() {
        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestCreateHotelMessage(replyTo,"Hotel-B", 10),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );

        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();
        assertFalse(message.status);
    }

    @DisplayName("Create hotel with no rooms")
    @Order(4)
    @Test
    void testCreateHotelWithNoRoom() {
        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestCreateHotelMessage(replyTo,"Hotel-C", 0),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );

        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();
        assertFalse(message.status);
    }

    @DisplayName("Delete a non-existing hotel")
    @Order(5)
    @Test
    void testDeleteNonExistingHotel() {
        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestDeleteHotelMessage(replyTo,"Hotel-X"),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );

        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();
        assertFalse(message.status);
    }

    @DisplayName("Get hotel rooms")
    @Order(6)
    @Test
    void testListingHotels() {
        CompletionStage<RentARoomMessage> result2 =
                AskPattern.ask(testSystem,
                        RentARoomMessage.RequestListHotelsMessage::new,
                        Duration.ofSeconds(3),
                        testSystem.scheduler());
        RentARoomMessage.ResponseToUser message2 = (RentARoomMessage.ResponseToUser) result2.toCompletableFuture().join();

        assertTrue(message2.status);
        assertEquals("Hotels:\n[Hotel-A, Hotel-B]", message2.stringMessage);
    }

    @DisplayName("Make a reservation for one hotel only")
    @Order(7)
    @Test
    void testReservationForOneHotel() {
        HashMap<String, Integer> reservationData = new HashMap<>();

        reservationData.put("Hotel-A",5);

        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestCreateReservationMessage(replyTo,reservationData),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );

        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();

        reservationUUIDToCancel = message.stringMessage;

        assertTrue(message.status);
    }

    @DisplayName("Make a reservation for one hotel with not enough rooms")
    @Order(8)
    @Test
    void testReservationForOneHotelWithRoomOverflow() {
        HashMap<String, Integer> reservationData = new HashMap<>();

        reservationData.put("Hotel-A",20);

        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestCreateReservationMessage(replyTo,reservationData),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );

        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();

        assertFalse(message.status);
    }

    @DisplayName("Make a reservation a non existing hotel")
    @Order(9)
    @Test
    void testReservationForNonExistingHotel() {
        HashMap<String, Integer> reservationData = new HashMap<>();

        reservationData.put("Hotel-F",1);

        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestCreateReservationMessage(replyTo,reservationData),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );

        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();

        assertFalse(message.status);
    }

    @DisplayName("Make a reservation for 2 hotels for 2 rooms each")
    @Order(10)
    @Test
    void testReservationFor2HotelsFor2Rooms() {
        HashMap<String, Integer> reservationData = new HashMap<>();

        reservationData.put("Hotel-A",2);
        reservationData.put("Hotel-B",2);

        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestCreateReservationMessage(replyTo,reservationData),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );

        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();

        reservationUUIDToConfirm = message.stringMessage;
        assertTrue(message.status);
    }

    @DisplayName("Test successful reservation cancellation")
    @Test
    @Order(11)
    void testCancelReservation() {
        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestCancelReservationMessage(replyTo,reservationUUIDToCancel),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );

        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();

        assertTrue(message.status);
    }

    @DisplayName("Test successful reservation confirmation")
    @Order(12)
    @Test
    void testConfirmReservation() {
        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestCancelReservationMessage(replyTo,reservationUUIDToConfirm),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );

        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();

        assertTrue(message.status);
    }

    @DisplayName("Test confirming non-existing reservation")
    @Order(13)
    @Test
    void testConfirmNonExistingReservation() {
        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestConfirmReservationMessage(replyTo,"WrongUUID"),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );

        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();

        assertFalse(message.status);
    }

    @DisplayName("Test cancel non-existing reservation")
    @Order(14)
    @Test
    void testCancelNonExistingReservation() {
        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestCancelReservationMessage(replyTo,"WrongUUID"),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );

        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();

        assertFalse(message.status);
    }

    @DisplayName("Confirm reservation after the hotel has been deleted")
    @Order(15)
    @Test
    void testConfirmReservationForDeletedHotel() throws InterruptedException {
        String uuid;
        ///==================[Step 1: Make a reservation]/==================
        HashMap<String, Integer> reservationData = new HashMap<>();
        reservationData.put("Hotel-A",1);
        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestCreateReservationMessage(replyTo,reservationData),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );

        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();
        uuid = message.stringMessage;
        assertTrue(message.status);

        //==================[Step 2: Delete Hotel-A]==================
        CompletionStage<RentARoomMessage> result2 =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestDeleteHotelMessage(replyTo,"Hotel-A"),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );
        RentARoomMessage.ResponseToUser message2 = (RentARoomMessage.ResponseToUser) result2.toCompletableFuture().join();
        assertTrue(message2.status);

        //==================[Step 3: Confirm Reservation]==================
        Thread.sleep(100); //Sleeping to allow Akka to notify reservation of hotel deletion
        CompletionStage<RentARoomMessage> result3 =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestConfirmReservationMessage(replyTo,uuid),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );

        RentARoomMessage.ResponseToUser message3 = (RentARoomMessage.ResponseToUser) result3.toCompletableFuture().join();
        assertFalse(message3.status);
    }

    @DisplayName("Cancel reservation after the hotel has been deleted")
    @Order(16)
    @Test
    void testCancelReservationForDeletedHotel() throws InterruptedException {
        String uuid;
        ///==================[Step 1: Make a reservation]/==================
        HashMap<String, Integer> reservationData = new HashMap<>();
        reservationData.put("Hotel-B",1);
        CompletionStage<RentARoomMessage> result =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestCreateReservationMessage(replyTo,reservationData),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );

        RentARoomMessage.ResponseToUser message = (RentARoomMessage.ResponseToUser) result.toCompletableFuture().join();
        uuid = message.stringMessage;
        assertTrue(message.status);

        //==================[Step 2: Delete Hotel-B]==================
        CompletionStage<RentARoomMessage> result2 =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestDeleteHotelMessage(replyTo,"Hotel-B"),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );
        RentARoomMessage.ResponseToUser message2 = (RentARoomMessage.ResponseToUser) result2.toCompletableFuture().join();
        assertTrue(message2.status);

        //==================[Step 3: Confirm Reservation]==================
        Thread.sleep(100); //Sleeping to allow Akka to notify reservation of hotel deletion
        CompletionStage<RentARoomMessage> result3 =
                AskPattern.ask(testSystem,
                        replyTo -> new RentARoomMessage.RequestCancelReservationMessage(replyTo,uuid),
                        Duration.ofSeconds(3),
                        testSystem.scheduler()
                );

        RentARoomMessage.ResponseToUser message3 = (RentARoomMessage.ResponseToUser) result3.toCompletableFuture().join();
        assertFalse(message3.status);
    }

}
