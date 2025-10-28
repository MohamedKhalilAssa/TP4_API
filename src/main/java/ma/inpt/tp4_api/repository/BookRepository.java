package ma.inpt.tp4_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ma.inpt.tp4_api.modal.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
}
