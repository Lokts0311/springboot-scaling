package lokts.springboot.service;

import lokts.springboot.dto.OrderDTO;
import lokts.springboot.dto.OrderResponse;
import lokts.springboot.entity.Order;
import lokts.springboot.entity.OrderItem;
import lokts.springboot.repository.OrderRepository;
import lokts.springboot.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public OrderResponse createOrder(OrderDTO orderDTO) {

        AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);

        Map<Long, Integer> productQuantities = orderDTO.getOrderItems().stream().collect(Collectors.toMap(
                OrderDTO.OrderItemDTO::getProductId,
                OrderDTO.OrderItemDTO::getQuantity,
                Integer::sum
        ));

        System.out.println("Start entering redis function");
        Map<Long, Integer> updatedPQ = productService.decrementStock(productQuantities);

        List<OrderItem> orderItems = new ArrayList<>();
        productQuantities.forEach((productId,quantity) -> {
            orderItems.add(OrderItem.builder()
                            .productId(productId)
                            .quantity(quantity).build());
        });


        Order order = Order.builder()
                .customerName(orderDTO.getCustomerName())
                .orderItems(orderItems)
                .build();

        orderRepository.save(order);


        List<OrderResponse.BoughtItems> boughtItems = new ArrayList<>();
        updatedPQ.forEach((k,v) -> {
            OrderResponse.BoughtItems boughtItem = new OrderResponse.BoughtItems();
            boughtItem.setProductId(k);
            boughtItem.setStockLeft(v);
            boughtItems.add(boughtItem);
        });


        OrderResponse orderResponse = OrderResponse.builder().id(order.getId()).boughtItems(boughtItems).build();

        return orderResponse;


    }

}
