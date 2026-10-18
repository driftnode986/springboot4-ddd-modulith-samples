package com.example.shop.order.infrastructure;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 表の形をそのまま写した入れ物。ドメインの Order とは別物で、
 * 変換は OrderMapper だけが行う。
 */
@Entity
@Table(name = "orders")
class OrderEntity {

    @Id
    private String id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String state;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "reservation_id")
    private String reservationId;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Version
    private Long version;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    private List<OrderLineEntity> lines = new ArrayList<>();

    protected OrderEntity() {
        // JPA が使う
    }

    OrderEntity(String id) {
        this.id = id;
    }

    String getId() {
        return id;
    }

    String getCustomerId() {
        return customerId;
    }

    void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    String getState() {
        return state;
    }

    void setState(String state) {
        this.state = state;
    }

    Instant getAcceptedAt() {
        return acceptedAt;
    }

    void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    String getReservationId() {
        return reservationId;
    }

    void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    String getPaymentId() {
        return paymentId;
    }

    void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    Instant getConfirmedAt() {
        return confirmedAt;
    }

    void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    Instant getCancelledAt() {
        return cancelledAt;
    }

    void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    String getCancelReason() {
        return cancelReason;
    }

    void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    Long getVersion() {
        return version;
    }

    List<OrderLineEntity> getLines() {
        return lines;
    }
}
