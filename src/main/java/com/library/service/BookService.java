package com.library.service;

import com.library.dto.BookRequest;
import com.library.dto.BookResponse;
import com.library.dto.BookUpdateRequest;
import com.library.dto.PagedResponse;
import com.library.entity.Book;
import com.library.enums.AvailabilityStatus;
import com.library.event.BookAvailableEvent;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.time.Year;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public BookResponse createBook(BookRequest request) {
        validatePublishedYear(request.getPublishedYear());

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw DuplicateResourceException.isbn(request.getIsbn());
        }

        Book book = Book.builder().title(request.getTitle())
                .author(request.getAuthor()).isbn(request.getIsbn())
                .publishedYear(request.getPublishedYear())
                .availabilityStatus(request.getAvailabilityStatus() != null ?
                        request.getAvailabilityStatus() : AvailabilityStatus.AVAILABLE)
                .build();

        Book saved = bookRepository.save(book);
        log.info("Created book id={} isbn={}", saved.getId(), saved.getIsbn());
        return BookResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public BookResponse getBook(Long id) {
        return BookResponse.from(findActiveOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookResponse> listBooks(String author, Integer publishedYear,
                                                 AvailabilityStatus status, Pageable pageable) {
        boolean hasFilters = author != null || publishedYear != null || status != null;
        Page<Book> bookPage = hasFilters ? bookRepository.findAllWithFilters(author, publishedYear, status, pageable) : bookRepository.findAllActive(pageable);

        return PagedResponse.from(bookPage.map(BookResponse::from));
    }

    @Transactional
    public BookResponse updateBook(Long id, BookUpdateRequest request) {
        Book book = findActiveOrThrow(id);
        AvailabilityStatus previousStatus = book.getAvailabilityStatus();
        applyUpdates(book, request);

        if (request.getIsbn() != null && !request.getIsbn().equals(book.getIsbn())) {
            book.setIsbn(request.getIsbn());
            try {
                bookRepository.flush();
            } catch (DataIntegrityViolationException ex) {
                throw DuplicateResourceException.isbn(request.getIsbn());
            }
        }
        log.info("Updated book id={}", book.getId());
        if (isTransition(previousStatus, book.getAvailabilityStatus())) {
            log.info("Book id={} transitioned BORROWED→AVAILABLE", id);
            eventPublisher.publishEvent(new BookAvailableEvent(book.getId(), book.getTitle()));
        }
        return BookResponse.from(book);
    }

    private boolean isTransition(AvailabilityStatus from, AvailabilityStatus to) {
        return from == AvailabilityStatus.BORROWED && to == AvailabilityStatus.AVAILABLE;
    }

    private void applyUpdates(Book book, BookUpdateRequest request) {
        if (request.getTitle() != null) book.setTitle(request.getTitle());
        if (request.getAuthor() != null) book.setAuthor(request.getAuthor());

        if (request.getPublishedYear() != null) {
            validatePublishedYear(request.getPublishedYear());
            book.setPublishedYear(request.getPublishedYear());
        }

        if (request.getAvailabilityStatus() != null) {
            book.setAvailabilityStatus(request.getAvailabilityStatus());
        }
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = findActiveOrThrow(id);
        book.setDeletedAt(LocalDateTime.now());
        book.setDeletedBy("system");
        bookRepository.save(book);
        log.info("Soft Deleted book id={}", id);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookResponse> searchBooks(String query, Pageable pageable) {
        Page<Book> result = bookRepository.searchByTitleOrAuthor(query.trim(), pageable);
        return PagedResponse.from(result.map(BookResponse::from));
    }

    private Book findActiveOrThrow(Long id) {
        return bookRepository.findActiveById(id).orElseThrow(() -> ResourceNotFoundException.book(id));
    }

    private void validatePublishedYear(int year) {
        int currentYear = Year.now().getValue();
        if (year > currentYear) {
            throw new IllegalArgumentException("publishedYear cannot be in the future. Current year: " + currentYear);
        }
    }
}
