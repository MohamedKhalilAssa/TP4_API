# Internationalization (i18n) Setup Guide

## ✅ i18n is now fully configured!

Your Spring Boot API now supports **4 languages** out of the box:
- 🇬🇧 English (default)
- 🇫🇷 French
- 🇸🇦 Arabic
- 🇪🇸 Spanish

---

## How It Works

The API automatically detects the client's preferred language from the `Accept-Language` HTTP header and returns messages in that language.

### Request Flow:
1. Client sends request with `Accept-Language: fr` header
2. Spring Boot detects the locale (French)
3. MessageTranslator loads messages from `messages_fr.properties`
4. API response returns messages in French

---

## What Was Configured

### 1. Configuration Files Created:

**Message Properties Files** (in `src/main/resources/i18n/`):
- `messages.properties` - English (default)
- `messages_fr.properties` - French
- `messages_ar.properties` - Arabic
- `messages_es.properties` - Spanish

### 2. Java Classes Created:

- **`LocaleConfig.java`** - Configures locale resolution from Accept-Language header
- **`MessageTranslator.java`** - Utility to translate messages based on current locale
- **`ApiResponse.java`** - Generic response wrapper for consistent API responses

### 3. Updated Classes:

- **`BookController.java`** - Now returns internationalized messages
- **`BookControllerV2.java`** - V2 API with full i18n support
- **`BookService.java`** - Uses translated error messages
- **`application.properties`** - Added i18n configuration

---

## Testing i18n

### Test 1: English (Default)
```bash
curl -H "Accept-Language: en" http://localhost:8080/api/v1/books
```
**Response:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": [...]
}
```

### Test 2: French
```bash
curl -H "Accept-Language: fr" http://localhost:8080/api/v1/books
```
**Response:**
```json
{
  "success": true,
  "message": "Opération réussie",
  "data": [...]
}
```

### Test 3: Arabic
```bash
curl -H "Accept-Language: ar" http://localhost:8080/api/v1/books
```
**Response:**
```json
{
  "success": true,
  "message": "العملية ناجحة",
  "data": [...]
}
```

### Test 4: Spanish
```bash
curl -H "Accept-Language: es" http://localhost:8080/api/v1/books
```
**Response:**
```json
{
  "success": true,
  "message": "Operación exitosa",
  "data": [...]
}
```

### Test 5: Error Message (Book Not Found) - French
```bash
curl -H "Accept-Language: fr" http://localhost:8080/api/v1/books/999
```
**Response:**
```json
{
  "success": false,
  "message": "Livre non trouvé avec l'id: 999",
  "data": null
}
```

---

## API Response Format

All API endpoints now return a consistent format:

```json
{
  "success": true,          // boolean: operation success status
  "message": "...",         // string: translated message
  "data": {...}             // object: actual data (Book, List<Book>, or null)
}
```

---

## Browser Testing

Browsers automatically send the `Accept-Language` header based on user preferences.

1. Open browser and set language preferences:
   - Chrome: Settings → Languages
   - Firefox: Settings → Language
   
2. Visit: `http://localhost:8080/api/v1/books`

3. Messages will automatically be in your browser's language!

---

## Supported Messages

### Book Operations:
- `book.notfound` - Book not found with id: {0}
- `book.created` - Book created successfully
- `book.updated` - Book updated successfully
- `book.deleted` - Book deleted successfully
- `book.list.empty` - No books available

### API Messages:
- `api.success` - Operation successful
- `api.error` - An error occurred
- `api.unauthorized` - Unauthorized access
- `api.forbidden` - Access forbidden

### Validation Messages:
- `validation.title.required` - Title is required
- `validation.author.required` - Author is required
- `validation.price.invalid` - Price must be greater than 0
- `validation.year.invalid` - Year must be a valid year

---

## Adding More Languages

To add a new language (e.g., German):

1. Create file: `src/main/resources/i18n/messages_de.properties`
2. Add translations:
   ```properties
   book.notfound=Buch nicht gefunden mit ID: {0}
   book.created=Buch erfolgreich erstellt
   api.success=Vorgang erfolgreich
   ```
3. Restart application
4. Test with: `Accept-Language: de`

---

## Adding More Messages

1. Open all `messages*.properties` files
2. Add the same key to all files with appropriate translations:

**messages.properties** (English):
```properties
book.outofstock=Book is out of stock
```

**messages_fr.properties** (French):
```properties
book.outofstock=Le livre est en rupture de stock
```

**messages_ar.properties** (Arabic):
```properties
book.outofstock=الكتاب غير متوفر في المخزون
```

3. Use in code:
```java
String message = messageTranslator.getMessage("book.outofstock");
```

---

## Using MessageTranslator in Your Code

```java
@Autowired
private MessageTranslator messageTranslator;

// Simple message
String msg = messageTranslator.getMessage("api.success");

// Message with parameters
String msg = messageTranslator.getMessage("book.notfound", bookId);

// Message with multiple parameters (use {0}, {1}, etc. in properties file)
String msg = messageTranslator.getMessage("book.price.range", minPrice, maxPrice);
```

---

## Example API Responses

### GET /api/v1/books (Empty list - French)
```json
{
  "success": true,
  "message": "Aucun livre disponible",
  "data": []
}
```

### POST /api/v1/books (Create - Arabic)
```json
{
  "success": true,
  "message": "تم إنشاء الكتاب بنجاح",
  "data": {
    "id": 1,
    "title": "Clean Code",
    "author": "Robert Martin",
    "category": "Programming",
    "year": 2008,
    "price": 29.99
  }
}
```

### PUT /api/v1/books/1 (Update - Spanish)
```json
{
  "success": true,
  "message": "Libro actualizado con éxito",
  "data": {
    "id": 1,
    "title": "Clean Code",
    ...
  }
}
```

### DELETE /api/v1/books/1 (Delete - English)
```json
{
  "success": true,
  "message": "Book deleted successfully",
  "data": null
}
```

---

## Configuration Reference

### application.properties
```properties
# i18n Configuration
spring.messages.basename=i18n/messages          # Location of message files
spring.messages.encoding=UTF-8                  # Encoding (important for Arabic, etc.)
spring.messages.fallback-to-system-locale=false # Always use default if not found
spring.web.locale=en                            # Default locale
spring.web.locale-resolver=accept_header        # Resolve from Accept-Language header
```

---

## Multiple Language Priority

If client sends: `Accept-Language: fr-FR, fr;q=0.9, en-US;q=0.8, en;q=0.7`

Spring Boot will try:
1. `messages_fr_FR.properties` (French - France)
2. `messages_fr.properties` (French - General)
3. `messages.properties` (Default - English)

---

## Testing with Postman

1. Open Postman
2. Create a GET request to: `http://localhost:8080/api/v1/books`
3. Go to **Headers** tab
4. Add header:
   - Key: `Accept-Language`
   - Value: `fr` (or `ar`, `es`, etc.)
5. Send request
6. See translated messages in response!

---

## Summary

✅ **4 languages supported** (English, French, Arabic, Spanish)  
✅ **Automatic language detection** from Accept-Language header  
✅ **Consistent API responses** with ApiResponse wrapper  
✅ **All endpoints internationalized** (v1 and v2)  
✅ **Easy to add more languages** - just create new properties file  
✅ **Easy to add more messages** - add to all properties files  

**No code changes needed when adding new languages or messages!**

