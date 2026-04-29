package com.library.controller;

import com.library.service.WishlistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishlistController.class)
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WishlistService wishlistService;

    @Test
    void addToWishlist_ReturnsNoContent() throws Exception {
        doNothing().when(wishlistService).addToWishlist(1L, 1L);

        mockMvc.perform(put("/api/v1/users/1/wishlist/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeFromWishlist_ReturnsNoContent() throws Exception {
        doNothing().when(wishlistService).removeFromWishlist(1L, 1L);

        mockMvc.perform(delete("/api/v1/users/1/wishlist/1"))
                .andExpect(status().isNoContent());
    }
}
