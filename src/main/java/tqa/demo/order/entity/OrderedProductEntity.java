package tqa.demo.order.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude= {"order"})
@Entity
@Table(name="ordered_product")
public class OrderedProductEntity implements Serializable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="product_id")
	private Long productId;
	
	private Long price;
	
	private Integer qty;
	
	@ManyToOne
	@JoinColumn(name="order_id") //ordered_product table에 order_id가 생기면서 Order와의 relation에 관여
	private OrderEntity order;
	
	public void setOrderEntity(OrderEntity order) {
//		if (this.order != null) {
//			this.order.getOrderedProducts().remove(this);
//		}
		this.order = order;
//		this.order.getOrderedProducts().add(this);
	}

}
