package ma.inpt.tp4_api.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * Custom property naming strategy that translates field names based on locale
 */
public class LocalizedPropertyNamingStrategy extends PropertyNamingStrategy {

    private final MessageSource messageSource;

    public LocalizedPropertyNamingStrategy(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
        return translateFieldName(defaultName);
    }

    @Override
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return translateFieldName(defaultName);
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return translateFieldName(defaultName);
    }

    private String translateFieldName(String fieldName) {
        try {
            Locale locale = LocaleContextHolder.getLocale();
            String key = "field." + fieldName;
            return messageSource.getMessage(key, null, locale);
        } catch (Exception e) {
            // If no translation found, return original field name
            return fieldName;
        }
    }
}
