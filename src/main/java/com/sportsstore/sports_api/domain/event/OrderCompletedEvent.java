package com.sportsstore.sports_api.domain.event;

public record OrderCompletedEvent(Long orderId, Long userId) {
}
