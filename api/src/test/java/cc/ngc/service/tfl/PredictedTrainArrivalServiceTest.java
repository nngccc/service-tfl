package cc.ngc.service.tfl;

import cc.ngc.service.tfl.api.IPredictedArrivalsQueryService;
import cc.ngc.service.tfl.api.IPrediction;
import cc.ngc.service.tfl.impl.PredictedArrivalsQueryService;
import cc.ngc.service.tfl.impl.PredictedTrainArrivalService;
import io.vertx.core.Vertx;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class PredictedTrainArrivalServiceTest {
    public static void main(String[] args) throws InterruptedException {
        Vertx vertx = Vertx.vertx();
        IPredictedArrivalsQueryService service = new PredictedTrainArrivalService(vertx, "surbiton", "london-waterloo");

        AtomicReference<IPrediction> nextArrival = new AtomicReference<>();
        long ticks = 0;

        System.out.println("Waiting...");
        Thread.sleep(5_000);
        while (true) {

            if (ticks++ % 30 == 0)
                service.poll(response -> {
                    if (response.succeeded()) {
                        Optional<IPrediction> next = response.result().getArrivals().stream().findFirst();
                        nextArrival.set(next.orElse(null));
                    } else {
                        nextArrival.set(null);
                    }
                });

            // Update
            IPrediction next = nextArrival.get();
            if (next != null) {
                long eta = next.getSecondsToExpectedArrival();
                System.out.print(String.format("\r[%02d:%02d] %s (%s) to %s", eta / 60, eta % 60, next.getLineName(), next.getVehicleId(), next.getDestinationName()));
            } else
                System.out.print("\rNext: ---");

            // Wait...
            Thread.sleep(1_000);

        }
    }
}
