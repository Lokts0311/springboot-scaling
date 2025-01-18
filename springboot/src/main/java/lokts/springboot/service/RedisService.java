package lokts.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    public RedisTemplate<String, Object> redisTemplate;

    public Long decrementStock(Long productId, int quantity) {

        Long updateStock = redisTemplate.opsForHash().increment("product:" + productId.toString(), "stock", -quantity);
        redisTemplate.expire("product:" + productId, 5, TimeUnit.MINUTES);
        return updateStock;
    }

    public Integer getStock(Long productId) {
        Object stock = redisTemplate.opsForHash().get("product:" + productId.toString(), "stock");
        return stock != null ? Integer.parseInt(stock.toString()) : null;
    }

    public void saveProductToCache(Long productId, int stock) {
        redisTemplate.opsForHash().put("product:" + productId, "stock", stock);
        redisTemplate.expire("product:" + productId, 5, TimeUnit.MINUTES);
    }

    public boolean isProductInCache(Long productId) {
        return redisTemplate.hasKey("product:" + productId);
    }

    public Set<String> getAllkeys(String pattern) {
        return redisTemplate.keys(pattern);
    }


}
