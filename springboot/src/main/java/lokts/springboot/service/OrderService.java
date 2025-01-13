package lokts.springboot.service;

import lokts.springboot.dto.OrderDTO;
import lokts.springboot.dto.OrderResponse;
import lokts.springboot.entity.Order;
import lokts.springboot.entity.OrderItem;
import lokts.springboot.entity.Product;
import lokts.springboot.exception.ProductNotExistsException;
import lokts.springboot.exception.ProductInsufficientStockException;
import lokts.springboot.repository.OrderRepository;
import lokts.springboot.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private Environment environment;

    @Transactional
    public OrderResponse createOrder(OrderDTO orderDTO) {

        AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);
        List<OrderResponse.OrderedItem> orderedItems = new ArrayList<>();

        Map<Long, Integer> productQuantities = orderDTO.getOrderItems().stream().collect(Collectors.toMap(
                OrderDTO.OrderItemDTO::getProductId,
                OrderDTO.OrderItemDTO::getQuantity
        ));

        List<Long> orderItemsId = new ArrayList<>(productQuantities.keySet());

        // Lock all required row, prevent asyn update by different order
        List<Product> OrderedProducts = productRepository.findByProductIdsForUpdate(orderItemsId);


        Order order = new Order().builder()
                .customerName(orderDTO.getCustomerName())
                .orderItems(OrderedProducts.stream().map(product -> {

                    Integer requiredQuantity = productQuantities.get(product.getId());

                    // Check stock availability
                    if (product.getStock() < requiredQuantity) {
                        throw new ProductInsufficientStockException("You have request "+ requiredQuantity + " for Product Id " + product.getId() + "! But there is only " + product.getStock() + " stocks left!");
                    }

                    product.setStock(product.getStock() - requiredQuantity);
                    product = productRepository.save(product);

                    // Create orderItem
                    OrderItem orderItem = new OrderItem().builder().product(product).quantity(requiredQuantity).build();


                    BigDecimal itemTotalAmount = product.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
                    totalAmount.set(totalAmount.get().add(itemTotalAmount));


                    OrderResponse.OrderedItem orderedItem = new OrderResponse.OrderedItem();
                    orderedItem.setStockLeft(product.getStock());
                    orderedItem.setProductId(product.getId());

                    orderedItems.add(orderedItem);

                    return orderItem;
                }).toList())
                .totalAmount(totalAmount.get())
                .build();

        order = orderRepository.save(order);

        OrderResponse orderResponse = OrderResponse.builder()
                .id(order.getId())
                .orderedItems(orderedItems)
                .totalAmount(order.getTotalAmount())
                .port(environment.getProperty("local.server.port"))
                .build();

        return orderResponse;

    }
}
