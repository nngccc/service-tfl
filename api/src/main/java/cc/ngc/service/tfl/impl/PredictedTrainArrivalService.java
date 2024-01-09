package cc.ngc.service.tfl.impl;

import cc.ngc.service.tfl.api.IPredictedArrivalsQueryResponse;
import cc.ngc.service.tfl.api.IPredictedArrivalsQueryService;
import cc.ngc.service.tfl.api.IPrediction;
import com.fasterxml.jackson.core.type.TypeReference;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.Json;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Type;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PredictedTrainArrivalService implements IPredictedArrivalsQueryService {

    //https://ojp.nationalrail.co.uk/service/ldb/livetrainsjson?departing=true&liveTrainsFrom=SUR&liveTrainsTo=WAT

    /*
     * https://ojp.nationalrail.co.uk/service/ldbboard/dep/SUR/WAT/To
     * Due
     * -05:56
     * Destination
     * -London
     * Status
     * -On time
     * -05:58 2 mins late
     * -Delayed
     * -Cancelled
     */

    private static final String Server = "www.nationalrail.co.uk";
    private static final String BaseURIPattern = "/live-trains/departures/%s/%s/";

    private final String requestURI;
    private final HttpClient client;

    public PredictedTrainArrivalService(Vertx vertx, String from, String to) {
        this.requestURI = String.format(BaseURIPattern, from, to);
        this.client = vertx.createHttpClient(new HttpClientOptions().setDefaultHost(Server).setDefaultPort(443).setSsl(true));
    }

    //------------------------------------------------------------------------------------------------------------------
    // IBusQueryService
    //------------------------------------------------------------------------------------------------------------------

    @Override
    public void poll(Handler<AsyncResult<IPredictedArrivalsQueryResponse>> handler) {
        client.get(requestURI).handler(response -> response.bodyHandler(body -> {
            try
            {
                String data = body.toString();
                PredictedArrivalsQueryResponse busQueryResponse = parse(data);
                handler.handle(Future.succeededFuture(busQueryResponse));
            }
            catch (Throwable e)
            {
                handler.handle(Future.failedFuture(e));
            }
        })).end();
    }

    //------------------------------------------------------------------------------------------------------------------
    // Helpers
    //------------------------------------------------------------------------------------------------------------------

    private PredictedArrivalsQueryResponse parse(String data) {
        List<IPrediction> trains = new ArrayList<>();

        Document doc = Jsoup.parse(data);

        Elements eData = doc.getElementsByAttributeValueContaining("id", "__NEXT_DATA__");
        Map<String, Object> json = Json.decodeValue(eData.get(0).data(), new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> props = (Map<String, Object>) json.get("props");
        Map<String, Object> pageProps = (Map<String, Object>) props.get("pageProps");
        Map<String, Object> liveState = (Map<String, Object>) pageProps.get("liveTrainsState");
        Map<String, Object> state = (Map<String, Object>) ((Map) ((List) liveState.get("queries")).get(0)).get("state");
        Map<String, Object> stateData = (Map<String, Object>) state.get("data");
        Map<String, Object> firstPage = (Map<String, Object>) ((List) stateData.get("pages")).get(0);
        List<Map<String, Object>> services = (List<Map<String, Object>>) firstPage.get("services");
        services.forEach(service -> {
            String status = ((Map<String, String>) service.get("status")).get("status");
            String scheduled = ((Map<String, String>) service.get("departureInfo")).get("scheduled");
            String estimated = ((Map<String, String>) service.get("departureInfo")).get("estimated");
            String due = Optional.ofNullable(estimated).orElse(scheduled);
            Instant expectedArrival = getExpectedArrival(due);
            String platform = Optional.ofNullable(service.get("platform")).map(Object::toString).orElse("?");
            String lineId = "Platform " + platform;
            String destination = ((Map<String, String>) ((List) service.get("destination")).get(0)).get("crs");

            trains.add(new Prediction(due, null, null, lineId, platform, destination, null,
                    expectedArrival.toString(), "train", status));
        });
        return new PredictedArrivalsQueryResponse(trains);
    }

    private Instant getExpectedArrival(String due) {
        // Parsing this: 2024-01-09T12:11:17+00:00
        // Why? RaspberryPi fails to parse as Instant
        String time = due.substring(11, 19);
        LocalTime lTime = LocalTime.parse(time);
        LocalDateTime now = LocalDateTime.now();
        return LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), lTime.getHour(), lTime.getMinute())
                .toInstant(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
    }
}
