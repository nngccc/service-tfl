package cc.ngc.service.tfl.api;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface IPredictedArrivalsQueryService {
    void poll(Handler<AsyncResult<IPredictedArrivalsQueryResponse>> handler);
}
