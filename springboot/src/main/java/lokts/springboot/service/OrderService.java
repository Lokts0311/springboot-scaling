package lokts.springboot.service;

import lokts.springboot.dto.OrderDTO;
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
import java.util.concurrent.atomic.AtomicReference;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private Environment environment;

    @Transactional
    public String createOrder(OrderDTO orderDTO) {

        AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);

        Order order = new Order().builder()
                .customerName(orderDTO.getCustomerName())
                .orderItems(orderDTO.getOrderItems().stream().map(orderItemDTO ->{
                    Product product = productRepository.findByIdForUpdate(orderItemDTO.getProductId())
                            .orElseThrow(() -> new ProductNotExistsException("Product Id" + orderItemDTO.getProductId() + " is not exist."));

                    // Check stock availability
                    if (product.getStock() < orderItemDTO.getQuantity()) {
                        throw new ProductInsufficientStockException("You have request "+ orderItemDTO.getQuantity() + " for Product Id " + orderItemDTO.getProductId() + "! But there is only " + product.getStock() + " stocks left!");
                    }

                    product.setStock(product.getStock() - orderItemDTO.getQuantity());
                    productRepository.save(product);

                    OrderItem orderItem = new OrderItem().builder().product(product).quantity(orderItemDTO.getQuantity()).build();

                    BigDecimal itemTotalAmount = product.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
                    totalAmount.set(totalAmount.get().add(itemTotalAmount));

                    return orderItem;
                }).toList())
                .totalAmount(totalAmount.get())
                .build();

        order = orderRepository.save(order);

        return new String( "Order "+ order.getId() + " with total price = " + order.getTotalAmount() + " from Server " + environment.getProperty("local.server.port") + " !");

    }
}
