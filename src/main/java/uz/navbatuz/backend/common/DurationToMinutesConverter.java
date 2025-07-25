package uz.navbatuz.backend.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Duration;

@Converter(autoApply = true)
public class DurationToMinutesConverter implements AttributeConverter<Duration, Long> {

    @Override
    public Long convertToDatabaseColumn(Duration attribute) {
        if (attribute == null) return null;
        return attribute.toMinutes(); // store only minutes
    }

    @Override
    public Duration convertToEntityAttribute(Long dbData) {
        if (dbData == null) return null;
        return Duration.ofMinutes(dbData);
    }
}
