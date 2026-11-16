package strategy;

import model.Notification;

public interface NotificationGatewayStrategy {
    void validate(Notification notificatioin);
    void  send(Notification notificatioin);
}
