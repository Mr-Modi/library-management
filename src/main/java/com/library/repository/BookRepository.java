package com.library.repository;

import com.library.entity.Book;
import com.library.enums.AvailabilityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Exclude soft-deleted books from all standard queries
    @Query("SELECT b FROM Book b WHERE b.deletedAt IS NULL")
    Page<Book> findAllActive(Pageable pageable);

    @Query("""
        SELECT b FROM Book b
        WHERE b.deletedAt IS NULL
          AND (:author IS NULL OR LOWER(b.author) = LOWER(:author))
          AND (:publishedYear IS NULL OR b.publishedYear = :publishedYear)
          AND (:status IS NULL OR b.availabilityStatus = :status)
        """)
    Page<Book> findAllWithFilters(
        @Param("author") String author,
        @Param("publishedYear") Integer publishedYear,
        @Param("status") AvailabilityStatus status,
        Pageable pageable
    );

    // Partial match search on title or author
    @Query("""
        SELECT b FROM Book b
        WHERE b.deletedAt IS NULL
          AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')))
        """)
    Page<Book> searchByTitleOrAuthor(@Param("query") String query, Pageable pageable);

    // ISBN uniqueness check - also consider soft-deleted to prevent reuse
    boolean existsByIsbn(String isbn);

    // Find active book by id
    @Query("SELECT b FROM Book b WHERE b.id = :id AND b.deletedAt IS NULL")
    Optional<Book> findActiveById(@Param("id") Long id);

    // Find active book by isbn (for duplicate check on update)
    @Query("SELECT b FROM Book b WHERE b.isbn = :isbn AND b.deletedAt IS NULL AND b.id <> :excludeId")
    Optional<Book> findActiveByIsbnExcluding(@Param("isbn") String isbn, @Param("excludeId") Long excludeId);
}
