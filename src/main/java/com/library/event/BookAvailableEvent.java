package com.library.event;

import lombok.Getter;

@Getter
public class BookAvailableEvent {

    private final Long bookId;
    private final String bookTitle;

    public BookAvailableEvent(Long bookId, String bookTitle) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
    }
}
