package tqa.demo.order.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import tqa.demo.order.dao.CommonDao;
import tqa.demo.order.dto.OrderDTO;
import tqa.demo.order.dto.ShippingAddressDTO;
import tqa.demo.order.entity.OrderEntity;
import tqa.demo.order.entity.Status;
import tqa.demo.order.exception.InvalidOrderException;
import tqa.demo.order.exception.OrderNotFoundException;
import tqa.demo.order.repository.OrderRepository;
import tqa.demo.order.util.Utils;

@Service
public class OrderServiceImpl implements OrderService {
	private final OrderRepository orderRepository;
	private final CommonDao commonDao;
	private final RestTemplate restTemplate;
	private final String localUrl;
	
	public OrderServiceImpl(OrderRepository orderRepository, CommonDao commonDao, RestTemplate restTemplate, @Value("${member.service.url}") String localUrl) {
		this.orderRepository = orderRepository;
		this.commonDao = commonDao;
		this.restTemplate = restTemplate;
		this.localUrl = localUrl;
	}
	
	
	@Override
	public OrderDTO placeOrder(OrderDTO placeOrder) throws Exception {
		// 1. 주문전 재고조회(외부 서비스 조회)
		String url = localUrl + "/product/stock/v1/retireveAvailable/{id}";
		List<Long> productIds = Utils.streamOf(placeOrder.getOrderedProducts()).map(e->e.getProductId()).collect(Collectors.toList());

		for(Long id : productIds) {
//			HttpHeaders headers = new HttpHeaders();
//			headers.setContentType(MediaType.APPLICATION_JSON);
//			HttpEntity<String> request = new HttpEntity<>("", headers);
//			String isAvailable = restTemplate.exchange(url, HttpMethod.GET, request, String.class, id);
			int productCnt = restTemplate.getForObject(url, Integer.class, id);
			if (productCnt < 0) {
				throw new InvalidOrderException("Out of Stock");
			}
		}

		// 2. 주문 상태 ORDERED로 변경, ORDERED_DATE, UPDATE_DATE 현재날짜로 변경
		placeOrder.setStatus(Status.ORDERED);
		placeOrder.setOrderedDate(Utils.getYYYMMDD());
		placeOrder.setUpdatedDate(Utils.getYYYMMDD());

		// 3. OrderDTO => Order(Entity) and 연관관계 master인 many쪽에서 관계setting
		OrderEntity order = Utils.toOrderEntity(placeOrder);
		order.getOrderedProducts().forEach(e->e.setOrderEntity(order));
		order.getShippingAddress().setOrderEntity(order);
		
		// 4.save =>(repository의 save)
		return Utils.toOrderDTO(orderRepository.save(order));
	}

	@Override
	public OrderDTO updateShippingAddress(ShippingAddressDTO addressDTO) throws Exception {
		// JPA를 이용한 update
		OrderEntity updateEntity = orderRepository.findById(addressDTO.getOrderId()).orElseThrow(()->new OrderNotFoundException("order not found"));
		updateEntity.getShippingAddress()
		    .updateAddress(
		    		addressDTO.getZipCode(),
		    		addressDTO.getRecipient()
		    		);
		return Utils.toOrderDTO(orderRepository.save(updateEntity));
	}

	@Override
	public int updateOrderStatus(OrderDTO modifiedOrder) throws Exception {
		// myBatis를 이용한 update
		OrderEntity updateEntity = orderRepository.findById(modifiedOrder.getId()).orElseThrow(()->new OrderNotFoundException("order not found"));
		updateEntity.updateStatus(modifiedOrder.getStatus());
		return commonDao.update("updateStatus", updateEntity);
	}

	@Override
	public List<OrderDTO> getOrders(String from, String to) throws Exception {
		// myBatis를 이용한 조회
		List<OrderEntity> orderEntityList = commonDao.selectList("findByOrderedDate", from, to);
		List<OrderDTO> orderDTOList = orderEntityList.stream().map(e->Utils.toOrderDTO(e)).collect(Collectors.toList());
		return orderDTOList;
	}
	
	@Override
	public int deleteOrder(Long id) throws Exception {
		// myBatis이용
		// many table 삭제(ordered_product, shipping_address)
		commonDao.delete("deleteShippingAddress", id);
		commonDao.delete("deleteOrderedProduct", id);
		return commonDao.delete("deleteOrder", id);
	}

}
