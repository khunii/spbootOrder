package tqa.demo.order;

//package는 코딩표준에 따르나 이전까지는 해당 클래스의 패키지에 준하여 가져오기
//import는 dto등과 같이 동적으로 변하는 것은 어떻게 처리하는지만 정해지만 나머지는 고정
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.contract.wiremock.restdocs.SpringCloudContractRestDocs;
import org.springframework.cloud.contract.wiremock.restdocs.WireMockRestDocs;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import tqa.demo.order.dto.OrderDTO;
import tqa.demo.order.dto.OrderedProductDTO;
import tqa.demo.order.dto.ShippingAddressDTO;
import tqa.demo.order.entity.Status;
import tqa.demo.order.service.OrderService;

/*
 * Component 테스트는 Controller 를 대상으로 DevOnTester를 사용한 뒤 Junit 생성시 사용한다.
 * 아래 annotation은 고정값으로 템플릿화
 * 단, in-memory db사용을 위해 기존의 application.yml을 복사하여 application-test.yml을 만들어야 한다.
 * 여기에는 datasource가 in-memory를 위한 것으로 변경되어야 하며
 * application-test.yml을 사용시
 * @ActiveProfiles("test")가 추가되어야 한다.
 * insert같은 곳에서 publish하는 것은 해당 객체 @mock처리 필수
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")//src/test/resources 에 application-test.yml이 있어야 함
@AutoConfigureWireMock(port=0)
@TestPropertySource(properties={"member.service.url=http://localhost:${wiremock.server.port}"})
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/snippets")
@AutoConfigureJsonTesters
public class OrderComponentTest {
	
	//고정값
	@Autowired
	MockMvc mockMvc;
	
	//OrderController소스에서 @Autowired나, 생성자 주입하는 타입들을 여기서 주입되도록 설정한다.
	@Autowired
	OrderService orderService;
	
	//각 controller의 return type을 보고 객체일경우(또는 generic이 객체일경우) 아래와 같이 지정한다.
	private JacksonTester<OrderDTO> orderDTOJson;
	private JacksonTester<List<OrderDTO>> orderListDTOJson;
	private JacksonTester<ShippingAddressDTO> addressJson;
	private JacksonTester<Status> statusJson;
	private JacksonTester<String> stringJson;
	
	//before, after sql 처리용[maven조정 필요]
	@Autowired
	private JdbcTemplate template;
	
	@Before
	public void setUp() throws Exception{
		//아래 2라인은 고정값
		ObjectMapper objMapper = new ObjectMapper();
		JacksonTester.initFields(this, objMapper);

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

		//when(Controller메소드 수행)
		/*
		 * 분석모델에 있어야 할 것
		 * 1. Controller class가 가지는 @RequestMapping값(parentURL)
		 * 2. 각 method가 가지는 @RequestMapping의 값(또는 value속성), method속성(있으면), consumes속성(있으면, 이 값은 Content-Type으로 사용)
		 * 3. 2가 아닌 @GetMapping, @PostMapping, @PutMapping, @DeletMapping 이면 URL값("{id}"와 같은 거 앞까지), 여기서 method를 Mapping앞자(Get, Post, Put, Delete)로 받기, contentType은 별말없으면 application-json
		 * 4. 각 method가 가지는 파라미터 중 @PathVariable, @RequestParam, @RequestBody 분리하여 저장   
		 */
		MvcResult actual = this.mockMvc.perform(MockMvcRequestBuilders.post("/demo/order/v1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.orderDTOJson.write(orderDTO).getJson()))
				.andExpect(status().isOk())
				.andDo(WireMockRestDocs.verify()
						.wiremock(
								post("/demo/order/v1")
								.withHeader("Content-Type", containing("application/json"))
								.withRequestBody(matchingJsonPath("$.orderUserId"))
								.withRequestBody(matchingJsonPath("$.orderedDate"))
								.withRequestBody(matchingJsonPath("$.updatedDate"))
								.withRequestBody(matchingJsonPath("$.status"))
								.withRequestBody(matchingJsonPath("$.orderedProducts"))
								.withRequestBody(matchingJsonPath("$.shippingAddress"))
						)
						.stub("placeOrder"))
				.andDo(MockMvcRestDocumentation.document("placeOrder",
						SpringCloudContractRestDocs.dslContract()))
				.andReturn();
		
		//then
		assertThat(actual.getResponse().getContentAsString()).contains("jacob_test","ORDERED");
	}
	
	@Test
	public void testUpdateAddress() throws Exception{
		//given(입력값)
		ShippingAddressDTO addressDTO = new ShippingAddressDTO();
		addressDTO.setOrderId(1L);	   
		addressDTO.setZipCode("999-999");
		addressDTO.setRecipient("testRecipient");

		//when
		/*
		 * 분석모델에 있어야 할 것
		 * 1. Controller class가 가지는 @RequestMapping값(parentURL)
		 * 2. 각 method가 가지는 @RequestMapping의 값(또는 value속성), method속성(있으면), consumes속성(있으면, 이 값은 Content-Type으로 사용)
		 * 3. 2가 아닌 @GetMapping, @PostMapping, @PutMapping, @DeletMapping 이면 URL값("{id}"와 같은 거 앞까지), 여기서 method를 Mapping앞자(Get, Post, Put, Delete)로 받기, contentType은 별말없으면 application-json
		 * 4. 각 method가 가지는 파라미터 중 @PathVariable, @RequestParam, @RequestBody 분리하여 저장   
		 */
		MvcResult actual = this.mockMvc.perform(MockMvcRequestBuilders.put("/demo/order/address/v1/"+addressDTO.getOrderId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.addressJson.write(addressDTO).getJson()))
				.andDo(print())
				.andExpect(status().isOk())
				.andDo(WireMockRestDocs.verify() //UrlPath도 정규표현식처럼 나올 수 있게 하는 방법 필요(사유:pathVariable이 오므로)
						.wiremock(
								put(urlMatching("/demo/order/address/v1/([0-9])+"))
								.withHeader("Content-Type", containing("application/json"))
								.withRequestBody(matchingJsonPath("$.zipCode"))
								.withRequestBody(matchingJsonPath("$.recipient"))
								.withRequestBody(matchingJsonPath("$.orderId"))
						)
						.stub("updateAddress")
						)
				.andDo(MockMvcRestDocumentation.document("updateAddress",
						SpringCloudContractRestDocs.dslContract()))
				.andReturn();

		//then
		assertThat(actual.getResponse().getContentAsString()).contains("999-999","testRecipient");
	}
	
	@Test
	public void testUpdateStatus() throws Exception{
		//given
		Long id = 1L;
		Status status = Status.SHIPPED;

		//when
		/*
		 * 분석모델에 있어야 할 것
		 * 1. Controller class가 가지는 @RequestMapping값(parentURL)
		 * 2. 각 method가 가지는 @RequestMapping의 값(또는 value속성), method속성(있으면), consumes속성(있으면, 이 값은 Content-Type으로 사용)
		 * 3. 2가 아닌 @GetMapping, @PostMapping, @PutMapping, @DeletMapping 이면 URL값("{id}"와 같은 거 앞까지), 여기서 method를 Mapping앞자(Get, Post, Put, Delete)로 받기, contentType은 별말없으면 application-json
		 * 4. 각 method가 가지는 파라미터 중 @PathVariable, @RequestParam, @RequestBody 분리하여 저장   
		 */
		MvcResult actual = this.mockMvc.perform(MockMvcRequestBuilders.put("/demo/order/status/v1/"+id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.statusJson.write(status).getJson()))
				.andDo(print())
				.andExpect(status().isOk())
				.andDo(WireMockRestDocs.verify() 
						.wiremock(
								put(urlMatching("/demo/order/status/v1/([0-9])+"))
								.withHeader("Content-Type", containing("application/json"))
								.withRequestBody(matching("^([\"A-Za-z0-9]+)$"))
								)
						.stub("updateStatus"))
				.andDo(MockMvcRestDocumentation.document("updateStatus",
						SpringCloudContractRestDocs.dslContract()))
				.andReturn();

		//then
		assertThat(actual.getResponse().getContentAsString()).contains("1");
		
	}
	
	@Test
	public void testSelectList() throws Exception{
		//given
		String fromDate = "20200701";
		String toDate = "20200731";

		//when
		/*
		 * 분석모델에 있어야 할 것
		 * 1. Controller class가 가지는 @RequestMapping값(parentURL)
		 * 2. 각 method가 가지는 @RequestMapping의 값(또는 value속성), method속성(있으면), consumes속성(있으면, 이 값은 Content-Type으로 사용)
		 * 3. 2가 아닌 @GetMapping, @PostMapping, @PutMapping, @DeletMapping 이면 URL값("{id}"와 같은 거 앞까지), 여기서 method를 Mapping앞자(Get, Post, Put, Delete)로 받기, contentType은 별말없으면 application-json
		 * 4. 각 method가 가지는 파라미터 중 @PathVariable, @RequestParam, @RequestBody 분리하여 저장   
		 */
		MvcResult actual = this.mockMvc.perform(MockMvcRequestBuilders.get("/demo/order/v1?fromDate="+fromDate+"&toDate="+toDate)
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andDo(WireMockRestDocs.verify() //QueryParam도 정규표현식처럼 나올 수 있게 하는 방법 필요
						.wiremock(
								get(urlEqualTo("/demo/order/v1"))
								.withHeader("Content-Type", containing("application/json"))
								.withQueryParam("fromDate", matching("^([0-9]+)$"))
								.withQueryParam("toDate", matching("^([0-9]+)$"))
								)
						.stub("selectOrders"))
				.andDo(MockMvcRestDocumentation.document("selectOrders",
						SpringCloudContractRestDocs.dslContract()))
				.andReturn();

		//then==> order개수 2개 검증해야 함
		List<OrderDTO> orders = this.orderListDTOJson.parseObject(actual.getResponse().getContentAsString());
		assertThat(orders).hasSize(2);
	}

	@Test
	public void testDeleteOrder() throws Exception{
		//given
		Long id = 1L;

		//when
		/*
		 * 분석모델에 있어야 할 것
		 * 1. Controller class가 가지는 @RequestMapping값(parentURL)
		 * 2. 각 method가 가지는 @RequestMapping의 값(또는 value속성), method속성(있으면), consumes속성(있으면, 이 값은 Content-Type으로 사용)
		 * 3. 2가 아닌 @GetMapping, @PostMapping, @PutMapping, @DeletMapping 이면 URL값("{id}"와 같은 거 앞까지), 여기서 method를 Mapping앞자(Get, Post, Put, Delete)로 받기, contentType은 별말없으면 application-json
		 * 4. 각 method가 가지는 파라미터 중 @PathVariable, @RequestParam, @RequestBody 분리하여 저장   
		 */
		MvcResult actual = this.mockMvc.perform(MockMvcRequestBuilders.delete("/demo/order/v1/"+id)
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andDo(WireMockRestDocs.verify() //UrlPath도 정규표현식처럼 나올 수 있게 하는 방법 필요(사유:pathVariable이 오므로)
						.wiremock(
								delete(urlMatching("/demo/order/v1/([0-9])+"))
								.withHeader("Content-Type", containing("application/json"))
						)
						.stub("deleteOrder"))
				.andDo(MockMvcRestDocumentation.document("deleteOrder",
						SpringCloudContractRestDocs.dslContract())
						)
				.andReturn();

		//then
		assertThat(actual.getResponse().getContentAsString()).contains("1");
	}
}
