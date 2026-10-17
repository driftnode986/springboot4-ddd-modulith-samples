package com.example.shop.before;

// #region class
public final class OrderWithEnum {

    public enum Status {
        ACCEPTED, RESERVED, PAID, CONFIRMED, CANCELLED
    }

    private Status status;
    private String reservationId;
    private String paymentId;
    private String cancelReason;

    public OrderWithEnum() {
        this.status = Status.ACCEPTED;
    }

    public Status status() {
        return status;
    }

    public String paymentId() {
        return paymentId;
    }

    public void markPaid(String paymentId) {
        this.status = Status.PAID;
        this.paymentId = paymentId;
    }
}
// #endregion
