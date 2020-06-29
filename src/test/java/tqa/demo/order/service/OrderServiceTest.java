package tqa.demo.order.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.junit4.SpringRunner;

import tqa.demo.order.dto.OrderDTO;
import tqa.demo.order.dto.OrderedProductDTO;
import tqa.demo.order.dto.ShippingAddressDTO;
import tqa.demo.order.entity.OrderEntity;
import tqa.demo.order.entity.Status;
import tqa.demo.order.repository.OrderRepository;

/*
 * 아래는 고정값으로 템플릿화 하는데, 그 중 @SpringBootTest의 properties는 개발자가 필요시 고쳐야 하므로 주석처리한다.
 * insert같은 곳에서 publish하는 것은 해당 객체 @mock처리 필수
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = {"member.service.url=http://localhost:${wiremock.server.port}"}
		)
@AutoConfigureWireMock(port=0)

public class OrderServiceTest {
	
   //테스트 대상 클래스를 주입
   @Autowired
   private OrderService orderService;

   //데이터 생성을 위하여 주입했으나, 자동생성시에는 SQL문을 돌리도록 할 지 repository를 이용할 지는 결정 후 적용
   @Autowired
   private OrderRepository repo;
   
   @Before
   public void setUp() throws Exception{
		//아래(wiremock을 통한 mocking)는 고민, 외부 서비스를 mocking하는 것인데 이건 처음버전은 템플릿(주석처리)을 제공하고 개발자가 보완하도록 처리
	   stubFor(get(urlMatching("/product/stock/v1/retireveAvailable/([0-9])+")).willReturn(aResponse().withStatus(200)
			   .withHeader("Content-Type", "application/json;charset=UTF-8")
			   .withBody("1")));
   }
   
   @Test
   public void testPlaceOrder() throws Exception{
		//given(입력값)
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
	   
	   //when(ServiceLayer method실행)
	   OrderDTO placedOrder = orderService.placeOrder(orderDTO);
	   
	   //then
	   assertThat(placedOrder.getOrderUserId()).isEqualTo(orderDTO.getOrderUserId());
	   assertThat(placedOrder.getOrderedProducts().size()).isEqualTo(orderDTO.getOrderedProducts().size());
	   assertThat(placedOrder.getShippingAddress().getRecipient()).isEqualTo(orderDTO.getShippingAddress().getRecipient());
   }
   
   @Test
   public void testUpdateShippingAddress() throws Exception{
		//fixture(주문 데이터 만들기)
		//data 만들기(repository로 db에 직접 쌓아도 무방)
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
	   
	   //given(입력값)
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
		//fixture(주문 데이터 만들기)
		//data 만들기(repository로 db에 직접 쌓아도 무방)
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
	   
	   //given(입력값)
	   OrderDTO modified = new OrderDTO();
	   modified.setId(placedOrder.getId());
	   modified.setStatus(Status.DELIVERED);
	   
	   //when
	   int count = orderService.updateOrderStatus(modified);
	   
	   //then
	   assertThat(count).isEqualTo(1);
   }
   
   /*
    * 이렇게 실행결과와 db에서 다시 조회해서 하는 형태는 사실상 DevOnTester에서는 불가능
    */
   @Test
   public void testUpdateOrderStatus_VerifyFromDB() throws Exception{
		//fixture(주문 데이터 만들기)
		//data 만들기(repository로 db에 직접 쌓아도 무방)
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
	   
	   //given(입력값)
	   OrderDTO modified = new OrderDTO();
	   modified.setId(placedOrder.getId());
	   modified.setStatus(Status.DELIVERED);
	   
	   //when
	   int count = orderService.updateOrderStatus(modified);
	   OrderEntity queriedOrder = repo.findById(modified.getId()).get();
	   
	   //then
	   assertThat(count).isEqualTo(1);
	   assertThat(queriedOrder.getStatus()).isEqualTo(Status.DELIVERED);
   }
   
   @Test
   public void testDeleteOrder() throws Exception{
		//fixture(주문 데이터 만들기)
		//data 만들기(repository로 db에 직접 쌓아도 무방)
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
	   
	   //given
	   Long id = placedOrder.getId();
	   
	   //when
	   int count = orderService.deleteOrder(id);
	   
	   //then
	   assertThat(count).isEqualTo(1);
   }
   
   @Test
   public void testGetOrders() throws Exception{
		//fixture(주문 데이터 만들기)
		//data 만들기(repository로 db에 직접 쌓아도 무방)
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
	   
	   List<OrderedProductDTO> productDTOList2 = new ArrayList<>();
	   productDTOList2.add(OrderedProductDTO.builder().productId(3L).price(4000L).qty(30).build());
	   productDTOList2.add(OrderedProductDTO.builder().productId(4L).price(3000L).qty(40).build());
	   
	   OrderDTO orderDTO2 = OrderDTO.builder()
			   .orderUserId("jacob_test")
			   .orderedDate("20200731")
			   .updatedDate("20200731")
			   .status(Status.PREPARED)
			   .orderedProducts(productDTOList2)
			   .shippingAddress(ShippingAddressDTO.builder().recipient("Jacob_test").zipCode("123-345").build())
			   .build();
	   
	   OrderDTO placedOrder2 = orderService.placeOrder(orderDTO2);
	   
	   //given
	   String fromDate = "20200601";
	   String toDate = "20200630";
	   
	   //when
	   List<OrderDTO> orders = orderService.getOrders(fromDate, toDate);
	   
	   //then
	   assertThat(orders.size()).isGreaterThan(2);
   }
/*
 * TODO 테스트 코드 노트
 * 
 * ##1##
 * 실제 ServiceImpl에서도 사용하는 member.service.url을 테스트용으로 wiremock등을 띄어 stubbing할때 포트 등 값을 바꾸는 방법
 * 1. 동적변경
 * @AutoConfigureWireMock(port=0) 으로 하면 random port 할당
 * @SpringBootTest(properties = {"member.service.url=http://localhost:${wiremock.server.port}"} 처럼 하면 application.yml에 있는 member.service.url값을 변경(override)
 * 
 * 2. 정적변경(해당 속성을 정적 파일에 세팅한 후 override)
 * application-test.yml을 만들어 그 안에 member.service.url을 재정의 한 후,
 * test code에서는 @ActiveProfiles("test")와 같이 application-xxx.yml의 xxx에 해당하는 것을 profile로 여기므로 그 파일을 불러서 사용하는 방법이 있음
 * 
 * ##2##
 * 이 ServiceTest는 slice test를 하지 않고, 통합테스트와 같이 실시한다.
 * repository가 인터페이스여서 DevonTester에서 지원하지 않는 이유도 있고,
 * CommonDao의 경우, 공통 bean으로 sql호출하는 용도이므로, 해당 퍼시스턴스 레이어를 slice테스트 하지 않기 때문이며,
 * 그렇다 하더라도 독립적인 테스트를 위해 in-memory db를 사용한다.(이를 위해 application.yml파일이 실제 db설정이 되어 있다면 datasource를 변경해야 하는 일이 필요)
 * 
 * slice 테스트가 필요하다면 repository, commonDao등 하위 레이어는 모두 @Mock처리하여 @Inject해야 한다.
 * 
 * 이 레이어의 test가 slice test가 아니더라도 외부 서비스 호출은 독립적 테스트를 보장하지 못하므로,
 * wiremock을 활용한 stubbing을 하여야 하며
 * 추후 consumer side의 contract 테스트에 재활용 될 수 있다.(stubbing코드를 StubRunner설정으로 대체하는 작업 필요)
 */
}
