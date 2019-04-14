package cc.ngc.service.tfl.bus;

import cc.ngc.service.tfl.bus.api.Bus;
import cc.ngc.service.tfl.bus.api.BusQueryResponse;
import cc.ngc.service.tfl.bus.api.IBusQueryService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class BusQueryService implements IBusQueryService {

    private static final String Server = "countdown.api.tfl.gov.uk";
    private static final String BaseURI = "/interfaces/ura/instant_V1";
    private static final String ReturnList = "StopCode1,StopPointName,LineName,DestinationText,EstimatedTime,MessageUUID,MessageText,MessagePriority,MessageType,ExpireTime";

    private final String requestURI;
    private final HttpClient client;

    public BusQueryService(Vertx vertx, long stopCode) {
        StringBuilder sbURI = new StringBuilder(BaseURI);
        sbURI.append("?StopCode1=").append(stopCode);
        sbURI.append("&VisitNumber=").append(1);
        sbURI.append("&ReturnList=").append(ReturnList);
        this.requestURI = sbURI.toString();
        this.client = vertx.createHttpClient(new HttpClientOptions().setDefaultHost(Server).setDefaultPort(80)
                .setSsl(false));
    }

    //------------------------------------------------------------------------------------------------------------------
    // IBusQueryService
    //------------------------------------------------------------------------------------------------------------------

    public void poll(Handler<AsyncResult<BusQueryResponse>> handler) {
        client.get(requestURI).handler(response -> response.bodyHandler(body -> {
            String data = body.toString();
            Optional<BusQueryResponse> busQueryResponse = parse(data);
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

    private Optional<BusQueryResponse> parse(String data) {
        if ((data == null) || (!data.startsWith("[")))
            return Optional.empty();

        /* Parse Data
         * Expected Format:
         * [4,"1.0",1555268856369]
         * [1,"Malvern Road","48411","K1","Kingston",1555268895000,1555268925000]
         * [1,"Malvern Road","48411","K1","Kingston",1555270115000,1555270145000]
         */
        String[] lines = data.split("\n");
        List<Bus> buses = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            String[] tokens = lines[i].split(",");
            if (tokens.length > 5) {
                Instant expected = Instant.ofEpochMilli(Long.valueOf(tokens[5]));
                buses.add(new Bus(tokens[3].replaceAll("\"", ""), tokens[4].replaceAll("\"", ""), expected));
            }
        }
        return Optional.of(new BusQueryResponse(buses));
    }
}
