package ma.inpt.tp4_api.service;

import ma.inpt.tp4_api.modal.Book;
import ma.inpt.tp4_api.repository.BookRepository;
import ma.inpt.tp4_api.util.MessageTranslator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MessageTranslator messageTranslator;

    public List<Book> getAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> getById(Long id) {
        return bookRepository.findById(id);
    }

    public Book create(Book book) {
        // Ensure ID is null so database auto-generates it
        book.setId(null);
        return bookRepository.save(book);
    }

    public Book update(Long id, Book updated) {
        // Check if book exists first
        return bookRepository.findById(id)
                .map(existing -> {
                    // Set the ID from path variable (not from request body)
                    updated.setId(id);
                    return bookRepository.save(updated);
                })
                .orElseThrow(() -> new RuntimeException(
                    messageTranslator.getMessage("book.notfound", id)
                ));
    }

    public void delete(Long id) {
        bookRepository.deleteById(id);
    }
}
