import Service.NotificationService;
import enums.ChannelType;
import model.Notification;
import model.User;
import strategy.NotificationFactory;

public class Main {

    public static void main(String []args){
        User A=new User("1","John Doe","1234567890","abdur@gmail.com","askdfsdkf","asdkfsdjkf");
        User B=new User("1","John Doe","1234567890","abdur@gmail.com","askdfsdkf","asdkfsdjkf");

        NotificationService notificationService=NotificationService.getInstance(4);
        notificationService.addUser(A);
        notificationService.addUser(B);

        notificationService.sendNotification(new Notification("abcd",ChannelType.SMS,"Good Morning","Hi rocky","1"));
    }
}
