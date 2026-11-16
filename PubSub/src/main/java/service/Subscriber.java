package service;

import model.Message;

public interface Subscriber {
    void onMessageUpdate(Message message);
}
