package cc.ngc.service.tfl.impl;

import cc.ngc.service.tfl.api.IPrediction;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Prediction implements IPrediction {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private String vehicleId;
    private String stationName;
    private String lineId;
    private String lineName;
    private String platformName;
    private String destinationName;
    private String towards;
    private Instant expectedArrival;
    private String modeName;
    private String status;

    @JsonCreator
    Prediction(
            @JsonProperty("vehicleId") String vehicleId,
            @JsonProperty("stationName") String stationName,
            @JsonProperty("lineId") String lineId,
            @JsonProperty("lineName") String lineName,
            @JsonProperty("platformName") String platformName,
            @JsonProperty("destinationName") String destinationName,
            @JsonProperty("towards") String towards,
            @JsonProperty("expectedArrival") String expectedArrival,
            @JsonProperty("modeName") String modeName,
            @JsonProperty("status") String status) {
        this.vehicleId = vehicleId;
        this.stationName = stationName;
        this.lineId = lineId;
        this.lineName = lineName;
        this.platformName = platformName;
        this.destinationName = destinationName;
        this.towards = towards;
        this.expectedArrival = Instant.parse(expectedArrival);
        this.modeName = modeName;
        this.status = status;
    }

    Prediction() {
    }

    //------------------------------------------------------------------------------------------------------------------
    // IPrediction
    //------------------------------------------------------------------------------------------------------------------

    @Override
    public String getVehicleId() {
        return vehicleId;
    }

    @Override
    public String getStationName() {
        return stationName;
    }

    @Override
    public String getLineId() {
        return lineId;
    }

    @Override
    public String getLineName() {
        return lineName;
    }

    @Override
    public String getPlatformName() {
        return platformName;
    }

    @Override
    public String getDestinationName() {
        return destinationName;
    }

    @Override
    public String getTowards() {
        return towards;
    }

    @Override
    public Instant getExpectedArrival() {
        return expectedArrival;
    }

    @Override
    public long getSecondsToExpectedArrival() {
        return expectedArrival.getEpochSecond() - Instant.now().getEpochSecond();
    }

    @Override
    public String getModeName() {
        return modeName;
    }

    @Override
    public String getStatus() {
        return status;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Setters
    //------------------------------------------------------------------------------------------------------------------

    Prediction vehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
        return this;
    }

    Prediction stationName(String stationName) {
        this.stationName = stationName;
        return this;
    }

    Prediction lineId(String lineId) {
        this.lineId = lineId;
        return this;
    }

    Prediction lineName(String lineName) {
        this.lineName = lineName;
        return this;
    }

    Prediction platformName(String platformName) {
        this.platformName = platformName;
        return this;
    }

    Prediction destinationName(String destinationName) {
        this.destinationName = destinationName;
        return this;
    }

    Prediction towards(String towards) {
        this.towards = towards;
        return this;
    }

    Prediction expectedArrival(Instant expectedArrival) {
        this.expectedArrival = expectedArrival;
        return this;
    }

    Prediction modeName(String modeName) {
        this.modeName = modeName;
        return this;
    }

    Prediction status(String status) {
        this.status = status;
        return this;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Overrides
    //------------------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format("%s, '%s' to '%s' (in %s seconds)",
                LocalDateTime.ofInstant(expectedArrival, ZoneId.systemDefault())
                        .format(formatter), lineName, destinationName, getSecondsToExpectedArrival());
    }
}
