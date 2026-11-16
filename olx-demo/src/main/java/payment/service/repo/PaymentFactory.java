package payment.service.repo;

import payment.dto.PaymentRequest;

public class PaymentFactory {
    private HashMap<String ,PaymentStrategry>factory;

    public static PaymentStrategy getInstance(PaymentRequest request){
        if(request.getMethod()=='UPI'){
            PaymentStrategy
        }
    }


}
