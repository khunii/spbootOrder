package tqa.demo.order.dto;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderedProductDTO implements Serializable{
    private Long id;
    private Long productId;
    private Long price;
    private Integer qty;
    private Long orderId;
    private OrderDTO order;
}
