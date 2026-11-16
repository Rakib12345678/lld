package strategy;

import Service.UserService;
import model.Notification;
import enums.ChannelType;

public class SMSGatewayStrategy implements NotificationGatewayStrategy{
    private UserService userService;
    public SMSGatewayStrategy() {
        this.userService=UserService.getInstance();
    }

    @Override
    public void validate(Notification notification) {
        if(notification.getChannelType() != null && notification.getChannelType() == ChannelType.SMS){
           if(notification.getRecipientId()==null || notification.getRecipientId().isEmpty()){
               throw new IllegalArgumentException("Recipient ID is required for SMS notifications");
           }
           String token=userService.getUser(notification.getRecipientId()).getFcmToken();
           if(token==null || token.isEmpty()){
               throw new IllegalArgumentException("User token is required for SMS notifications");
           }
       } else {
           throw new IllegalArgumentException("Invalid channel type for SMSGatewayStrategy");
       }
    }

    @Override
    public void send(Notification notification) {
        String token=userService.getUser(notification.getRecipientId()).getFcmToken();
        System.out.println("Sending SMS notification to user with token: " + token);
    }
}
