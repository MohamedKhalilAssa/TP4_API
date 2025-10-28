package ma.inpt.tp4_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.inpt.tp4_api.modal.Book;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for Book with localized field names
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalizedBook {
    private Long id;
    private String title;
    private String author;
    private String category;
    private int year;
    private double price;

    public static LocalizedBook fromBook(Book book) {
        return new LocalizedBook(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getCategory(),
            book.getYear(),
            book.getPrice()
        );
    }

    public static List<LocalizedBook> fromBooks(List<Book> books) {
        return books.stream()
                .map(LocalizedBook::fromBook)
                .collect(Collectors.toList());
    }
}

