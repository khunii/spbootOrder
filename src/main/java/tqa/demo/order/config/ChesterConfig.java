package tqa.demo.order.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tqa.demo.order.Koon;

@Configuration
public class ChesterConfig {
	@Autowired
	ApplicationContext appCtx;

	@ConditionalOnSingleCandidate(Chester.class)
	@Bean
	public Koon koon() {
		
		Koon k = new Koon(appCtx.getBean(Chester.class));
		System.out.println("koonConfig....");
		return k;
	}
	
	@Bean
	@ConditionalOnBean(Koon.class)
	public Chester chester() {
		Chester ch = new Chester();
		System.out.println("aaa");
		return ch;
	}
	@Bean
	@ConditionalOnBean(Koon.class)
	public Chester chester2() {
		Chester ch = new Chester();
		System.out.println("bbb");
		return ch;
	}
	
	@Bean
	@ConditionalOnBean(Koon.class)
	public Chester chester3() {
		Chester ch = new Chester();
		System.out.println("ccc");
		return ch;
	}
	


}
