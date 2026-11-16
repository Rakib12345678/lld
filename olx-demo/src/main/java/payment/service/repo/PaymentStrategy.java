package payment.service.repo;

public interface PaymentStrategy {

    public void validatepayment(PaymentRequest request);

    public PaymentResult processPayment(PaymentRequest request);
}
