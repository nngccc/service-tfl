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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PredictedTrainArrivalService implements IPredictedArrivalsQueryService {

    /*
     * http://ojp.nationalrail.co.uk/service/ldbboard/dep/SUR/WAT/To
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

    private static final String Server = "ojp.nationalrail.co.uk";
    private static final String BaseURIPattern = "/service/ldbboard/dep/%s/%s/To";

    private final String requestURI;
    private final HttpClient client;

    public PredictedTrainArrivalService(Vertx vertx, String from, String to) {
        this.requestURI = String.format(BaseURIPattern, from, to);
        this.client = vertx.createHttpClient(new HttpClientOptions().setDefaultHost(Server));
    }

    //------------------------------------------------------------------------------------------------------------------
    // IBusQueryService
    //------------------------------------------------------------------------------------------------------------------

    @Override
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
        List<IPrediction> trains = new ArrayList<>();

        Document doc = Jsoup.parse(data);
        Elements eResults = doc.getElementsByClass("results trains");
        Elements eTblCont = eResults.get(0).getElementsByClass("tbl-cont");
        Elements eBody = eTblCont.get(0).getElementsByTag("tbody");
        Elements eCells = eBody.get(0).getElementsByTag("td");

        String due = null;
        String destination = null;
        String status = null;
        String platform = null;

        int index = 0;
        for (Element element : eCells) {
            switch (index++ % 5) {
                case 0:
                    due = element.text();
                    break;
                case 1:
                    destination = element.text();
                    break;
                case 2:
                    status = element.text();
                    break;
                case 3:
                    platform = element.text();
                    break;
                case 4:
                    Instant expectedArrival = getExpectedArrival(due);
                    trains.add(new Prediction(due, null, null, null, platform, destination, null,
                            expectedArrival.toString(), "train", status));
                    break;
            }
        }

        return Optional.of(new PredictedArrivalsQueryResponse(trains));
    }

    private Instant getExpectedArrival(String due) {
        LocalTime lTime = LocalTime.parse(due);
        LocalDateTime now = LocalDateTime.now();
        return LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), lTime.getHour(), lTime.getMinute())
                .toInstant(ZoneOffset.UTC);
    }
}
