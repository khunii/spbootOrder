package tqa.demo.order.test;

import java.io.File;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

public class ReadAndWriteYaml {

	public static void main(String args[]) throws Exception {
		// write할 때 문서 첫줄에 --- 가 프린트 되는 현상 disable, yml만들때 값에 "" 제거하는 것 enable
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER)
				.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES));
		// application.yml에는 있는데, AppYaml에 없는 속성이 있더라도 무시
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		AppYaml yaml = mapper.readValue(new File("src/main/resources/application.yml"), AppYaml.class);

		for (AppYaml.Services service : yaml.getFramework().getDomain().services) {
			System.out.println("name : " + service.getName());
			System.out.println("url : " + service.getUrl());
		}
		System.out.println("apim endpoint-url : " + yaml.getFramework().getApimConsumer().getEndpointUrl());

		// modify application.yml for Testing into application-local.yml
		System.out.println("--------------------------------------------------------------------------");
		System.out.println("After modifying application.yml");
		System.out.println("---------------------------------------------------------------------------");

		for (AppYaml.Services service : yaml.getFramework().getDomain().services) {
			service.setUrl("http://localhost:${wiremock.server.port}");
			System.out.println("url : " + service.getUrl());
		}

		String originalEndpointUrl = yaml.getFramework().getApimConsumer().getEndpointUrl();
		String tailUrl = originalEndpointUrl.split(":[0-9]+")[1];
		yaml.getFramework().getApimConsumer().setEndpointUrl("http://localhost:${wiremock.server.port}" + tailUrl);
		System.out.println("apim endpoint-url : " + yaml.getFramework().getApimConsumer().getEndpointUrl());

		mapper.writeValue(new File("src/test/resources/application-local.yaml"), yaml);

	}

}
