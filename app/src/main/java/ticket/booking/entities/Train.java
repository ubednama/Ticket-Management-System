package ticket.booking.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Train {

    private String trainId;
    private String trainNo;
    private String source;
    private String destination;
    private List<List<Integer>> seats;
    private Map<String, String> stationTimes;
    private List<String> stations;

    public Train() {}

    public Train(String trainId, String trainNo, String source, String destination, List<List<Integer>> seats,
                 Map<String, String> stationTimes,
                 List<String> stations) {
        this.trainId = trainId;
        this.trainNo = trainNo;
        this.seats = seats;
        this.source = source;
        this.destination = destination;
        this.stationTimes = stationTimes;
        this.stations = stations;
    }

    @JsonProperty("stations")
    public void setStations(Object stations) {
        if (stations instanceof List<?> stationList) {
            if (!stationList.isEmpty() && stationList.get(0) instanceof String) {
                @SuppressWarnings("unchecked")
                List<String> stringList = (List<String>) stationList;
                this.stations = stringList;
            }
        } else if (stations instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, String> stationMap = (Map<String, String>) stations;
            this.stations = new ArrayList<>(stationMap.keySet());
            if (this.stationTimes == null) {
                this.stationTimes = stationMap;
            }
        }
    }

    public String getTrainInfo() {
        return String.format("Train ID: %s Train No: %s", trainId, trainNo);
    }
}