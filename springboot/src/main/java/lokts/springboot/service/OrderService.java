package lokts.springboot.service;

import jakarta.persistence.*;
import lokts.springboot.dto.OrderDTO;
import lokts.springboot.dto.OrderResponse;
import lokts.springboot.entity.Order;
import lokts.springboot.entity.OrderItem;
import lokts.springboot.entity.Product;
import lokts.springboot.exception.ProductInsufficientStockException;
import lokts.springboot.repository.OrderRepository;
import lokts.springboot.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.util.*;
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

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderResponse.BoughtItems> boughtItems = new ArrayList<>();

        // Use TreeMap to ensure the order of productId
        Map<Long, Integer> productQuantities = orderDTO.getOrderItems().stream().collect(Collectors.toMap(
                OrderDTO.OrderItemDTO::getProductId,
                OrderDTO.OrderItemDTO::getQuantity,
                Integer::sum,
                TreeMap::new
        ));

        List<Long> orderItemsId = new ArrayList<>(productQuantities.keySet());
        List<Integer> orderItemsQuantity = new ArrayList<>(productQuantities.values());

        // Lock all required row, prevent asyn update by other orders, keep itemId in ascending order
        List<Product> products = productRepository.findByProductIdsForUpdate(orderItemsId);

        for(Product product : products) {
            if (product.getStock() < productQuantities.get(product.getId())) {
                throw new ProductInsufficientStockException("You have request " + productQuantities.get(product.getId()) + " for Product Id " + product.getId() + "! But there is only " + product.getStock() + " stocks left!");
            }
        }

        // Update Products Stock
        List<Product> updatedProducts = updateProductStock(productQuantities);

        List<OrderItem> orderItems = new ArrayList<>();
        for(int i = 0; i < updatedProducts.size(); i++) {
            orderItems.add(OrderItem
                    .builder()
                    .product(updatedProducts.get(i))
                    .quantity(orderItemsQuantity.get(i))
                    .build());

            OrderResponse.BoughtItems boughtItem = new OrderResponse.BoughtItems();
            boughtItem.setProductId(updatedProducts.get(i).getId());
            boughtItem.setStockLeft(updatedProducts.get(i).getStock());
            boughtItems.add(boughtItem);

            totalAmount = totalAmount.add(updatedProducts.get(i).getPrice().multiply(new BigDecimal(orderItemsQuantity.get(i))));
        }

        Order order = Order.builder()
                .customerName(orderDTO.getCustomerName())
                .orderItems(orderItems)
                .totalAmount(totalAmount)
                .build();

        // Create Order in Database
        order = orderRepository.save(order);

        OrderResponse orderResponse = OrderResponse.builder()
                .id(order.getId())
                .boughtItems(boughtItems)
                .totalAmount(order.getTotalAmount())
                .port(environment.getProperty("local.server.port"))
                .build();

        return orderResponse;

    }

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public List<Product> updateProductStock(Map<Long, Integer> productQuantities) {

        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            entityManager.createQuery("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :productId")
                    .setParameter("quantity", entry.getValue())
                    .setParameter("productId", entry.getKey())
                    .executeUpdate();
        }

        // Force Write Change to Database
        entityManager.flush();
        entityManager.clear();

        String selectQuery = "SELECT p FROM Product p WHERE p.id IN :productIds";

        TypedQuery<Product> query = entityManager.createQuery(selectQuery,Product.class);
        query.setParameter("productIds", productQuantities.keySet());

        List<Product> updatedProducts = query.getResultList();

        return updatedProducts;
    }


}
