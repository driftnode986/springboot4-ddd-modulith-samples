package com.example.shop.order.web;

// #region controller-imports
import com.example.shop.order.application.PlaceOrderService;
import com.example.shop.order.domain.OrderId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// #endregion

// #region controller
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final PlaceOrderService placeOrderService;

    public OrderController(
            PlaceOrderService placeOrderService) {
        this.placeOrderService = placeOrderService;
    }

    // #region post-mapping
    @PostMapping(version = "1")
    // #endregion
    public ResponseEntity<PlaceOrderResponse> place(
            @RequestBody PlaceOrderRequest request) {
        OrderId id =
                placeOrderService.place(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PlaceOrderResponse(id.value()));
    }
}
// #endregion
