package cc.ngc.service.tfl.api;

import java.util.Collection;

public interface IPredictedArrivalsQueryResponse {
    Collection<IPrediction> getArrivals();
}
