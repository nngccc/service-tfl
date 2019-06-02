package cc.ngc.service.tfl;

import cc.ngc.service.tfl.api.IPredictedArrivalsQueryService;
import cc.ngc.service.tfl.api.IPrediction;
import cc.ngc.service.tfl.impl.PredictedTrainArrivalService;
import io.vertx.core.Vertx;

import java.util.Collection;

public class PredictedTrainArrivalServiceTest {
    public static void main(String[] args) throws InterruptedException {
        Vertx vertx = Vertx.vertx();
        IPredictedArrivalsQueryService service = new PredictedTrainArrivalService(vertx, "SUR", "WAT");

        service.poll(response -> {
            if (response.succeeded()) {
                Collection<IPrediction> arrivals = response.result().getArrivals();
                for (IPrediction train : arrivals){
                    System.out.print(String.format("Due: %s, To: %s, Platform: %s, Status: %s\n", train.getVehicleId(),
                            train.getDestinationName(), train.getPlatformName(), train.getStatus()));
                }
            }
        });

        // Wait...
        Thread.sleep(5_000);
    }
}
