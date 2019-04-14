package cc.ngc.service.tfl.bus.api;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Bus {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public final String route;
    public final String destination;
    public final Instant expectedUTC;

    public Bus(String route, String destination, Instant expectedUTC) {
        this.route = route;
        this.destination = destination;
        this.expectedUTC = expectedUTC;
    }

    @Override
    public String toString() {
        return String.format("%s, '%s' to '%s'",
                LocalDateTime.ofInstant(expectedUTC, ZoneId.systemDefault()).format(formatter), route, destination);
    }
}
