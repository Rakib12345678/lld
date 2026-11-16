package Service;

import Model.Seat;
import Model.Show;

import java.util.List;

public interface PricingStrategy {

    double calcaulatePrice(Seat seat);
}
