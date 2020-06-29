package tqa.demo.order.service;

import java.util.List;

import tqa.demo.order.dto.OrderDTO;
import tqa.demo.order.dto.ShippingAddressDTO;

public interface OrderService {
	
	public OrderDTO placeOrder(OrderDTO placeOrder) throws Exception; //insert
	public OrderDTO updateShippingAddress(ShippingAddressDTO addressDTO) throws Exception; //update
	public int updateOrderStatus(OrderDTO modifiedOrder) throws Exception; //update MyBatis
	public List<OrderDTO> getOrders(String from, String to) throws Exception; //select
	public int deleteOrder(Long id) throws Exception; //delete MyBatis
}
