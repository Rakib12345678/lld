package strategy;

import enums.ChannelType;

import java.util.concurrent.ConcurrentHashMap;

public class NotificationFactory {

    private final ConcurrentHashMap<ChannelType, NotificationGatewayStrategy> strategyMap;

    private static final NotificationFactory INSTANCE =
            new NotificationFactory();

    private NotificationFactory() {
        strategyMap = new ConcurrentHashMap<>();
    }

    public static NotificationFactory getInstance() {
        return INSTANCE;
    }

    public NotificationGatewayStrategy getStrategy(ChannelType channel) {

        return strategyMap.computeIfAbsent(
                channel,
                this::createStrategy
        );
    }

    private NotificationGatewayStrategy createStrategy(ChannelType channel) {

        return switch (channel) {
            case EMAIL -> new EmailNotificationStrategy();
            default -> new SMSGatewayStrategy();
        };
    }
}