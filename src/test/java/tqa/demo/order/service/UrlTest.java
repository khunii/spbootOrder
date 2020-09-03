package tqa.demo.order.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.lide.uta.core.launcher.springboot.UTAConfiguration;
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration(exclude=UTAConfiguration.class)
//@ActiveProfiles("test")//src/test/resources 에 application-test.yml이 있어야 함
//@AutoConfigureWireMock(port=0)
//@TestPropertySource(properties={"member.service.url=http://localhost:${wiremock.server.port}"})
@EnableConfigurationProperties
public class UrlTest {
	
	@Value("${member.service.url}")
	public String testedUrl;
	
	@Test
	public void valueTest() throws Exception{
		System.out.println(testedUrl);
	}
}
