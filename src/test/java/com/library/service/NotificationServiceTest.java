package com.library.service;

import com.library.entity.Book;
import com.library.entity.User;
import com.library.entity.Wishlist;
import com.library.event.BookAvailableEvent;
import com.library.repository.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void handleBookAvailable_WithEntries_SendsNotifications() {
        BookAvailableEvent event = new BookAvailableEvent(1L, "Test Book");
        User user = User.builder().id(1L).email("test@example.com").build();
        Book book = Book.builder().id(1L).title("Test Book").build();
        Wishlist wishlist = Wishlist.builder().user(user).book(book).build();

        when(wishlistRepository.findByBookIdWithUser(1L)).thenReturn(List.of(wishlist));

        notificationService.handleBookAvailable(event);

        verify(wishlistRepository).findByBookIdWithUser(1L);
        // sendNotification is private and just logs, so we mainly verify it didn't crash and called the repository
    }

    @Test
    void handleBookAvailable_NoEntries_DoesNothing() {
        BookAvailableEvent event = new BookAvailableEvent(1L, "Test Book");
        when(wishlistRepository.findByBookIdWithUser(1L)).thenReturn(Collections.emptyList());

        notificationService.handleBookAvailable(event);

        verify(wishlistRepository).findByBookIdWithUser(1L);
    }
}
