package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.BookRequest;
import com.library.dto.BookResponse;
import com.library.dto.BookUpdateRequest;
import com.library.dto.PagedResponse;
import com.library.enums.AvailabilityStatus;
import com.library.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBook_ReturnsCreated() throws Exception {
        BookRequest request = new BookRequest();
        request.setTitle("Test Book");
        request.setAuthor("Test Author");
        request.setIsbn("1234567890");
        request.setPublishedYear(2020);

        BookResponse response = BookResponse.builder()
                .id(1L)
                .title("Test Book")
                .author("Test Author")
                .isbn("1234567890")
                .publishedYear(2020)
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .build();

        when(bookService.createBook(any(BookRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    void getBook_ReturnsOk() throws Exception {
        BookResponse response = BookResponse.builder()
                .id(1L)
                .title("Test Book")
                .build();

        when(bookService.getBook(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    void listBooks_ReturnsOk() throws Exception {
        PagedResponse<BookResponse> response = new PagedResponse<>();
        response.setContent(List.of(new BookResponse()));

        when(bookService.listBooks(any(), any(), any(), any(Pageable.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void updateBook_ReturnsOk() throws Exception {
        BookUpdateRequest request = new BookUpdateRequest();
        request.setTitle("Updated Title");

        BookResponse response = BookResponse.builder()
                .id(1L)
                .title("Updated Title")
                .build();

        when(bookService.updateBook(eq(1L), any(BookUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void deleteBook_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void searchBooks_ReturnsOk() throws Exception {
        PagedResponse<BookResponse> response = new PagedResponse<>();
        response.setContent(List.of(new BookResponse()));

        when(bookService.searchBooks(eq("query"), any(Pageable.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/books/search")
                .param("query", "query"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
