package com.library.service;

import com.library.entity.Book;
import com.library.entity.User;
import com.library.entity.Wishlist;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.UserRepository;
import com.library.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Transactional
    public void addToWishlist(Long userId, Long bookId) {
        if (!userRepository.existsById(userId)) {
            throw ResourceNotFoundException.user(userId);
        }

        Book book = bookRepository.findActiveById(bookId)
            .orElseThrow(() -> ResourceNotFoundException.book(bookId));

        if (wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new DuplicateResourceException(
                "Book is already in the wishlist for user_id=" + userId);
        }

        User userRef = userRepository.getReferenceById(userId);
        Wishlist entry = Wishlist.builder()
            .user(userRef)
            .book(book)
            .build();

        wishlistRepository.save(entry);
        log.info("Added book id={} to wishlist for user id={}", bookId, userId);
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long bookId) {
        if (!wishlistRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new ResourceNotFoundException(
                "Wishlist entry not found for user_id=" + userId + " and book_id=" + bookId);
        }

        wishlistRepository.findByBookIdWithUser(bookId).stream()
            .filter(w -> w.getUser().getId().equals(userId))
            .findFirst()
            .ifPresent(wishlistRepository::delete);

        log.info("Removed book id={} from wishlist for user id={}", bookId, userId);
    }
}
