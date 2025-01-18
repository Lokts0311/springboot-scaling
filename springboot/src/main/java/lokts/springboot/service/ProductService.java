package lokts.springboot.service;

import lokts.springboot.dto.ProductDTO;
import lokts.springboot.entity.Product;
import lokts.springboot.exception.ProductAlreadyExistsException;
import lokts.springboot.exception.ProductInsufficientStockException;
import lokts.springboot.exception.ProductNotExistsException;
import lokts.springboot.mapper.ProductMapper;
import lokts.springboot.repository.ProductRepository;
import org.antlr.v4.runtime.misc.IntegerStack;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public ProductDTO createProduct(ProductDTO productDTO) {

        Optional<Product> existingProductOpt = productRepository.findByName(productDTO.getName());

        if (existingProductOpt.isPresent()) {
            throw new ProductAlreadyExistsException("Product " + productDTO.getName() + " already exists.");
        }

        Product product = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .stock(productDTO.getStock())
                .build();

        product = productRepository.save(product);

        return ProductMapper.INSTANCE.productToProductDTO(product);
    }

    public ProductDTO updateProduct(ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(productDTO.getId())
                .orElseThrow(()-> new ProductNotExistsException("Product ID " + productDTO.getId() + " is not exist."));

        if (!existingProduct.getName().equals(productDTO.getName()) &&
        productRepository.findByName(productDTO.getName()).isPresent()) {
            throw new ProductAlreadyExistsException("Product Name must be unique.");
        }

        existingProduct.setName(productDTO.getName());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStock(productDTO.getStock());
        Product updatedProduct = productRepository.save(existingProduct);

        return ProductMapper.INSTANCE.productToProductDTO(updatedProduct);
    }

    public ProductDTO getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new ProductNotExistsException("Product ID " + id + " is not exist."));

        return ProductMapper.INSTANCE.productToProductDTO(product);

    }

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedissonClient redissonClient;

    private static final String LOCK_KEY_PREFIX = "lock:product:";

    public Map<Long, Integer> decrementStock(Map<Long, Integer> productQuantities) {

        List<Long> productIds = new ArrayList<>(productQuantities.keySet());
        Collections.sort(productIds);
        List<RLock> locks = new ArrayList<>();

        Map<Long, Integer> updatedMap = new HashMap<>();

        for (Long productId : productIds) {
            locks.add(redissonClient.getLock(LOCK_KEY_PREFIX + productId));
        }

        RedissonMultiLock multiLock = new RedissonMultiLock(locks.toArray(locks.toArray(new RLock[0])));
        try {
            if (multiLock.tryLock(5, 10, TimeUnit.SECONDS)) {

                for (Long productId : productIds) {
                    int quantity = productQuantities.get(productId);

                    Product product;

                    if (!redisService.isProductInCache(productId)) {
                        product = productRepository.findById(productId)
                                .orElseThrow(() -> new ProductNotExistsException("Product Id " + productId + "not exist"));

                        redisService.saveProductToCache(productId, product.getStock());
                    }

                    Integer currentStock = redisService.getStock(productId);

                    if (currentStock < quantity) {
                        throw new ProductInsufficientStockException("Not enough stock!");
                    }
                    Long updatedStock = redisService.decrementStock(productId, quantity);

                    updatedMap.put(productId, updatedStock.intValue());
                }
            } else {
                throw new RuntimeException("Unable to acquire multi-lock for products!");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to update stocks: " + e.getMessage() ,e);
        } finally {
            if (multiLock.isHeldByCurrentThread()) {
                multiLock.unlock();
            }
        }
        return updatedMap;
    }


    public void updateProductStock(Long productId, Integer stock) {

        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ProductNotExistsException("Product Id " + productId + "not exist"));

        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + productId);

        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                // Update stock of product in Redis first
                redisService.saveProductToCache(productId, stock);

                // For High Consistency, Write-Through approach, Update Redis then Database
                // For Eventually Consistency, Write-Behind approach, Update Redis then wait for sync
                product.setStock(stock);
                productRepository.save(product);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    public void syncStockToDatabase() {

        System.out.println("Start Sync Redis Data to Database");
        Set<String> keys = redisService.getAllkeys("product:*");

        for (String key : keys) {
            Long productId = Long.valueOf(key.split(":")[1]);
            Integer stock = redisService.getStock(productId);

            if (stock != null) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found!"));

                product.setStock(stock);
                productRepository.save(product);
            }
        }
    }
}
