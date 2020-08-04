package tqa.demo.order.test;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import org.apache.coyote.http11.filters.BufferedInputFilter;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.representer.Representer;

public class ReadAndWriteYaml {

	public static void main(String args[]) throws Exception {
		//read 세팅
		Constructor cns = new Constructor(AppYaml.class);
		cns.setPropertyUtils(new PropertyUtils() {
			@Override
			public Property getProperty(Class<? extends Object> type, String name){
				if ( name.indexOf('-') > -1 ) {
					name = CamelCase.camelize(name);
				}
				return super.getProperty(type, name);
			}
		});
		cns.getPropertyUtils().setSkipMissingProperties(true);
		Yaml mYaml = new Yaml(cns);
		File mYamlFile = new File("src/main/resources/application.yml");
		InputStream ins = new FileInputStream(mYamlFile);
		Iterable<Object> iter = mYaml.loadAll(ins);

		//Writer 세팅
		// write할 때 문서 첫줄에 --- 가 프린트 되는 현상 disable, yml만들때 값에 "" 제거하는 것 enable
		YAMLFactory yamlFactory = new YAMLFactory().disable(Feature.WRITE_DOC_START_MARKER)
				.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
		ObjectMapper mapper = new ObjectMapper(yamlFactory);
		// application.yml에는 있는데, AppYaml에 없는 속성이 있더라도 무시
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		//profile이 local인것만 유효
        AppYaml yaml = null;
        for(Object obj:iter){
        	if (obj instanceof AppYaml){
        		System.out.println(obj);
        		AppYaml temp = (AppYaml)obj;
        		if ("local".equals(temp.getSpring().getProfile())){
        			yaml = temp;
        			break;
				}
			}
		}

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

		mapper.writeValue(new File("src/test/resources/application-local.yml"), yaml);

	}

}
