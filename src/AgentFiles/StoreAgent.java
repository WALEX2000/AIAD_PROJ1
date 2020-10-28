package AgentFiles;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class StoreAgent extends Agent {
    private final Location storeLocation = new Location(0,0);
    List<Product> listOfOrders;
    private List<AID> couriers;

    public void setup() {
        this.couriers = new ArrayList<>();
        addBehaviour(new CheckInResponder());
        System.out.println("Store Setup complete");
    }

    private void addCourier(AID courierAgent) {
        this.couriers.add(courierAgent);
    }

    private void sendDeliveryRequest(Product product) { //Call this when we want to send a delivery request to our Couriers
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        try {
            cfp.setContentObject(product);
        } catch (IOException e) {
            System.err.println("Couldn't set content Object in CFP message with product: " + product.toString());
            return;
        }

        addBehaviour(new FIPAContractNetInit(this, cfp, couriers));
    }

    class CheckInResponder extends CyclicBehaviour {
        CheckInResponder() {
            System.out.println("Store waiting for Check-Ins");
        }

        @Override
        public void action() {
            ACLMessage msg = receive();
            if(msg != null) {
                System.out.println(msg);

                AID courierAgent;
                try {
                    courierAgent = (AID) msg.getContentObject();
                } catch (UnreadableException e) {
                    System.err.println("Can't get Object from CheckIn msg");
                    return;
                }
                addCourier(courierAgent);
                System.out.println("Added " + courierAgent.getLocalName() + " To list of Couriers");

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent("Got your message!");
                send(reply);
            }
            else block();
        }
    }

    class FIPAContractNetInit extends ContractNetInitiator {
        private List<AID> couriers;

        FIPAContractNetInit(Agent a, ACLMessage msg, List<AID> couriers) {
            super(a, msg);
            this.couriers = couriers;
        }

        protected Vector prepareCfps(ACLMessage cfp) {
            Vector<ACLMessage> v = new Vector<>();

            for(AID courierAgent : couriers) {
                cfp.addReceiver(courierAgent);
            }

            v.add(cfp);

            System.out.println("Prepared all messages for cfp: " + cfp);

            return v;
        }

        protected void handleAllResponses(Vector responses, Vector acceptances) {
            System.out.println("got " + responses.size() + " responses!");

            for (Object response : responses) {
                ACLMessage msg = ((ACLMessage) response).createReply();
                msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL); // OR NOT!
                acceptances.add(msg);
            }
        }

        protected void handleAllResultNotifications(Vector resultNotifications) {
            System.out.println("got " + resultNotifications.size() + " result notifs!");
        }

    }
}