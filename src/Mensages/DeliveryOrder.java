public class DeliveryOrder {
    
    private int version;
    private MessageType type;
    private Product order;

    public DeliveryOrder(int version, Product order) {
        this.version = version;
        this.type = DeliveryOrder;
        this.order = order;
    }

}
