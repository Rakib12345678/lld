import model.Message;
import model.Topic;
import service.AlertSubscriber;
import service.OrderSubscriber;
import service.PubSubService;

public class Main {
    public static void main(String []args){
        PubSubService pubsubservice =PubSubService.getInstance();
        pubsubservice.createTopic(new Topic("1","notification",null));
        pubsubservice.createTopic(new Topic("2","order",null));
        pubsubservice.addSubscriber("1",new AlertSubscriber());
        pubsubservice.addSubscriber("1",new OrderSubscriber());


   pubsubservice.publish("1", new Message("1","Hello, this is a notification message!"));
//   pubsubservice.publish("1", new Message("1","Hello, this is a notification message!"));
    }
}
