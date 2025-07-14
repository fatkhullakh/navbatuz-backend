package uz.navbatuz.backend.common;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageSource messageSource;

    public String get(String key, Locale locale) {
        return messageSource.getMessage(key, null, locale);
    }

    public String get(String key, Object[] args, Locale locale) {
        return messageSource.getMessage(key, args, locale);
    }
}
