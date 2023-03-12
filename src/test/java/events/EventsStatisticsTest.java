package events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventsStatisticsTest {
    private EventsStatistics eventsStatistics;
    private EventsClock clock;

    private static final int MINUTES_IN_HOUR = 60;
    private static final String EVENT_1 = "Event1";
    private static final String EVENT_2 = "Event2";
    private static final double EPS = 1e-9;

    @BeforeEach
    void setUp() {
        clock = new EventsClock(Instant.now());
        eventsStatistics = new EventsStatisticsImpl(clock);
    }

    @Test
    void testEmptyStatistics() {
        assertEquals(Optional.empty(), eventsStatistics.getEventStatisticByName(EVENT_1));
        assertEquals(0, eventsStatistics.getAllEventStatistic().size());
    }

    @Test
    void testAddEvent() {
        IntStream.range(0, MINUTES_IN_HOUR)
                .forEach(v -> eventsStatistics.incEvent(EVENT_1));

        checkStatByName(EVENT_1, 1.0);
    }

    @Test
    void testAddManyEvents() {
        IntStream.range(0, MINUTES_IN_HOUR)
                .forEach(v -> {
                    eventsStatistics.incEvent(EVENT_1);
                    eventsStatistics.incEvent(EVENT_2);
                    eventsStatistics.incEvent(EVENT_2);
                });

        checkStatByName(EVENT_1, 1.0);
        checkStatByName(EVENT_2, 2.0);
    }

    @Test
    void testDifferentTimes() {
        fillStatisticsWithEvent1(MINUTES_IN_HOUR / 10, 10);

        checkStatByName(EVENT_1, 0.1);
    }

    @Test
    void testPartiallyOutdatedEvents() {
        fillStatisticsWithEvent1(MINUTES_IN_HOUR * 3 / 2, 1);

        checkStatByName(EVENT_1, 1.0);
    }

    @Test
    void testEventsIncStopped() {
        fillStatisticsWithEvent1(MINUTES_IN_HOUR, 1);

        clock.setInstant(clock.instant().plus(30, ChronoUnit.MINUTES));

        checkStatByName(EVENT_1, 0.5);
    }

    @Test
    void testAllOutdatedEvents() {
        fillStatisticsWithEvent1(MINUTES_IN_HOUR, 1);

        clock.setInstant(clock.instant().plus(1, ChronoUnit.HOURS));

        checkStatByName(EVENT_1, 0.0);
    }

    private void fillStatisticsWithEvent1(int untilMinute, int clockAdd) {
        IntStream.range(0, untilMinute)
                .forEach(v -> {
                    eventsStatistics.incEvent(EVENT_1);
                    clock.setInstant(clock.instant().plus(clockAdd, ChronoUnit.MINUTES));
                });
    }

    private void checkStatByName(String eventName, double expectedRpm) {
        assertTrue(eventsStatistics.getEventStatisticByName(eventName).isPresent());
        assertEquals(expectedRpm, eventsStatistics.getEventStatisticByName(eventName).get(), EPS);
    }
}
