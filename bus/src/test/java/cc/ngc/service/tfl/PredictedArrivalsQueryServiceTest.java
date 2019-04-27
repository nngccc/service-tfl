package cc.ngc.service.tfl;

import cc.ngc.service.tfl.api.IPredictedArrivalsQueryService;
import cc.ngc.service.tfl.impl.PredictedArrivalsQueryService;
import io.vertx.core.Vertx;

public class PredictedArrivalsQueryServiceTest {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        IPredictedArrivalsQueryService service = new PredictedArrivalsQueryService(vertx, "490009534W");
        service.poll(response -> {
            System.out.println(response.succeeded() ? "[OK]" : "[FAILED]");
            if (response.succeeded()) {
                System.out.println(response.result().toString());
            } else {
                System.out.println(response.cause());
            }
        });
    }
}
