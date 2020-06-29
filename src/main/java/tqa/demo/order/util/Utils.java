package tqa.demo.order.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import tqa.demo.order.dto.OrderDTO;
import tqa.demo.order.dto.OrderedProductDTO;
import tqa.demo.order.dto.ShippingAddressDTO;
import tqa.demo.order.entity.OrderEntity;
import tqa.demo.order.entity.OrderedProductEntity;
import tqa.demo.order.entity.ShippingAddressEntity;

public class Utils {
	
	private static final Logger logger = LoggerFactory.getLogger(Utils.class);
	
	public static String getNow() {
		return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date())+"";
	}
	
	public static String getYYYMMDD() {
		return new SimpleDateFormat("yyyyMMdd").format(new Date())+"";
		
	}
	
	public static String toJsonString(Object obj) {
		ObjectMapper objMapper = new ObjectMapper();
		String ret = "";
		try {
			ret = objMapper.writeValueAsString(obj);
		} catch (Exception e) {
			logger.error("toJsonString error", e);
		}
		return ret;
	}
	
	public static <T> Stream<T> streamOf(List<T> list){
		if (list == null || list.isEmpty()) {
			return Stream.empty();
		}
		return list.stream();
	}
	
	public static OrderDTO toOrderDTO(OrderEntity entity) {
		return OrderDTO.builder()
				.id(entity.getId())
				.orderUserId(entity.getOrderUserId())
				.orderedDate(entity.getOrderedDate())
				.updatedDate(entity.getUpdatedDate())
				.status(entity.getStatus())
				.orderedProducts(streamOf(entity.getOrderedProducts()).map(e->toOrderedProductDTO(e)).collect(Collectors.toList()))
				.shippingAddress(entity.getShippingAddress() != null ? toShippingAddressDTO(entity.getShippingAddress()) : null)
				.build();
	}
	
	public static List<OrderedProductDTO> toOrderedProductListDTO(OrderEntity entity){
		return streamOf(entity.getOrderedProducts())
				.map(e->toOrderedProductDTO(e))
				.collect(Collectors.toList());
	}
	
	public static ShippingAddressDTO toShippingAddressDTO(OrderEntity entity) {
		return toShippingAddressDTO(entity.getShippingAddress());
	}
	
	public static OrderEntity toOrderEntity(OrderDTO dto){
		OrderEntity orderEntity = OrderEntity.builder()
				.orderUserId(dto.getOrderUserId())
				.orderedDate(dto.getOrderedDate())
				.updatedDate(dto.getUpdatedDate())
				.status(dto.getStatus())
				.orderedProducts(streamOf(dto.getOrderedProducts()).map(e->toOrderedProductEntity(e)).collect(Collectors.toList()))
				.shippingAddress(toShippingAddressEntity(dto.getShippingAddress()))
				.build();
		orderEntity.getOrderedProducts().forEach(e->e.setOrderEntity(orderEntity));
		orderEntity.getShippingAddress().setOrderEntity(orderEntity);

		return orderEntity;

	}

	public static OrderedProductDTO toOrderedProductDTO(OrderedProductEntity entity) {
		return OrderedProductDTO.builder()
				.id(entity.getId())
				.productId(entity.getProductId())
				.price(entity.getPrice())
				.qty(entity.getQty())
				.orderId(entity.getOrder() != null ?entity.getOrder().getId() : null)
				.build();
	}
	
	public static OrderedProductEntity toOrderedProductEntity(OrderedProductDTO dto) {
		return OrderedProductEntity.builder()
				.id(dto.getId())
				.productId(dto.getProductId())
				.price(dto.getPrice())
				.qty(dto.getQty())
				.build();
	}
	
	public static ShippingAddressDTO toShippingAddressDTO(ShippingAddressEntity entity) {
		return ShippingAddressDTO.builder()
				.id(entity.getId())
				.zipCode(entity.getZipCode())
				.recipient(entity.getRecipient())
				.orderId(entity.getOrder() != null ?entity.getOrder().getId() : null)
				.build();
	}
	
	public static ShippingAddressEntity toShippingAddressEntity(ShippingAddressDTO dto) {
		return ShippingAddressEntity.builder()
				.id(dto.getId())
				.zipCode(dto.getZipCode())
				.recipient(dto.getRecipient())
				.build();
	}

	//안쓸듯.
	public static OrderEntity toOrderEntityForJoin(OrderDTO dto){
		return OrderEntity.builder()
				.orderUserId(dto.getOrderUserId())
				.orderedDate(dto.getOrderedDate())
				.updatedDate(dto.getUpdatedDate())
				.status(dto.getStatus())
				.build();
	}

}
