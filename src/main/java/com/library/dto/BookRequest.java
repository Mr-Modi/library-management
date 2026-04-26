package com.library.dto;

import com.library.entity.Book;
import com.library.enums.AvailabilityStatus;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author must not exceed 255 characters")
    private String author;

    @NotBlank(message = "ISBN is required")
    @Pattern(
        regexp = "^(?:\\d{9}[\\dX]|\\d{13})$",
        message = "ISBN must be a valid 10 or 13 digit number"
    )
    private String isbn;

    @NotNull(message = "Published year is required")
    @Min(value = 1000, message = "Published year must be at least 1000")
    @Max(value = 9999, message = "Published year must be a 4-digit year")
    private Integer publishedYear;

    private AvailabilityStatus availabilityStatus;
}
