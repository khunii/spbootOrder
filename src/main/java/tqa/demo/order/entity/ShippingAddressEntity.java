package tqa.demo.order.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"order"})
@Entity
@Table(name="shipping_address")
public class ShippingAddressEntity implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7169651639058384496L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="zip_code")
	private String zipCode;
	
	private String recipient;
	
	@OneToOne
	@JoinColumn(name="order_id")
	private OrderEntity order;
	
	public void updateAddress(String zipCode, String recipient) {
		this.zipCode = zipCode;
		this.recipient = recipient;
	}
	
	public void setOrderEntity(OrderEntity order) {
		this.order = order;
//		this.order.changeShippingAddress(order.getShippingAddress());
	}
	
}