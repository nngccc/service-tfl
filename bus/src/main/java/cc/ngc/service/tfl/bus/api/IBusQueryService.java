package cc.ngc.service.tfl.bus.api;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface IBusQueryService {
    void poll(Handler<AsyncResult<BusQueryResponse>> handler);
}
