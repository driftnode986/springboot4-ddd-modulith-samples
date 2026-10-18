package com.example.shop.shipping.application;

import com.example.shop.shipping.domain.Shipment;
import com.example.shop.shipping.spi.ShipmentArrangement;
import com.example.shop.shipping.spi.ShipmentId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class ArrangeShipmentService implements ShipmentArrangement {

    /** 保存は第17章で扱う。ここでは配送先の検査が働くことだけを示す。 */
    private final List<Shipment> arranged = new ArrayList<>();

    @Override
    public ShipmentId arrange(String orderId, String address) {
        Shipment shipment =
                new Shipment(new ShipmentId(UUID.randomUUID().toString()), orderId, address);
        arranged.add(shipment);
        return shipment.id();
    }
}
