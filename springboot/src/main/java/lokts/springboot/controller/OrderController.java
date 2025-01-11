package lokts.springboot.controller;

import lokts.springboot.dto.OrderDTO;
import lokts.springboot.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;


    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderDTO orderDTO) {

        String order = orderService.createOrder(orderDTO);

        return ResponseEntity.status(HttpStatus.OK).body(order);

    }

}
