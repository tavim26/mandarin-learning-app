package com.chineselearning.flashcardservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateFlashcardRequest(
        @NotNull(message = "Set ID must not be null")
        Long setId,

        @NotBlank(message = "Front text must not be blank")
        @Size(max = 2000, message = "Front text must not exceed 2000 characters")
        String frontText,

        @NotBlank(message = "Back text must not be blank")
        @Size(max = 2000, message = "Back text must not exceed 2000 characters")
        String backText
) {
}