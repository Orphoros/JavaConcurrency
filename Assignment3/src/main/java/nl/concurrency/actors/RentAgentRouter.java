package nl.concurrency.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import nl.concurrency.messages.RentARoomMessage;

import java.util.UUID;

public class RentAgentRouter extends AbstractBehavior<RentARoomMessage> {

    private final ActorRef<RentARoomMessage> router;

    public RentAgentRouter(ActorContext<RentARoomMessage> context) {
        super(context);
        GroupRouter<RentARoomMessage> group = Routers.group(Agent.CREATE_AGENT);
        router = context.spawn(group, "agent-group");

        context.spawn(Agent.create(), "Agent-Smith");
    }

    public static Behavior<RentARoomMessage> create() {
        return Behaviors.setup(RentAgentRouter::new);
    }

    @Override
    public Receive<RentARoomMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(RentARoomMessage.RequestAddAgentMessage.class, this::addAgent)
                .onMessage(RentARoomMessage.class, message -> {
                    router.tell(message);
                    return Behaviors.same();
                })
                .build();
    }

    private Behavior<RentARoomMessage> addAgent(RentARoomMessage.RequestAddAgentMessage message){
        String agentID = "Agent--"+ UUID.randomUUID();
        getContext().spawn(Agent.create(), agentID);
        message.sender.tell(new RentARoomMessage.ResponseToUser(true, agentID));
        return Behaviors.same();
    }

}
