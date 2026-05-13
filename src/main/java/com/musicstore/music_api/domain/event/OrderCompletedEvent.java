package com.musicstore.music_api.domain.event;

public record OrderCompletedEvent(Long orderId, Long userId) {
}

