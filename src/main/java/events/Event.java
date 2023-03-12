package events;

import java.time.Instant;

public record Event(Instant time, String name) {
}
