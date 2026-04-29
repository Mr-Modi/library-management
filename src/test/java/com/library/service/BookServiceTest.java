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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private BookRequest bookRequest;

    @BeforeEach
    void setUp() {
        book = Book.builder()
                .id(1L)
                .title("Test Book")
                .author("Test Author")
                .isbn("1234567890")
                .publishedYear(2020)
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .build();

        bookRequest = new BookRequest();
        bookRequest.setTitle("Test Book");
        bookRequest.setAuthor("Test Author");
        bookRequest.setIsbn("1234567890");
        bookRequest.setPublishedYear(2020);
    }

    @Test
    void createBook_Success() {
        when(bookRepository.existsByIsbn(bookRequest.getIsbn())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookResponse response = bookService.createBook(bookRequest);

        assertThat(response).isNotNull();
        assertThat(response.getIsbn()).isEqualTo(bookRequest.getIsbn());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createBook_DuplicateIsbn_ThrowsException() {
        when(bookRepository.existsByIsbn(bookRequest.getIsbn())).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(bookRequest))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createBook_FutureYear_ThrowsException() {
        bookRequest.setPublishedYear(Year.now().getValue() + 1);

        assertThatThrownBy(() -> bookService.createBook(bookRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getBook_Success() {
        when(bookRepository.findActiveById(1L)).thenReturn(Optional.of(book));

        BookResponse response = bookService.getBook(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getBook_NotFound_ThrowsException() {
        when(bookRepository.findActiveById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBook(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listBooks_NoFilters_ReturnsPagedResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<>(List.of(book));
        when(bookRepository.findAllActive(pageable)).thenReturn(page);

        PagedResponse<BookResponse> response = bookService.listBooks(null, null, null, pageable);

        assertThat(response.getContent()).hasSize(1);
        verify(bookRepository).findAllActive(pageable);
    }

    @Test
    void listBooks_WithFilters_ReturnsPagedResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<>(List.of(book));
        when(bookRepository.findAllWithFilters(any(), any(), any(), any())).thenReturn(page);

        PagedResponse<BookResponse> response = bookService.listBooks("Author", 2020, AvailabilityStatus.AVAILABLE, pageable);

        assertThat(response.getContent()).hasSize(1);
        verify(bookRepository).findAllWithFilters(eq("Author"), eq(2020), eq(AvailabilityStatus.AVAILABLE), eq(pageable));
    }

    @Test
    void updateBook_Success() {
        BookUpdateRequest updateRequest = new BookUpdateRequest();
        updateRequest.setTitle("Updated Title");
        when(bookRepository.findActiveById(1L)).thenReturn(Optional.of(book));

        BookResponse response = bookService.updateBook(1L, updateRequest);

        assertThat(response.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void updateBook_StatusTransition_PublishesEvent() {
        book.setAvailabilityStatus(AvailabilityStatus.BORROWED);
        BookUpdateRequest updateRequest = new BookUpdateRequest();
        updateRequest.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        
        when(bookRepository.findActiveById(1L)).thenReturn(Optional.of(book));

        bookService.updateBook(1L, updateRequest);

        verify(eventPublisher).publishEvent(any(BookAvailableEvent.class));
    }

    @Test
    void updateBook_DuplicateIsbn_ThrowsException() {
        BookUpdateRequest updateRequest = new BookUpdateRequest();
        updateRequest.setIsbn("new-isbn");
        when(bookRepository.findActiveById(1L)).thenReturn(Optional.of(book));
        doThrow(DataIntegrityViolationException.class).when(bookRepository).flush();

        assertThatThrownBy(() -> bookService.updateBook(1L, updateRequest))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void deleteBook_Success() {
        when(bookRepository.findActiveById(1L)).thenReturn(Optional.of(book));

        bookService.deleteBook(1L);

        assertThat(book.getDeletedAt()).isNotNull();
        verify(bookRepository).save(book);
    }

    @Test
    void searchBooks_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<>(List.of(book));
        when(bookRepository.searchByTitleOrAuthor(anyString(), any())).thenReturn(page);

        PagedResponse<BookResponse> response = bookService.searchBooks("query", pageable);

        assertThat(response.getContent()).hasSize(1);
        verify(bookRepository).searchByTitleOrAuthor(eq("query"), eq(pageable));
    }
}
