package cc.ngc.service.tfl.api;

import java.time.Instant;

public interface IPrediction {
    String getVehicleId();

    String getStationName();

    String getLineId();

    String getLineName();

    String getDestinationName();

    String getTowards();

    Instant getExpectedArrival();

    long getSecondsToExpectedArrival();

    String getModeName();
}
