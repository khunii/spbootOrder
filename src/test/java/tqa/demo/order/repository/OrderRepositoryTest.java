package tqa.demo.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import tqa.demo.order.dao.CommonDao;
import tqa.demo.order.entity.OrderEntity;
import tqa.demo.order.entity.OrderedProductEntity;
import tqa.demo.order.entity.ShippingAddressEntity;
import tqa.demo.order.entity.Status;
import tqa.demo.order.util.Utils;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureMybatis
@Import(CommonDao.class)
public class OrderRepositoryTest {
   @Autowired
   private OrderRepository repo;
   
   @Autowired
   CommonDao commonDao;
   
   //before, after sql 처리용[maven조정 필요]
   @Autowired
   private JdbcTemplate template;
   
   @Before
   public void setUp() {
	   //before sql
	   StringBuilder sql = new StringBuilder();
	   sql.append("insert into orders values(1, 'jacob_test', '20200701', '20200701', 'ORDERED');");
	   sql.append("insert into ordered_product values (1, 1, 2000, 10, 1);");
	   sql.append("insert into ordered_product values (2, 2, 1000, 20, 1);");
	   sql.append("insert into shipping_address values (1, '123-345', 'jacob_test', 1);");
	   template.batchUpdate(sql.toString().split(";"));
   }
   
   @After
   public void teadDown() throws Exception{
	   //after sql
	   StringBuilder sql = new StringBuilder();
	   sql.append("delete from ordered_product where id = 1;");
	   sql.append("delete from ordered_product where id = 2;");
	   sql.append("delete from shipping_address where id = 1;");
	   sql.append("delete from orders where id = 1;");
	   template.batchUpdate(sql.toString().split(";"));
   }
   
   @Test
   public void testGetOne() {
	   //given
	   Long id = 1L;
	   //when
	   OrderEntity actual = repo.getOne(id);
	   //then
	   assertThat(actual.getId()).isEqualTo(1L);
	   assertThat(actual.getOrderUserId()).isEqualTo("jacob_test");
	   assertThat(actual.getOrderedDate()).isEqualTo("20200701");
	   assertThat(actual.getOrderedProducts().size()).isEqualTo(2);
	   assertThat(actual.getShippingAddress().getRecipient()).isEqualTo("jacob_test");
   }
   
   @Test
   public void testSave() {
	   //given
	   OrderEntity orderEntity = OrderEntity.builder()
			   .orderUserId("jacob_test")
			   .orderedDate("20200702")
			   .updatedDate("20200702")
			   .status(Status.ORDERED)
			   .orderedProduct(OrderedProductEntity.builder().productId(1L).price(2000L).qty(10).build())
			   .orderedProduct(OrderedProductEntity.builder().productId(2L).price(1000L).qty(20).build())
			   .shippingAddress(ShippingAddressEntity.builder().recipient("Jacob_test").zipCode("123-345").build())
			   .build();

	   orderEntity.getOrderedProducts().forEach(e->e.setOrderEntity(orderEntity));
	   orderEntity.getShippingAddress().setOrderEntity(orderEntity);
	   repo.save(orderEntity);
	   
	   //when
	   OrderEntity actual = repo.getOne(orderEntity.getId());
	   
	   //then
	   assertThat(actual.getOrderedDate()).isEqualTo("20200702");
   }
   
   @Test
   public void testUpdateStatus() {
	   //given
	   OrderEntity expected = commonDao.selectOne("findByOrderId", 1);
	   expected.updateStatus(Status.SHIPPED);
	   
	   //when
	   commonDao.update("updateStatus", expected);
	   OrderEntity actual = commonDao.selectOne("findByOrderId", expected.getId());
	   
	   //then
	   assertThat(actual.getStatus()).isEqualTo(Status.SHIPPED);
	   assertThat(actual.getUpdatedDate()).isEqualTo(Utils.getYYYMMDD());
   }
   
   @Test
   public void testSelectList() {
	   //given
	   String fromDate = "20200701";
	   String toDate = "20200731";
	   
	   //when
	   List<OrderEntity> orderList = commonDao.selectList("findByOrderedDate", fromDate, toDate);
	   
	   //then
	   assertThat(orderList.size()).isEqualTo(1);
   }
}
