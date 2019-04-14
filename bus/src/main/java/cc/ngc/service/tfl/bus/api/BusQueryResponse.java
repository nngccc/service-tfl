package cc.ngc.service.tfl.bus.api;

import java.util.Collection;

public class BusQueryResponse {
    public Collection<Bus> buses;

    public BusQueryResponse(Collection<Bus> buses) {
        this.buses = buses;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (Bus bus : buses)
            sb.append(bus.toString()).append("\n");
        return sb.toString();
    }
}
