package cc.ngc.service.tfl.bus;

import cc.ngc.service.tfl.bus.api.IBusQueryService;
import io.vertx.core.Vertx;

public class BusQueryServiceTest {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        IBusQueryService service = new BusQueryService(vertx, 48411);

        service.poll(event -> {
        });
    }
}
