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

    private List<BoughtItems> boughtItems;

    @Setter
    @Getter
    public static class BoughtItems {
        private Long productId;
        private Integer stockLeft;
    }
    private BigDecimal totalAmount;
    private String port;
}
