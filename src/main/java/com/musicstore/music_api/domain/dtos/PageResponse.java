package com.musicstore.music_api.domain.dtos;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages) {
}

