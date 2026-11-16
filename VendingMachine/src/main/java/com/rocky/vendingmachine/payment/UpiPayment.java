package com.rocky.vendingmachine.payment;

/**
 * Payment request for UPI payments.
 * Contains UPI-specific details: UPI ID and QR code reference.
 */
public class UpiPayment extends PaymentRequest {

    private final String upiId;
    private final String qrCodeRef;

    public UpiPayment(double amountPaid, double cartTotalPrice, String payerId,
                      String upiId, String qrCodeRef) {
        super(amountPaid, cartTotalPrice, payerId);
        this.upiId = upiId;
        this.qrCodeRef = qrCodeRef;
    }

    public String getUpiId() {
        return upiId;
    }

    public String getQrCodeRef() {
        return qrCodeRef;
    }

    @Override
    public String toString() {
        return super.toString() + " UpiPayment{" +
                "upiId='" + upiId + '\'' +
                '}';
    }
}