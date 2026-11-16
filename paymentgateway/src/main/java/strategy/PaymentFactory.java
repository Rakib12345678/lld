package strategy;

import enums.PaymentMethod;

public class PaymentFactory {

    public static PaymentProcessor getPaymentProcessor(PaymentMethod method) {
        switch (method) {
            case PAYPAL:
                return new PaypalStrategy();
            case UPI:
                return new UpiStrategy();
            case NET_BANKING:
                return new NetBankingStrategy();
            default:
                throw new IllegalArgumentException("Unsupported payment method: " + method);
        }
    }
}