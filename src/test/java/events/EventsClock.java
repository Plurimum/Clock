package events;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class EventsClock extends Clock {
    private Instant instant;
    private final ZoneId zoneId;

    public EventsClock(Instant instant, ZoneId zoneId) {
        this.instant = instant;
        this.zoneId = zoneId;
    }

    public EventsClock(Instant instant) {
        this(instant, ZoneId.of("Europe/Moscow"));
    }

    @Override
    public ZoneId getZone() {
        return zoneId;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new EventsClock(this.instant, zone);
    }

    @Override
    public Instant instant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }
}
