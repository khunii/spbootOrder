package tqa.demo.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import tqa.demo.order.dao.CommonDao;
import tqa.demo.order.dto.OrderDTO;
import tqa.demo.order.dto.OrderedProductDTO;
import tqa.demo.order.dto.ShippingAddressDTO;
import tqa.demo.order.entity.OrderEntity;
import tqa.demo.order.entity.OrderedProductEntity;
import tqa.demo.order.entity.ShippingAddressEntity;
import tqa.demo.order.entity.Status;
import tqa.demo.order.util.Utils;
//import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureMybatis
@Import(CommonDao.class)
public class OrderRepositoryTest {
   @Autowired
   private OrderRepository repo;
   
   @Autowired
   CommonDao commonDao;
//   private OrderMapper orderMapper;
   
   OrderEntity orderEntity;
   
   @Before
   public void setUp() {
	   orderEntity = OrderEntity.builder()
			   .orderUserId("jacob_test")
			   .orderedDate("20200701")
			   .updatedDate("20200701")
			   .status(Status.ORDERED)
			   .orderedProduct(OrderedProductEntity.builder().productId(1L).price(2000L).qty(10).build())
			   .orderedProduct(OrderedProductEntity.builder().productId(2L).price(1000L).qty(20).build())
			   .shippingAddress(ShippingAddressEntity.builder().recipient("Jacob_test").zipCode("123-345").build())
			   .build();
	   
	   orderEntity.getOrderedProducts().forEach(e->e.setOrderEntity(orderEntity));
	   orderEntity.getShippingAddress().setOrderEntity(orderEntity);
	   repo.save(orderEntity);
   }
   
   @Test
   public void testGetOne() {
	   OrderEntity actual = repo.getOne(orderEntity.getId());
	   assertThat(actual).isEqualTo(orderEntity);
   }
   
   @Test
   public void testSave() {
	   OrderEntity actual = repo.getOne(orderEntity.getId());
	   OrderDTO order = Utils.toOrderDTO(actual);
	   List<OrderedProductDTO> productList = Utils.toOrderedProductListDTO(actual);
	   ShippingAddressDTO address = Utils.toShippingAddressDTO(actual); 
	   
	   assertThat(productList.get(0).getProductId())
	   .isEqualTo(orderEntity.getOrderedProducts().get(0).getProductId());
	   
	   assertThat(address.getZipCode()).isEqualTo(orderEntity.getShippingAddress().getZipCode());
   }
   
   @Test
   public void testUpdateStatus() {
	   //given
//	   OrderEntity expected = orderMapper.findByOrderId(orderEntity.getId());
	   OrderEntity expected = commonDao.selectOne("findByOrderId", orderEntity.getId());
	   expected.updateStatus(Status.SHIPPED);
	   //when
//	   orderMapper.updateStatus(expected);
	   commonDao.update("updateStatus", expected);
//	   OrderEntity actual = orderMapper.findByOrderId(expected.getId());
	   OrderEntity actual = commonDao.selectOne("findByOrderId", expected.getId());
	   
	   //then
	   assertThat(actual.getStatus()).isEqualTo(Status.SHIPPED);
	   assertThat(actual.getUpdatedDate()).isEqualTo(Utils.getYYYMMDD());
	   
   }
   
   @Test
   public void testSelectList() {
	   OrderEntity orderEntity2 = OrderEntity.builder()
			   .orderUserId("jacob_test")
			   .orderedDate("20200701")
			   .updatedDate("20200701")
			   .status(Status.ORDERED)
			   .orderedProduct(OrderedProductEntity.builder().productId(3L).price(3000L).qty(10).build())
			   .orderedProduct(OrderedProductEntity.builder().productId(4L).price(4000L).qty(20).build())
			   .shippingAddress(ShippingAddressEntity.builder().recipient("Jacob_test").zipCode("123-345").build())
			   .build();
	   
	   orderEntity2.getOrderedProducts().forEach(e->e.setOrderEntity(orderEntity2));
	   orderEntity2.getShippingAddress().setOrderEntity(orderEntity2);
	   repo.save(orderEntity2);
	   
//	   List<OrderEntity> orderList = orderMapper.findByOrderedDate("20200701", "20200701");
	   List<OrderEntity> orderList = commonDao.selectList("findByOrderedDate", "20200701", "20200701");
	   System.out.println(Utils.toJsonString(orderList));
	   assertThat(orderList.size()).isEqualTo(2);
   }
   
   

}
