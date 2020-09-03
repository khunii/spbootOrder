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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.lide.uta.core.launcher.springboot.UTAConfiguration;

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
/*
 * DTester 오류있는듯...
 * WebEnvironment.NONE으로 하면 No ServletContext오류 발생함, 어떻게 해야 할까?
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration(exclude=UTAConfiguration.class)
@ActiveProfiles("test")//src/test/resources 에 application-test.yml이 있어야 함
@AutoConfigureWireMock(port=0)
//@TestPropertySource(properties={"member.service.url=http://localhost:${wiremock.server.port}"})
@EnableConfigurationProperties
public class OrderServiceTest {

  
   //테스트 대상 클래스를 주입
   @Autowired
   private OrderService orderService;
   
   //before, after sql 처리용[maven조정 필요]
   @Autowired
   private JdbcTemplate template;

   //데이터 생성을 위하여 주입했으나, 자동생성시에는 SQL문을 돌리도록 할 지 repository를 이용할 지는 결정 후 적용
   @Autowired
   private OrderRepository repo;
   
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
   
   @Test@Ignore
   public void testUpdateShippingAddress() throws Exception{
	   //given(입력값)
	   ShippingAddressDTO addressDTO = new ShippingAddressDTO();
	   addressDTO.setOrderId(1L);
	   addressDTO.setZipCode("999-999");
	   addressDTO.setRecipient("testRecipient");
	   
	   //when
	   OrderDTO modified = orderService.updateShippingAddress(addressDTO);
	   
	   //then
	   assertThat(modified.getShippingAddress().getZipCode()).isEqualTo("999-999");
	   assertThat(modified.getShippingAddress().getRecipient()).isEqualTo("testRecipient");
   }
   
   @Test@Ignore
   public void testUpdateOrderStatus_VerifyReturnValue() throws Exception{
	   //given(입력값)
	   OrderDTO modified = new OrderDTO();
	   modified.setId(1L);
	   modified.setStatus(Status.DELIVERED);
	   
	   //when
	   int count = orderService.updateOrderStatus(modified);
	   
	   //then
	   assertThat(count).isEqualTo(1);
   }
   
   /*
    * 이렇게 실행결과와 db에서 다시 조회해서 하는 형태는 사실상 DTester에서는 불가능
    */
   @Test@Ignore
   public void testUpdateOrderStatus_VerifyFromDB() throws Exception{
	   //given(입력값)
	   OrderDTO modified = new OrderDTO();
	   modified.setId(1L);
	   modified.setStatus(Status.DELIVERED);
	   
	   //when
	   int count = orderService.updateOrderStatus(modified);
	   OrderEntity queriedOrder = repo.findById(modified.getId()).get();
	   
	   //then
	   assertThat(count).isEqualTo(1);
	   assertThat(queriedOrder.getStatus()).isEqualTo(Status.DELIVERED);
   }
   
   @Test@Ignore
   public void testDeleteOrder() throws Exception{
	   //given
	   Long id = 1L;
	   
	   //when
	   int count = orderService.deleteOrder(id);
	   
	   //then
	   assertThat(count).isEqualTo(1);
   }
   
   @Test@Ignore
   public void testGetOrders() throws Exception{
	   //given
	   String fromDate = "20200701";
	   String toDate = "20200731";
	   
	   //when
	   List<OrderDTO> orders = orderService.getOrders(fromDate, toDate);
	   
	   //then
	   assertThat(orders.size()).isGreaterThanOrEqualTo(2);
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
 * repository가 인터페이스여서 DTester에서 지원하지 않는 이유도 있고,
 * CommonDao의 경우, 공통 bean으로 sql호출하는 용도이므로, 해당 퍼시스턴스 레이어를 slice테스트 하지 않기 때문이며,
 * 그렇다 하더라도 독립적인 테스트를 위해 in-memory db를 사용한다.(이를 위해 application.yml파일이 실제 db설정이 되어 있다면 datasource를 변경해야 하는 일이 필요)
 * 
 * slice 테스트가 필요하다면 repository, commonDao등 하위 레이어는 모두 @Mock처리하여 @Inject해야 한다.
 * 
 * 이 레이어의 test가 slice test가 아니더라도 외부 서비스 호출은 독립적 테스트를 보장하지 못하므로,
 * wiremock을 활용한 stubbing을 하여야 하며
 * 추후 consumer side의 contract 테스트에 재활용 될 수 있다.(stubbing코드를 StubRunner설정으로 대체하는 작업 필요)
 * 
 * ##3##
 * 내가 필요한 sql을 통해 테스트 클래스 실행시(즉, springboot실행시)초기 데이터 로딩하는 방법
 * @TestPropertySource(properties={"spring.datasource.data=classpath:data_for_serviceTest.sql"});
 * 위 처럼 spring.datasource.data에 내가 원하는 sql파일을 지정하면 되며, sql파일은 src/test/resources에 위치하면 된다.
 * data.sql은 무조건 loading하는 스타일이고, 이것은 embeddedDB일때만 default이고 아니면 안된다. 되게 하려면, spring.datasource.initialization-mode=always로 세팅해야 한다.
 * 만일 다양한 datasource가 있고(다양한 DB) 그것마다 처음에 실행시키고 싶다면 data-h2.sql, data-mysql.sql 등으로 datasource의 platform명을 붙여서 만들면 된다.
 * 
 * 참고로, 
 * application-test.yml이 src/test/resources에 위치하면
 * @ActiveProfiles("test") 로 해당 설정을 기초로 springboot 로드 가능하고
 * 이때의 property를 동적으로 변경하려면 위에처럼 @TestPropertySource(properties={"...=...}); 식으로 변경하고
 * 이 값을 테스트 코드에서 참조하려면 @Value("${test.xxxxxxx}") 로 주입받아서 참조 가능하다.
 */
 
}
