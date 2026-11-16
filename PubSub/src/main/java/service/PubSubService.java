package service;

import model.Message;
import model.Topic;

public class PubSubService {

    private TopicService topicService;

    private static final PubSubService  pubsubservice=new PubSubService();


    PubSubService(){
        topicService=TopicService.getInstance();
    }

    public static PubSubService getInstance(){
        return pubsubservice;
    }

    public void createTopic(Topic topic){
        topicService.createTopic(topic);
    }

    public void addSubscriber(String topicId,Subscriber subs){
        topicService.subscribe(topicId, subs);
    }

    public void publish(String topicId, Message message){
        for(Subscriber subs:topicService.getSubscribers(topicId)){
            subs.onMessageUpdate(message);
        }
    }
}
