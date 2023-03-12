package events;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EventsStatisticsImpl implements EventsStatistics {
    private final Clock clock;

    private final Map<String, Integer> stat = new HashMap<>();
    private final LinkedList<Event> eventsQueue = new LinkedList<>();

    private static final int MINUTES_IN_HOUR = 60;

    public EventsStatisticsImpl(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void incEvent(String eventName) {
        stat.merge(eventName, 1, Integer::sum);
        eventsQueue.add(new Event(clock.instant(), eventName));
    }

    @Override
    public Optional<Double> getEventStatisticByName(String eventName) {
        recalculateStatistics();

        return Optional.ofNullable(stat.get(eventName)).map(count -> ((double) count) / MINUTES_IN_HOUR);
    }

    @Override
    public Map<String, Double> getAllEventStatistic() {
        recalculateStatistics();

        return getEventsRpmStream(stat)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void printStatistic() {
        getEventsRpmStream(stat)
                .forEach(entry -> System.out.printf("Event: %s, RPM: %s", entry.getKey(), entry.getValue()));
    }

    private Stream<Map.Entry<String, Double>> getEventsRpmStream(Map<String, Integer> eventsMap) {
        return eventsMap.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), ((double) entry.getValue()) / MINUTES_IN_HOUR));
    }

    private void recalculateStatistics() {
        Instant hourAgo = clock.instant().minus(1, ChronoUnit.HOURS);

        while (!eventsQueue.isEmpty() && eventsQueue.peek().time().isBefore(hourAgo)) {
            Event popEvent = eventsQueue.pop();

            stat.merge(popEvent.name(), 0, (oldCount, unused) -> oldCount - 1);
        }
    }
}
