package ma.inpt.tp4_api.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Utility class to translate messages based on current locale
 */
@Component
public class MessageTranslator {

    @Autowired
    private MessageSource messageSource;

    /**
     * Get translated message for current locale
     * @param key message key from properties file
     * @return translated message
     */
    public String getMessage(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, null, locale);
    }

    /**
     * Get translated message with parameters
     * @param key message key from properties file
     * @param params parameters to insert into message
     * @return translated message with parameters
     */
    public String getMessage(String key, Object... params) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, params, locale);
    }
}
