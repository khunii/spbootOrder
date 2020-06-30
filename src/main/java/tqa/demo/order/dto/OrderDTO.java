package tqa.demo.order.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tqa.demo.order.entity.Status;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString(exclude = {"orderedProducts", "shippingAddress"})
public class OrderDTO implements Serializable{

	private static final long serialVersionUID = -6991051757173059930L;
	private Long id;
    private String orderUserId;
    private String orderedDate;
    private String updatedDate;
    private Status status;
    private List<OrderedProductDTO> orderedProducts;
    private ShippingAddressDTO shippingAddress;
}
