package com.example.shop.order.web;

import com.example.shop.order.application.PlaceOrderService;
import com.example.shop.order.domain.OrderId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HTTP の入り口。受け取った形を移し替えて、ユースケースを1つ呼ぶだけに留める。 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final PlaceOrderService placeOrderService;

    public OrderController(PlaceOrderService placeOrderService) {
        this.placeOrderService = placeOrderService;
    }

    @PostMapping(version = "1")
    public ResponseEntity<PlaceOrderResponse> place(@RequestBody PlaceOrderRequest request) {
        OrderId id = placeOrderService.place(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PlaceOrderResponse(id.value()));
    }
}
