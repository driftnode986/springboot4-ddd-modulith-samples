package com.example.shop.before;

/**
 * 状態を enum で持った場合の形。状態ごとに必要な項目が違うため、
 * どの状態でも使わないフィールドを抱えることになる。
 */
public final class OrderWithEnum {

    public enum Status {
        ACCEPTED,
        RESERVED,
        PAID,
        CONFIRMED,
        CANCELLED
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

    public void markCancelled(String reason) {
        this.status = Status.CANCELLED;
        this.cancelReason = reason;
    }
}
