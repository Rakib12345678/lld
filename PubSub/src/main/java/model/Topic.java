package model;

import service.Subscriber;

import java.util.List;

public class Topic {
    private String id;
    private String name;
    private List<Subscriber> subscribers;

    public Topic(String id, String name, List<Subscriber> subscribers) {
        this.id = id;
        this.name = name;
        this.subscribers = subscribers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Subscriber> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(List<Subscriber> subscribers) {
        this.subscribers = subscribers;
    }
}
