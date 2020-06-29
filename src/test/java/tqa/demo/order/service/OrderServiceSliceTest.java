package tqa.demo.order.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;

import tqa.demo.order.dao.CommonDao;
import tqa.demo.order.dto.OrderDTO;
import tqa.demo.order.dto.OrderedProductDTO;
import tqa.demo.order.dto.ShippingAddressDTO;
import tqa.demo.order.entity.OrderEntity;
import tqa.demo.order.entity.OrderedProductEntity;
import tqa.demo.order.entity.ShippingAddressEntity;
import tqa.demo.order.entity.Status;
import tqa.demo.order.repository.OrderRepository;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceSliceTest {
	
   OrderService orderService;

   @Mock
   private OrderRepository orderRepository;
   
   private RestTemplate restTemplate = new RestTemplate();
   
   @Mock
   private CommonDao commonDao;
   
   OrderEntity result;
   List<Object> resultList = new ArrayList<>();
   
   @ClassRule
   public static WireMockClassRule wiremock = new WireMockClassRule(WireMockConfiguration.options().port(7676));
   
   @Before
   public void setUp() throws Exception{
	   //리턴 객체(placeOrder)
	   result = OrderEntity.builder()
			   .id(1L)
			   .orderUserId("jacob_test")
			   .orderedDate("20200701")
			   .updatedDate("20200701")
			   .status(Status.ORDERED)
			   .orderedProduct(OrderedProductEntity.builder().id(1L).productId(1L).price(2000L).qty(10).build())
			   .orderedProduct(OrderedProductEntity.builder().id(2L).productId(2L).price(1000L).qty(20).build())
			   .shippingAddress(ShippingAddressEntity.builder().id(1L).recipient("Jacob_test").zipCode("123-345").build())
			   .build();
	   
	   result.getOrderedProducts().forEach(e->e.setOrderEntity(result));
	   result.getShippingAddress().setOrderEntity(result);
	   
	   resultList.add(result);
	   

	   /*
	    * orderRepository
	    * save(Entity)
	    * findById(Long id)
	    */
	   Mockito.when(orderRepository.save(Mockito.any(OrderEntity.class))).thenReturn(result);
	   Mockito.when(orderRepository.findById(Mockito.any(Long.class))).thenReturn(Optional.of(result));
	   
	   /*
	    * CommonDao
	    * int update(String queryId, Object param)
	    * List<OrderDTO> selectList(String queryId, String from, String to)
	    * int delete(String queryId, Long id)
	    */
	   Mockito.when(commonDao.update(Mockito.anyString(), Mockito.anyObject())).thenReturn(1);
	   Mockito.when(commonDao.delete(Mockito.anyString(), Mockito.anyObject())).thenReturn(1);
	   Mockito.when(commonDao.selectList(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(resultList);
	   
	   orderService = new OrderServiceImpl(orderRepository, commonDao, restTemplate, "http://localhost:7676");
	   
	   stubFor(get(urlMatching("/product/stock/v1/retireveAvailable/([0-9])+")).willReturn(aResponse().withStatus(200)
			   .withHeader("Content-Type", "application/json;charset=UTF-8")
			   .withBody("1")));
   }
   
   @Test
   public void testPlaceOrder() throws Exception{
	   //given
	   List<OrderedProductDTO> productDTOList = new ArrayList<>();
	   productDTOList.add(OrderedProductDTO.builder().productId(1L).price(2000L).qty(10).build());
	   productDTOList.add(OrderedProductDTO.builder().productId(2L).price(1000L).qty(20).build());
	   
	   OrderDTO orderDTO = OrderDTO.builder()
			   .orderUserId("jacob_test")
			   .orderedDate("20200701")
			   .updatedDate("20200701")
			   .status(Status.PREPARED)
			   .orderedProducts(productDTOList)
			   .shippingAddress(ShippingAddressDTO.builder().recipient("Jacob_test").zipCode("123-345").build())
			   .build();
	   
	   //when
	   OrderDTO placedOrder = orderService.placeOrder(orderDTO);
	   
	   //then
	   assertThat(placedOrder.getOrderUserId()).isEqualTo(orderDTO.getOrderUserId());
	   assertThat(placedOrder.getOrderedProducts().size()).isEqualTo(orderDTO.getOrderedProducts().size());
	   assertThat(placedOrder.getShippingAddress().getRecipient()).isEqualTo(orderDTO.getShippingAddress().getRecipient());
   }
   
   @Test
   public void testUpdateShippingAddress() throws Exception{
	   //given
	   List<OrderedProductDTO> productDTOList = new ArrayList<>();
	   productDTOList.add(OrderedProductDTO.builder().productId(1L).price(2000L).qty(10).build());
	   productDTOList.add(OrderedProductDTO.builder().productId(2L).price(1000L).qty(20).build());
	   OrderDTO orderDTO = OrderDTO.builder()
			   .orderUserId("jacob_test")
			   .orderedDate("20200701")
			   .updatedDate("20200701")
			   .status(Status.PREPARED)
			   .orderedProducts(productDTOList)
			   .shippingAddress(ShippingAddressDTO.builder().recipient("Jacob_test").zipCode("123-345").build())
			   .build();
	   
	   OrderDTO placedOrder = orderService.placeOrder(orderDTO);
	   
	   ShippingAddressDTO addressDTO = new ShippingAddressDTO();
	   addressDTO.setOrderId(placedOrder.getId());	   
	   addressDTO.setZipCode("999-999");
	   addressDTO.setRecipient("testRecipient");
	   
	   //when
	   OrderDTO modified = orderService.updateShippingAddress(addressDTO);
	   
	   //then
	   assertThat(modified.getShippingAddress().getZipCode()).isEqualTo("999-999");
	   assertThat(modified.getShippingAddress().getRecipient()).isEqualTo("testRecipient");
   }
   
   @Test
   public void testUpdateOrderStatus_VerifyReturnValue() throws Exception{
	   //given
	   List<OrderedProductDTO> productDTOList = new ArrayList<>();
	   productDTOList.add(OrderedProductDTO.builder().productId(1L).price(2000L).qty(10).build());
	   productDTOList.add(OrderedProductDTO.builder().productId(2L).price(1000L).qty(20).build());
	   OrderDTO orderDTO = OrderDTO.builder()
			   .orderUserId("jacob_test")
			   .orderedDate("20200701")
			   .updatedDate("20200701")
			   .status(Status.PREPARED)
			   .orderedProducts(productDTOList)
			   .shippingAddress(ShippingAddressDTO.builder().recipient("Jacob_test").zipCode("123-345").build())
			   .build();
	   
	   OrderDTO placedOrder = orderService.placeOrder(orderDTO);
	   
	   placedOrder.setStatus(Status.DELIVERED);
	   
	   int count = orderService.updateOrderStatus(placedOrder);
	   
	   assertThat(count).isEqualTo(1);
   }
   
   @Test
   public void testUpdateOrderStatus_VerifyFromDB() throws Exception{
	   //given
	   List<OrderedProductDTO> productDTOList = new ArrayList<>();
	   productDTOList.add(OrderedProductDTO.builder().productId(1L).price(2000L).qty(10).build());
	   productDTOList.add(OrderedProductDTO.builder().productId(2L).price(1000L).qty(20).build());
	   OrderDTO orderDTO = OrderDTO.builder()
			   .orderUserId("jacob_test")
			   .orderedDate("20200701")
			   .updatedDate("20200701")
			   .status(Status.PREPARED)
			   .orderedProducts(productDTOList)
			   .shippingAddress(ShippingAddressDTO.builder().recipient("Jacob_test").zipCode("123-345").build())
			   .build();
	   
	   OrderDTO placedOrder = orderService.placeOrder(orderDTO);
	   
	   placedOrder.setStatus(Status.DELIVERED);
	   
	   //when
	   int count = orderService.updateOrderStatus(placedOrder);
	   OrderEntity queriedOrder = orderRepository.findById(placedOrder.getId()).get();
	   
	   //then
	   assertThat(count).isEqualTo(1);
	   assertThat(queriedOrder.getStatus()).isEqualTo(Status.DELIVERED);
   }
   
   @Test
   public void testDeleteOrder() throws Exception{
	   //given
	   //when
	   int count = orderService.deleteOrder(1L);
	   
	   //then
	   assertThat(count).isEqualTo(1);
   }
   
   @Test
   public void testGetOrders() throws Exception{
	   //given
	   
	   //when
	   List<OrderDTO> orders = orderService.getOrders("20200601", "20200630");
	   
	   //then
	   assertThat(orders.size()).isGreaterThan(0);
   }
}
