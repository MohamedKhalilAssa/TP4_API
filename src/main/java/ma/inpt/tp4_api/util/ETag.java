package ma.inpt.tp4_api.util;

import java.util.List;

import ma.inpt.tp4_api.modal.Book;

public class ETag {

    public static String generateETag(Book book) {
        return Integer.toHexString(book.hashCode());
    }

    public static String generateETag(List<Book> books) {
        return Integer.toHexString(books.hashCode());
    }
}