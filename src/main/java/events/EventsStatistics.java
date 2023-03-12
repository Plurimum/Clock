package events;

import java.util.Map;
import java.util.Optional;

public interface EventsStatistics {
    void incEvent(String eventName);

    Optional<Double> getEventStatisticByName(String eventName);

    Map<String, Double> getAllEventStatistic();

    void printStatistic();
}
