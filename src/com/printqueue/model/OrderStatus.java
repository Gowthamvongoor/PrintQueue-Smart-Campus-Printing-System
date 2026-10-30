package com.printqueue.model;

/**
 * Represents the lifecycle stages of a print order.
 */
public enum OrderStatus {
    SUBMITTED,
    PENDING,
    IN_PROGRESS,
    READY_FOR_PICKUP,
    COLLECTED,
    CANCELLED
}
