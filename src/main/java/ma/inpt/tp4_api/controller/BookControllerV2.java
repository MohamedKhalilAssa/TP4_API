package ma.inpt.tp4_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.inpt.tp4_api.dto.ApiResponse;
import ma.inpt.tp4_api.modal.Book;
import ma.inpt.tp4_api.service.BookService;
import ma.inpt.tp4_api.util.MessageTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/books") // Versioning
@Tag(name = "Books V2", description = "Book management APIs (v2) with i18n support")
public class BookControllerV2 {

    @Autowired
    private BookService bookService;

    @Autowired
    private MessageTranslator messageTranslator;

    @GetMapping
    @Operation(summary = "Get all books", description = "Returns a list of all books with i18n messages",
            parameters = {
                @Parameter(name = "Accept-Language", in = ParameterIn.HEADER,
                        description = "Language preference",
                        schema = @Schema(type = "string", allowableValues = {"en", "fr", "ar", "es"}, defaultValue = "en")),
                @Parameter(name = "Accept-Encoding", in = ParameterIn.HEADER,
                        description = "Compression preference",
                        schema = @Schema(type = "string", allowableValues = {"gzip", "br", "gzip, deflate, br"}, defaultValue = "gzip, deflate, br"))
            })
    public ResponseEntity<ApiResponse<List<Book>>> getAllBooks() {
        List<Book> books = bookService.getAll();
        String message = books.isEmpty()
            ? messageTranslator.getMessage("book.list.empty")
            : messageTranslator.getMessage("api.success");
        return ResponseEntity.ok(ApiResponse.success(message, books));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Returns a single book by its ID with i18n messages",
            parameters = {
                @Parameter(name = "Accept-Language", in = ParameterIn.HEADER,
                        description = "Language preference",
                        schema = @Schema(type = "string", allowableValues = {"en", "fr", "ar", "es"}, defaultValue = "en")),
                @Parameter(name = "Accept-Encoding", in = ParameterIn.HEADER,
                        description = "Compression preference",
                        schema = @Schema(type = "string", allowableValues = {"gzip", "br", "gzip, deflate, br"}, defaultValue = "gzip, deflate, br"))
            })
    public ResponseEntity<ApiResponse<Book>> getBookById(@PathVariable Long id) {
        return bookService.getById(id)
                .map(book -> ResponseEntity.ok(
                    ApiResponse.success(messageTranslator.getMessage("api.success"), book)
                ))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(messageTranslator.getMessage("book.notfound", id))));
    }

    @PostMapping
    @Operation(summary = "Create a new book", description = "Creates a new book (ID is auto-generated) with i18n messages",
            parameters = {
                @Parameter(name = "Accept-Language", in = ParameterIn.HEADER,
                        description = "Language preference",
                        schema = @Schema(type = "string", allowableValues = {"en", "fr", "ar", "es"}, defaultValue = "en")),
                @Parameter(name = "Accept-Encoding", in = ParameterIn.HEADER,
                        description = "Compression preference",
                        schema = @Schema(type = "string", allowableValues = {"gzip", "br", "gzip, deflate, br"}, defaultValue = "gzip, deflate, br"))
            })
    public ResponseEntity<ApiResponse<Book>> createBook(@RequestBody Book book) {
        Book created = bookService.create(book);
        String message = messageTranslator.getMessage("book.created");
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(message, created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a book", description = "Updates an existing book with i18n messages",
            parameters = {
                @Parameter(name = "Accept-Language", in = ParameterIn.HEADER,
                        description = "Language preference",
                        schema = @Schema(type = "string", allowableValues = {"en", "fr", "ar", "es"}, defaultValue = "en")),
                @Parameter(name = "Accept-Encoding", in = ParameterIn.HEADER,
                        description = "Compression preference",
                        schema = @Schema(type = "string", allowableValues = {"gzip", "br", "gzip, deflate, br"}, defaultValue = "gzip, deflate, br"))
            })
    public ResponseEntity<ApiResponse<Book>> updateBook(@PathVariable Long id, @RequestBody Book book) {
        try {
            Book updated = bookService.update(id, book);
            String message = messageTranslator.getMessage("book.updated");
            return ResponseEntity.ok(ApiResponse.success(message, updated));
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
                        schema = @Schema(type = "string", allowableValues = {"en", "fr", "ar", "es"}, defaultValue = "en")),
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