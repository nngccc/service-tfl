package cc.ngc.service.tfl.impl;

import cc.ngc.service.tfl.api.IPredictedArrivalsQueryResponse;
import cc.ngc.service.tfl.api.IPredictedArrivalsQueryService;
import cc.ngc.service.tfl.api.IPrediction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

import java.io.IOException;
import java.util.List;

public class PredictedArrivalsQueryService implements IPredictedArrivalsQueryService {
    /*
     * Retrieve Stop Points:    https://api.tfl.gov.uk/Line/K1/StopPoints
     * Retrieve Arrivals:       https://api.tfl.gov.uk/StopPoint/490009534W/Arrivals
     */

    private static final String Server = "api.tfl.gov.uk";
    private static final String BaseURIPattern = "/StopPoint/%s/Arrivals";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, Prediction.class);

    private final String requestURI;
    private final HttpClient client;

    public PredictedArrivalsQueryService(Vertx vertx, String stopPoint) {
        this.requestURI = String.format(BaseURIPattern, stopPoint);
        this.client = vertx.createHttpClient(new HttpClientOptions().setDefaultHost(Server).setDefaultPort(443).setSsl(true));
    }

    //------------------------------------------------------------------------------------------------------------------
    // IBusQueryService
    //------------------------------------------------------------------------------------------------------------------

    @Override
    public void poll(Handler<AsyncResult<IPredictedArrivalsQueryResponse>> handler) {
        client.get(requestURI).handler(response -> response.bodyHandler(body -> {
            try {
                String data = body.toString();
                List<IPrediction> arrivals = objectMapper.readValue(data, collectionType);
                handler.handle(Future.succeededFuture(new PredictedArrivalsQueryResponse(arrivals)));
            } catch (IOException e) {
                handler.handle(Future.failedFuture(e));
            }
        })).end();
    }
}
