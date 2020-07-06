package tqa.demo.order.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;
import tqa.demo.order.util.Utils;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"orderedProducts", "shippingAddress"})
@Entity
@Table(name="orders")
//@Alias("orders")
public class OrderEntity implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2000735262983906599L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "order_user_id")
	private String orderUserId;
	
	@Column(name = "ordered_date")
	private String orderedDate;
	
	@Column(name = "updated_date")
	private String updatedDate;
	
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@JsonIgnore
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	@Singular
	private List<OrderedProductEntity> orderedProducts = new ArrayList<>();
		
	@JsonIgnore
	@OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
	private ShippingAddressEntity shippingAddress;
	
	public void updateStatus(Status status) {
		this.status = status;
		this.updatedDate = Utils.getYYYMMDD();
	}
	
	public void changeShippingAddress(ShippingAddressEntity shippingAddress) {
		this.shippingAddress = shippingAddress;
	}
}
