package cc.ngc.service.tfl.impl;

import cc.ngc.service.tfl.api.IPredictedArrivalsQueryResponse;
import cc.ngc.service.tfl.api.IPrediction;

import java.util.*;
import java.util.stream.Collectors;

public class PredictedArrivalsQueryResponse implements IPredictedArrivalsQueryResponse {
    private final Collection<IPrediction> arrivals;

    PredictedArrivalsQueryResponse(Collection<IPrediction> arrivals) {
        this.arrivals = Collections.unmodifiableCollection(arrivals.stream().
                sorted(Comparator.comparing(IPrediction::getExpectedArrival)).collect(Collectors.toList()));
    }

    //------------------------------------------------------------------------------------------------------------------
    // IBusQueryResponse
    //------------------------------------------------------------------------------------------------------------------

    @Override
    public Collection<IPrediction> getArrivals() {
        return arrivals;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Overrides
    //------------------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (IPrediction arrival : arrivals)
            sb.append(arrival.toString()).append("\n");
        return sb.toString();
    }
}
