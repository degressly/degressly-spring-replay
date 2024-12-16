package org.degressly.helper.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonUtils {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private static ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public static String getJsonString(Object object) {
		if (object == null) {
			return null;
		}
		return getObjectMapper().convertValue(object, JsonNode.class).toString();
	}

}
