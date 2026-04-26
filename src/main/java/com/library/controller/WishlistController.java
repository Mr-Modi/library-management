package com.library.controller;

import com.library.service.WishlistService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    // PUT /api/v1/users/{userId}/wishlist/{bookId}
    @PutMapping("/{bookId}")
    public ResponseEntity<Void> addToWishlist(@PathVariable @Min(1) Long userId,
                                              @PathVariable @Min(1) Long bookId) {
        wishlistService.addToWishlist(userId, bookId);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/v1/users/{userId}/wishlist/{bookId}
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable @Min(1) Long userId,
                                                   @PathVariable @Min(1) Long bookId) {
        wishlistService.removeFromWishlist(userId, bookId);
        return ResponseEntity.noContent().build();
    }
}
