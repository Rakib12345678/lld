package service;

import model.Message;

public class AlertSubscriber implements Subscriber{
    public AlertSubscriber(){}
    @Override
    public void onMessageUpdate(Message message) {
        System.out.println("AlertSubscriber received message update: " + message.getContent());
    }
}
