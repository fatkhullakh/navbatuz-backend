package uz.navbatuz.backend.availability.dto;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public record TimeRange(LocalTime start, LocalTime end) {
    public List<TimeRange> subtract(LocalTime s, LocalTime e) {
        List<TimeRange> result = new ArrayList<>();
        if(s.isAfter(end) || e.isBefore(start)) {
            result.add(this);
        } else {
            if (s.isAfter(start)) result.add(new TimeRange(start, s));
            if (s.isBefore(end)) result.add(new TimeRange(e, end));
        }
        return result;
    }
}
