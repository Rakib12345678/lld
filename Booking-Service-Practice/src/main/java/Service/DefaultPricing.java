package Service;

import Model.Seat;

public class DefaultPricing implements PricingStrategy{

    @Override
    public double calcaulatePrice(Seat seat) {
        return
                switch(seat.getSeatType()){
                   case NORMAL->200.0;
                  case  PREMIUM->400.0;
                   case  RECLINER->600.0;
                   default->0.0;
                };
    }
}


}