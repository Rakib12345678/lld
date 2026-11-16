package Model;

import enums.PaymentStatus;

public class Payment {
    private String id;
    private PaymentStatus paymentStatus;
    private String  bookingId;
    private double totalPrice;

    public Payment(String id, PaymentStatus paymentStatus, String bookingId, double totalPrice) {
        this.id = id;
        this.paymentStatus = paymentStatus;
        this.bookingId = bookingId;
        this.totalPrice = totalPrice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
