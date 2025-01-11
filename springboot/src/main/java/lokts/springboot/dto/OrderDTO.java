package lokts.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {


    private Long id;
    private String customerName;

    // private String status;

    private List<OrderItemDTO> orderItems;

    @Setter
    @Getter
    public static class OrderItemDTO {
        private Long productId;
        private Integer quantity;
    }

    private BigDecimal totalAmount;


}
