package Service;
import model.Notification;
import model.User;
import strategy.NotificationFactory;
import strategy.NotificationGatewayStrategy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class NotificationService {
    private int maxRetry;

    private final UserService userService;
    private final ExecutorService executor;
    private final ConcurrentHashMap<String, Notification> notificationMap;

    private static NotificationService instance;

    private NotificationService(int maxCount
           ) {

        this.userService = UserService.getInstance();
        this.executor = Executors.newFixedThreadPool(5);
        this.notificationMap = new ConcurrentHashMap<>();
        this.maxRetry=maxCount;
    }

    public static synchronized NotificationService getInstance(int maxcount
            ) {

        if (instance == null) {
            instance = new NotificationService(
                    maxcount
            );
        }

        return instance;
    }

    public void addUser(User user) {
        userService.addUser(user);
    }

    public User getUser(String id) {
        return userService.getUser(id);
    }

    public void sendNotification(Notification notification) {
        AtomicInteger count=new AtomicInteger(0);
        NotificationGatewayStrategy strategy = NotificationFactory.getInstance()
                .getStrategy(notification.getChannelType());
        executor.submit(() -> {
            try {
                strategy.validate(notification);
                strategy.send(notification

                );
                count.getAndIncrement();

            } catch (Exception e) {
                System.err.println(
                        "Failed to send notification: " + e.getMessage()
                );
                if(maxRetry>count.get()){
                    strategy.send(notification);
                }else{
                    System.err.println(
                            "Max retry limit reached for notification: " + notification.getId()
                    );
                }


            }
        });
    }



}