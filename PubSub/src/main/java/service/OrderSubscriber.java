package service;

import model.Message;

public class OrderSubscriber implements Subscriber{
    public OrderSubscriber(){};
    @Override
    public void onMessageUpdate(Message message) {
        System.out.println("OrderSubscriber received message update: " + message.getContent());
    }
}
