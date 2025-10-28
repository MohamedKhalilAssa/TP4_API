package ma.inpt.tp4_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import ma.inpt.tp4_api.modal.Book;
import ma.inpt.tp4_api.service.BookService;
import ma.inpt.tp4_api.util.ETag;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Books", description = "Book management APIs (v1)")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks(
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        List<Book> books = bookService.getAll();
        String etag = ETag.generateETag(books);

        // If client's ETag matches, return 304 Not Modified
        if (etag.equals(ifNoneMatch)) {
            System.out.println(ifNoneMatch);
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        // Return 200 with ETag header
        return ResponseEntity.ok()
                .eTag(etag)
                .body(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(
            @PathVariable Long id,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        var bookOpt = bookService.getById(id);
        if (bookOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Book book = bookOpt.get();
        String etag = ETag.generateETag(book);

        // If client's ETag matches, return 304 Not Modified
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .build();
        }

        // Return 200 with ETag header
        return ResponseEntity.ok()
                .eTag(etag)
                .body(book);
    }

    @PostMapping
    public Book createBook(@RequestBody Book book) {
        return bookService.create(book);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
            @PathVariable Long id,
            @RequestBody Book book,
            @RequestHeader(value = "If-Match", required = false) String ifMatch) {

        // If client provides If-Match header, verify it matches current resource
        if (ifMatch != null) {
            var existingBookOpt = bookService.getById(id);
            if (existingBookOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Book existingBook = existingBookOpt.get();
            String currentETag = ETag.generateETag(existingBook);

            // If ETags don't match, return 412 Precondition Failed
            if (!currentETag.equals(ifMatch)) {
                return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
            }
        }

        // ETags match (or no If-Match header), proceed with update
        Book updated = bookService.update(id, book);
        String newETag = ETag.generateETag(updated);

        return ResponseEntity.ok()
                .eTag(newETag)
                .body(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        bookService.delete(id);
    }
}