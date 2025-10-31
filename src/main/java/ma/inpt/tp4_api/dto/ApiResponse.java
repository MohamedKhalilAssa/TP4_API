package ma.inpt.tp4_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic API Response wrapper for internationalized messages
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    /**
     * Convert to a Map with localized field names
     */
    public Map<String, Object> toLocalizedMap(MessageSource messageSource) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(getFieldName("success", messageSource), success);
        map.put(getFieldName("message", messageSource), message);
        map.put(getFieldName("data", messageSource), data);
        return map;
    }

    private String getFieldName(String fieldName, MessageSource messageSource) {
        try {
            return messageSource.getMessage("field." + fieldName, null, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return fieldName;
        }
    }
}
