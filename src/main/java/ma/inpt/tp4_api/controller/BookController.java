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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.inpt.tp4_api.dto.ApiResponse;
import ma.inpt.tp4_api.modal.Book;
import ma.inpt.tp4_api.service.BookService;
import ma.inpt.tp4_api.util.ETag;
import ma.inpt.tp4_api.util.MessageTranslator;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Books", description = "Book management APIs (v1)")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private MessageTranslator messageTranslator;

    @GetMapping
    @Operation(summary = "Get all books", description = "Returns a list of all books with i18n messages and ETag caching",
            parameters = {
                @Parameter(name = "Accept-Language", in = ParameterIn.HEADER,
                        description = "Language preference",
                        schema = @Schema(type = "string", allowableValues = {"en", "fr"}, defaultValue = "en")),
                @Parameter(name = "Accept-Encoding", in = ParameterIn.HEADER,
                        description = "Compression preference",
                        schema = @Schema(type = "string", allowableValues = {"gzip", "br", "gzip, deflate, br"}, defaultValue = "gzip, deflate, br")),
                @Parameter(name = "If-None-Match", in = ParameterIn.HEADER,
                        description = "ETag from previous request for caching",
                        schema = @Schema(type = "string"))
            })
    public ResponseEntity<ApiResponse<List<Book>>> getAllBooks(
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        List<Book> books = bookService.getAll();
        String etag = ETag.generateETag(books);

        // If client's ETag matches, return 304 Not Modified
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        String message = books.isEmpty()
            ? messageTranslator.getMessage("book.list.empty")
            : messageTranslator.getMessage("api.success");

        // Return 200 with ETag header
        return ResponseEntity.ok()
                .eTag(etag)
                .body(ApiResponse.success(message, books));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Returns a single book by its ID with i18n messages and ETag caching",
            parameters = {
                @Parameter(name = "Accept-Language", in = ParameterIn.HEADER,
                        description = "Language preference",
                        schema = @Schema(type = "string", allowableValues = {"en", "fr"}, defaultValue = "en")),
                @Parameter(name = "Accept-Encoding", in = ParameterIn.HEADER,
                        description = "Compression preference",
                        schema = @Schema(type = "string", allowableValues = {"gzip", "br", "gzip, deflate, br"}, defaultValue = "gzip, deflate, br")),
                @Parameter(name = "If-None-Match", in = ParameterIn.HEADER,
                        description = "ETag from previous request for caching",
                        schema = @Schema(type = "string"))
            })
    public ResponseEntity<ApiResponse<Book>> getBookById(
            @PathVariable Long id,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        var bookOpt = bookService.getById(id);
        if (bookOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(messageTranslator.getMessage("book.notfound", id)));
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
                .body(ApiResponse.success(messageTranslator.getMessage("api.success"), book));
    }

    @PostMapping
    @Operation(summary = "Create a new book", description = "Creates a new book (ID is auto-generated) with i18n messages",
            parameters = {
                @Parameter(name = "Accept-Language", in = ParameterIn.HEADER,
                        description = "Language preference",
                        schema = @Schema(type = "string", allowableValues = {"en", "fr"}, defaultValue = "en")),
                @Parameter(name = "Accept-Encoding", in = ParameterIn.HEADER,
                        description = "Compression preference",
                        schema = @Schema(type = "string", allowableValues = {"gzip", "br", "gzip, deflate, br"}, defaultValue = "gzip, deflate, br"))
            })
    public ResponseEntity<ApiResponse<Book>> createBook(@RequestBody Book book) {
        // ID will be auto-generated, even if user tries to send one
        Book created = bookService.create(book);
        String message = messageTranslator.getMessage("book.created");
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(message, created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a book", description = "Updates an existing book with i18n messages and optimistic locking via If-Match",
            parameters = {
                @Parameter(name = "Accept-Language", in = ParameterIn.HEADER,
                        description = "Language preference",
                        schema = @Schema(type = "string", allowableValues = {"en", "fr"}, defaultValue = "en")),
                @Parameter(name = "Accept-Encoding", in = ParameterIn.HEADER,
                        description = "Compression preference",
                        schema = @Schema(type = "string", allowableValues = {"gzip", "br", "gzip, deflate, br"}, defaultValue = "gzip, deflate, br")),
                @Parameter(name = "If-Match", in = ParameterIn.HEADER,
                        description = "ETag for optimistic locking (prevents concurrent updates)",
                        schema = @Schema(type = "string"))
            })
    public ResponseEntity<ApiResponse<Book>> updateBook(
            @PathVariable Long id,
            @RequestBody Book book,
            @RequestHeader(value = "If-Match", required = false) String ifMatch) {

        // If client provides If-Match header, verify it matches current resource
        if (ifMatch != null) {
            var existingBookOpt = bookService.getById(id);
            if (existingBookOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(messageTranslator.getMessage("book.notfound", id)));
            }

            Book existingBook = existingBookOpt.get();
            String currentETag = ETag.generateETag(existingBook);

            // If ETags don't match, return 412 Precondition Failed
            if (!currentETag.equals(ifMatch)) {
                return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
                    .body(ApiResponse.error("ETag mismatch: resource has been modified by another request"));
            }
        }

        // ETags match (or no If-Match header), proceed with update
        try {
            Book updated = bookService.update(id, book);
            String newETag = ETag.generateETag(updated);
            String message = messageTranslator.getMessage("book.updated");

            return ResponseEntity.ok()
                    .eTag(newETag)
                    .body(ApiResponse.success(message, updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book", description = "Deletes a book by ID with i18n messages",
            parameters = {
                @Parameter(name = "Accept-Language", in = ParameterIn.HEADER,
                        description = "Language preference",
                        schema = @Schema(type = "string", allowableValues = {"en", "fr"}, defaultValue = "en")),
                @Parameter(name = "Accept-Encoding", in = ParameterIn.HEADER,
                        description = "Compression preference",
                        schema = @Schema(type = "string", allowableValues = {"gzip", "br", "gzip, deflate, br"}, defaultValue = "gzip, deflate, br"))
            })
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.delete(id);
        String message = messageTranslator.getMessage("book.deleted");
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }
}
