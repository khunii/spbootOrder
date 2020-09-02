package tqa.demo.order.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.lide.uta.core.launcher.springboot.UTAConfiguration;

import tqa.demo.order.dao.CommonDao;
import tqa.demo.order.dto.OrderDTO;
import tqa.demo.order.dto.OrderedProductDTO;
import tqa.demo.order.dto.ShippingAddressDTO;
import tqa.demo.order.entity.OrderEntity;
import tqa.demo.order.entity.Status;
import tqa.demo.order.repository.OrderRepository;
import tqa.demo.order.util.Utils;

/*
 * 아래는 고정값으로 템플릿화 하는데, 그 중 @SpringBootTest의 properties는 개발자가 필요시 고쳐야 하므로 주석처리한다.
 * insert같은 곳에서 publish하는 것은 해당 객체 @mock처리 필수
 */
@RunWith(SpringRunner.class)
/*
 * DTester 오류있는듯...
 * WebEnvironment.NONE으로 하면 No ServletContext오류 발생함, 어떻게 해야 할까?
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration(exclude=UTAConfiguration.class)
@ActiveProfiles("test")//src/test/resources 에 application-test.yml이 있어야 함
@AutoConfigureWireMock(port=0)
@TestPropertySource(properties={"member.service.url=http://localhost:${wiremock.server.port}"})
@EnableConfigurationProperties
public class OrderServiceMockBeanTest {

  
   //테스트 대상 클래스를 주입
   @Autowired
   private OrderService orderService;
   
   //before, after sql 처리용[maven조정 필요]
   @Autowired
   private JdbcTemplate template;

   //데이터 생성을 위하여 주입했으나, 자동생성시에는 SQL문을 돌리도록 할 지 repository를 이용할 지는 결정 후 적용
   @Autowired
   private OrderRepository repo;
   
   @SpyBean
   private CommonDao dao;
   
   @Before
   public void setUp() throws Exception{
		//아래(wiremock을 통한 mocking)는 고민, 외부 서비스를 mocking하는 것인데 이건 처음버전은 템플릿(주석처리)을 제공하고 개발자가 보완하도록 처리
	   stubFor(get(urlMatching("/product/stock/v1/retireveAvailable/([0-9])+")).willReturn(aResponse().withStatus(200)
			   .withHeader("Content-Type", "application/json;charset=UTF-8")
			   .withBody("1")));
	   
	   //before sql
	   StringBuilder sql = new StringBuilder();
	   sql.append("insert into orders values(1, 'jacob_test', '20200701', '20200701', 'PREPARED');");
	   sql.append("insert into orders values(2, 'jacob_test', '20200731', '20200731', 'PREPARED');");
	   sql.append("insert into ordered_product values (1, 1, 2000, 10, 1);");
	   sql.append("insert into ordered_product values (2, 2, 1000, 20, 1);");
	   sql.append("insert into ordered_product values (3, 3, 4000, 30, 2);");
	   sql.append("insert into ordered_product values (4, 4, 3000, 40, 2);");
	   sql.append("insert into shipping_address values (1, '123-345', 'jacob_test', 1);");
	   sql.append("insert into shipping_address values (2, '123-345', 'jacob_test', 2);");
	   template.batchUpdate(sql.toString().split(";"));
	   
   }
   
   @After
   public void tearDown() throws Exception{
	   //after sql
	   StringBuilder sql = new StringBuilder();
	   sql.append("delete from ordered_product where id = 1;");
	   sql.append("delete from ordered_product where id = 2;");
	   sql.append("delete from ordered_product where id = 3;");
	   sql.append("delete from ordered_product where id = 4;");
	   sql.append("delete from shipping_address where id = 1;");
	   sql.append("delete from shipping_address where id = 2;");
	   sql.append("delete from orders where id = 1;");
	   sql.append("delete from orders where id = 2;");
	   template.batchUpdate(sql.toString().split(";"));
   }
   
  
   
   @Test
   public void testUpdateOrderStatus_VerifyReturnValue() throws Exception{
	   //given(입력값)
	   OrderDTO modified = new OrderDTO();
	   modified.setId(1L);
	   modified.setStatus(Status.DELIVERED);
	   
	   //when
	   int count = orderService.updateOrderStatus(modified);
	   
	   System.out.println("Normal count :" + count);
	   //then
	   assertThat(count).isEqualTo(1);
   }

   @Test
   public void testUpdateOrderStatus_withMock() throws Exception{
	   //given(입력값)
	   OrderDTO modified = new OrderDTO();
	   modified.setId(1L);
	   modified.setStatus(Status.DELIVERED);
	   
	   Mockito.doReturn(100).when(dao).update(Mockito.anyString(), Mockito.any());
	   
	   //when
	   int count = orderService.updateOrderStatus(modified);
	   
	   System.out.println("withModk count :" + count);
	   
	   //then
	   assertThat(count).isEqualTo(100);
   }

   @Test(expected=Exception.class)
   public void testUpdateOrderStatus_withMock_Exception() throws Exception{
	   //given(입력값)
	   OrderDTO modified = new OrderDTO();
	   modified.setId(1L);
	   modified.setStatus(Status.DELIVERED);
	   
	   Mockito.doThrow(Exception.class).when(dao).update(Mockito.anyString(), Mockito.any());
	   
	   //when
	   int count = orderService.updateOrderStatus(modified);
	   
	   System.out.println("withModk count :" + count);
	   
	   //then
	   assertThat(count).isEqualTo(100);
   }

   @Test
   public void testGetOrders() throws Exception{
	   //given
	   String fromDate = "20200701";
	   String toDate = "20200731";
	   
	   //when
	   List<OrderDTO> orders = orderService.getOrders(fromDate, toDate);
	   
	   System.out.println(orders);
	   
	   //then
	   assertThat(orders.size()).isGreaterThanOrEqualTo(2);
   }
  
   @Test
   public void testGetOrders_withMock() throws Exception{
	   //given
	   String fromDate = "20200701";
	   String toDate = "20200731";
	   
	   List<OrderedProductDTO> productDTOList = new ArrayList<>();
	   productDTOList.add(OrderedProductDTO.builder().productId(1L).price(2000L).qty(10).build());
	   productDTOList.add(OrderedProductDTO.builder().productId(2L).price(1000L).qty(20).build());
	   
	   OrderDTO orderDTO = OrderDTO.builder()
			   .orderUserId("mock_test")
			   .orderedDate("20201231")
			   .updatedDate("20201231")
			   .status(Status.PREPARED)
			   .orderedProducts(productDTOList)
			   .shippingAddress(ShippingAddressDTO.builder().recipient("mock_test").zipCode("999999").build())
			   .build();

	   OrderEntity mockEntity = Utils.toOrderEntity(orderDTO);
	   List<OrderEntity> mockList = new ArrayList<>();
	   mockList.add(mockEntity);
	   
	   Mockito.doReturn(mockList).when(dao).selectList(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	   
	   //when
	   List<OrderDTO> orders = orderService.getOrders(fromDate, toDate);
	   
	   System.out.println(orders);
	   
	   //then
	   assertThat(orders.size()).isGreaterThanOrEqualTo(1);
   }
   
}
