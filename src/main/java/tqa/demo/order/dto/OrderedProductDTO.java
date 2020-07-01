package tqa.demo.order.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderedProductDTO implements Serializable{

	private static final long serialVersionUID = 7631421380083352189L;
	private Long id;
    private Long productId;
    private Long price;
    private Integer qty;
    private Long orderId;
//    private OrderDTO order;
}
