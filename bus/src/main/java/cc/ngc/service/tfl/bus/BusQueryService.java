package cc.ngc.service.tfl.bus;

import cc.ngc.service.tfl.bus.api.BusQueryResponse;
import cc.ngc.service.tfl.bus.api.IBusQueryService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

public class BusQueryService implements IBusQueryService {

    private static final String TflServer = "countdown.api.tfl.gov.uk";
    private static final String ReturnList = "&ReturnList=StopCode1,StopPointName,LineName,DestinationText,EstimatedTime,MessageUUID,MessageText,MessagePriority,MessageType,ExpireTime";
    private static final String BaseURI = "/interfaces/ura/instant_V1?StopCode1=%s%s&VisitNumber=1" + ReturnList;

    private final String requestURL;
    private final HttpClient client;

    public BusQueryService(Vertx vertx, int stopCode) {
        this.requestURL = String.format(BaseURI, stopCode, "");
        this.client = vertx.createHttpClient(new HttpClientOptions().setDefaultHost(TflServer).setDefaultPort(80)
                .setSsl(false));
    }

    //------------------------------------------------------------------------------------------------------------------
    // IBusQueryService
    //------------------------------------------------------------------------------------------------------------------

    public void poll(Handler<AsyncResult<BusQueryResponse>> response) {

        client.get(requestURL).handler(x -> {
            System.out.println("!!");
            x.bodyHandler(body -> {
                String res = body.toString();
                System.out.println("!!!" + res);
            });
        }).end();
    }
}
