package lokts.springboot.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class OrderResponse {

    private Long id;

    private BigDecimal totalAmount;

    private List<OrderedItem> orderedItems;

    @Setter
    @Getter
    public static class OrderedItem {
        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getStockLeft() {
            return stockLeft;
        }

        public void setStockLeft(Integer stockLeft) {
            this.stockLeft = stockLeft;
        }

        private Long productId;
        private Integer stockLeft;


    }

    private String port;
}
