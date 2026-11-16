package service;

import model.Topic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TopicService {
    HashMap<String, List<Subscriber>> topicSubscribersMap;
    HashMap<String, Topic>topicList;

    private static final TopicService topicService=new TopicService();
    private TopicService(){
        topicList=new HashMap<>();
        topicSubscribersMap=new HashMap<>();

    }

    public static TopicService getInstance(){
        return topicService;
    }


    public void createTopic(Topic topic){
        topicList.put(topic.getId(),topic);
    }

    public void subscribe(String topicId,Subscriber subscriber){
        topicSubscribersMap
                .computeIfAbsent(topicId, k -> new ArrayList<>())
                .add(subscriber);
    }

    public List<Subscriber> getSubscribers(String topicId){
        return topicSubscribersMap.get(topicId);
    }

}
