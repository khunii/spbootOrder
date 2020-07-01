package tqa.demo.order.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tqa.demo.order.dto.OrderDTO;
import tqa.demo.order.dto.ShippingAddressDTO;
import tqa.demo.order.entity.Status;
import tqa.demo.order.service.OrderService;

@RestController
@RequestMapping("/demo")
public class OrderController {

	@Autowired
	OrderService orderService;
	
	@PostMapping("/order/v1")
	public OrderDTO placeOrder(@RequestBody OrderDTO orderDTO) throws Exception{
		return orderService.placeOrder(orderDTO); 
	}
	
	@PutMapping("/order/address/v1/{id}")
	public OrderDTO updateShippingAddress(@PathVariable Long id, @RequestBody ShippingAddressDTO addressDTO) throws Exception {
		addressDTO.setOrderId(id);
		return orderService.updateShippingAddress(addressDTO);
	}
	
	@PutMapping("/order/status/v1/{id}")
	public int updateStatus(@PathVariable Long id, @RequestBody Status status) throws Exception {
		OrderDTO dto = new OrderDTO();
		dto.setId(id);
		dto.setStatus(status);
		return orderService.updateOrderStatus(dto); 
	}
	
//	@GetMapping("/order/v1")
//	public List<OrderDTO> selectOrders(@RequestParam Map<String,String> params) throws Exception{
//		return orderService.getOrders(params.get("fromDate"), params.get("toDate"));
//	}
	
	@GetMapping("/order/v1")
	public List<OrderDTO> selectOrders(@RequestParam("fromDate") String fromDate, @RequestParam("toDate") String toDate) throws Exception{
//		return orderService.getOrders(params.get("fromDate"), params.get("toDate"));
		return orderService.getOrders(fromDate, toDate);
	}
	
	@DeleteMapping("/order/v1/{id}")
	public int deleteOrder(@PathVariable Long id) throws Exception{
		return orderService.deleteOrder(id);
	}
}
