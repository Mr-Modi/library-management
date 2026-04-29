package com.library.service;

import com.library.entity.Book;
import com.library.entity.User;
import com.library.entity.Wishlist;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.UserRepository;
import com.library.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private WishlistService wishlistService;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@example.com").build();
        book = Book.builder().id(1L).title("Test Book").build();
    }

    @Test
    void addToWishlist_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookRepository.findActiveById(1L)).thenReturn(Optional.of(book));
        when(wishlistRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(false);
        when(userRepository.getReferenceById(1L)).thenReturn(user);

        wishlistService.addToWishlist(1L, 1L);

        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    void addToWishlist_UserNotFound_ThrowsException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> wishlistService.addToWishlist(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addToWishlist_BookNotFound_ThrowsException() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookRepository.findActiveById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.addToWishlist(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addToWishlist_DuplicateEntry_ThrowsException() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookRepository.findActiveById(1L)).thenReturn(Optional.of(book));
        when(wishlistRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> wishlistService.addToWishlist(1L, 1L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void removeFromWishlist_Success() {
        Wishlist wishlist = Wishlist.builder().user(user).book(book).build();
        when(wishlistRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(true);
        when(wishlistRepository.findByBookIdWithUser(1L)).thenReturn(List.of(wishlist));

        wishlistService.removeFromWishlist(1L, 1L);

        verify(wishlistRepository).delete(wishlist);
    }

    @Test
    void removeFromWishlist_NotFound_ThrowsException() {
        when(wishlistRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> wishlistService.removeFromWishlist(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
