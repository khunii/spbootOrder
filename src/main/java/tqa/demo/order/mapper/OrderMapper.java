package tqa.demo.order.mapper;

import java.util.List;

import tqa.demo.order.entity.OrderEntity;

//@Mapper
/*
 * commonDao로 대체하여 사용하지 않음
 */
public interface OrderMapper {
    //처리 전 Order 를 조회하여 상태를 set한 객체를 전달하여 처리
	public int updateStatus(OrderEntity order);
	public OrderEntity findByOrderId(Long id);
	public List<OrderEntity> findByOrderedDate(String fromDate, String toDate);
	public int deleteOrder(Long id);
	public int deleteOrderedProduct(Long id);
	public int deleteShippingAddress(Long id);
	
}
