package ma.inpt.tp4_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.inpt.tp4_api.modal.Book;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DTO for Book with localized field names
 */
public class LocalizedBook {

    public static Map<String, Object> fromBook(Book book, MessageSource messageSource) {
        Map<String, Object> localizedBook = new LinkedHashMap<>();
        localizedBook.put(getFieldName("id", messageSource), book.getId());
        localizedBook.put(getFieldName("title", messageSource), book.getTitle());
        localizedBook.put(getFieldName("author", messageSource), book.getAuthor());
        localizedBook.put(getFieldName("category", messageSource), book.getCategory());
        localizedBook.put(getFieldName("year", messageSource), book.getYear());
        localizedBook.put(getFieldName("price", messageSource), book.getPrice());
        return localizedBook;
    }

    public static List<Map<String, Object>> fromBooks(List<Book> books, MessageSource messageSource) {
        return books.stream()
                .map(book -> fromBook(book, messageSource))
                .collect(Collectors.toList());
    }

    private static String getFieldName(String fieldName, MessageSource messageSource) {
        try {
            return messageSource.getMessage("field." + fieldName, null, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return fieldName;
        }
    }
}
