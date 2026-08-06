package com.example.shop.order.infrastructure;

import com.example.shop.order.domain.Order;
import com.example.shop.order.domain.OrderId;
import com.example.shop.order.domain.OrderRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** ドメインが決めたインターフェースを、JPA で満たす。 */
@Repository
class JpaOrderRepository implements OrderRepository {

    private final OrderJpaRepository jpa;

    JpaOrderRepository(OrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Order order) {
        jpa.save(OrderMapper.toEntity(order));
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpa.findWithLinesById(id.value()).map(OrderMapper::toDomain);
    }
}
