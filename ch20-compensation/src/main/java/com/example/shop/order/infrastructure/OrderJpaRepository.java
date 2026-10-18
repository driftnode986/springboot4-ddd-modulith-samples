package com.example.shop.order.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Spring Data が実装を用意する。infrastructure の中だけで使う。 */
interface OrderJpaRepository extends JpaRepository<OrderEntity, String> {

    /** 集約はまとめて 1 回で読む。明細を後から取りにいかせない。 */
    @Query("select o from OrderEntity o left join fetch o.lines where o.id = :id")
    Optional<OrderEntity> findWithLinesById(String id);
}
