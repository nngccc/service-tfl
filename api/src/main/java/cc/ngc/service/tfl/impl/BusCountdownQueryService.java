package cc.ngc.service.tfl.impl;

import cc.ngc.service.tfl.api.IPredictedArrivalsQueryResponse;
import cc.ngc.service.tfl.api.IPredictedArrivalsQueryService;
import cc.ngc.service.tfl.api.IPrediction;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BusCountdownQueryService implements IPredictedArrivalsQueryService {

    private static final String Server = "countdown.api.tfl.gov.uk";
    private static final String BaseURI = "/interfaces/ura/instant_V1";
    private static final String ReturnList = "StopCode1,StopPointName,LineName,DestinationText,EstimatedTime,MessageUUID,MessageText,MessagePriority,MessageType,ExpireTime";

    private final String requestURI;
    private final HttpClient client;

    public BusCountdownQueryService(Vertx vertx, long stopCode) {
        StringBuilder sbURI = new StringBuilder(BaseURI);
        sbURI.append("?StopCode1=").append(stopCode);
        sbURI.append("&VisitNumber=").append(1);
        sbURI.append("&ReturnList=").append(ReturnList);
        this.requestURI = sbURI.toString();
        this.client = vertx.createHttpClient(new HttpClientOptions().setDefaultHost(Server).setDefaultPort(80).setSsl(false));
    }

    //------------------------------------------------------------------------------------------------------------------
    // IBusQueryService
    //------------------------------------------------------------------------------------------------------------------

    public void poll(Handler<AsyncResult<IPredictedArrivalsQueryResponse>> handler) {
        client.get(requestURI).handler(response -> response.bodyHandler(body -> {
            String data = body.toString();
            Optional<PredictedArrivalsQueryResponse> busQueryResponse = parse(data);
            if (busQueryResponse.isPresent()) {
                handler.handle(Future.succeededFuture(busQueryResponse.get()));
            } else {
                handler.handle(Future.failedFuture(data));
            }
        })).end();
    }

    //------------------------------------------------------------------------------------------------------------------
    // Helpers
    //------------------------------------------------------------------------------------------------------------------

    private Optional<PredictedArrivalsQueryResponse> parse(String data) {
        if ((data == null) || (!data.startsWith("[")))
            return Optional.empty();

        /* Parse Data
         * Expected Format:
         * [4,"1.0",1555268856369]
         * [1,"Malvern Road","48411","K1","Kingston",1555268895000,1555268925000]
         * [1,"Malvern Road","48411","K1","Kingston",1555270115000,1555270145000]
         */
        String[] lines = data.split("\n");
        List<IPrediction> arrivals = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            String[] tokens = lines[i].split(",");
            if (tokens.length > 5) {
                Instant expected = Instant.ofEpochMilli(Long.valueOf(tokens[5]));
                arrivals.add(new Prediction().expectedArrival(expected).lineName(tokens[3].replaceAll("\"", "")).destinationName(tokens[4].replaceAll("\"", "")));
            }
        }
        return Optional.of(new PredictedArrivalsQueryResponse(arrivals));
    }
}
