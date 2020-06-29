package tqa.demo.order.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddressDTO implements Serializable{
	private Long id;
	private String zipCode;
	private String recipient;
	private Long orderId;
	private OrderDTO order;

}
