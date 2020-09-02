package tqa.demo.order.config;

import lombok.Data;

@Data
public class Chester {
	
	public Chester() {}
	
	public Chester(String realName) {
		
		this.realName = realName;
	}
	
	public String realName;
	public int age;
	public String familyName;

}
