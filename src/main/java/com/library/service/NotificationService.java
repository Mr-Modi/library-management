package com.library.service;

import com.library.entity.Wishlist;
import com.library.event.BookAvailableEvent;
import com.library.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final WishlistRepository wishlistRepository;

    /**
     * Handles the BookAvailableEvent asynchronously.
     * Runs in a separate thread (notificationExecutor pool), so the HTTP response
     * is returned to the caller before this method begins processing.
     */
    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBookAvailable(BookAvailableEvent event) {
        log.info("[Notification] Starting wishlist notification job for book id={} title='{}'",
            event.getBookId(), event.getBookTitle());

        List<Wishlist> wishlists = wishlistRepository.findByBookIdWithUser(event.getBookId());

        if (wishlists.isEmpty()) {
            log.info("[Notification] No wishlist entries found for book id={}", event.getBookId());
            return;
        }

        log.info("[Notification] Found {} user(s) to notify for book '{}'",
            wishlists.size(), event.getBookTitle());

        for (Wishlist entry : wishlists) {
            sendNotification(entry.getUser().getId(), entry.getUser().getEmail(), event.getBookTitle());
        }

        log.info("[Notification] Completed wishlist notification job for book id={}", event.getBookId());
    }

    /**
     * Simulates sending a notification.
     * In production this would call an email provider, push service, SMS gateway, etc.
     */
    private void sendNotification(Long userId, String userEmail, String bookTitle) {
        log.info("Notification prepared for user_id={} ({}): Book '{}' is now available.",
            userId, userEmail, bookTitle);
    }
}
