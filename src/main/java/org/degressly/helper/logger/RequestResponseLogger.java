package org.degressly.helper.logger;

import org.degressly.helper.config.ThreadConfigManager;
import org.degressly.helper.dto.DegresslyRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.degressly.helper.wrapper.CustomHttpServletRequestWrapper;
import org.springframework.http.RequestEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.degressly.helper.utils.JsonUtils;

import java.util.*;

@Slf4j
@UtilityClass
public class RequestResponseLogger {

	public static void logIncomingRequest(String traceId, CustomHttpServletRequestWrapper request) {
		try {
			ThreadConfigManager.runAsync(traceId, () -> {
				MultiValueMap<String, String> headers = getHeaders(request);
				MultiValueMap<String, String> params = getMultiValueMapFromArrayMap(request.getParameterMap());

				DegresslyRequest degresslyIncomingRequest = DegresslyRequest.builder().type("INCOMING").traceId(traceId)
						.method(request.getMethod()).url(request.getRequestURL().toString()).headers(headers)
						.body(request.getBody()).params(params).build();

				log.info(JsonUtils.getJsonString(degresslyIncomingRequest));
			});
		}
		catch (Exception e) {
			// Do nothing
		}
	}

	public void logOutgoingRequest(String traceId, RequestEntity<?> request, int statusCode,
			MultiValueMap<String, String> responseHeaders, Object responseBody) {

		try {
			ThreadConfigManager.runAsync(traceId, () -> {
				MultiValueMap<String, String> headers = request.getHeaders();
				String body = (request.getBody() != null) ? request.getBody().toString() : null;

				String responseBodyString = null;
				if (responseBody != null) {
					if (responseBody instanceof String) {
						responseBodyString = (String) responseBody;
					}
					else {
						responseBodyString = JsonUtils.getJsonString(responseBody);
					}
				}

				DegresslyRequest degresslyRequest = DegresslyRequest.builder().traceId(traceId)
						.method(Objects.requireNonNull(request.getMethod()).name()).statusCode(statusCode)
						.headers(headers).type("OUTGOING").url(request.getUrl().toString()).body(body)
						.responseHeaders(responseHeaders).responseBody(responseBodyString).build();

				log.info(JsonUtils.getJsonString(degresslyRequest));
			});
		}
		catch (Exception e) {
			// Do nothing
		}
	}

	public void logFailedOutgoingRequest(String traceId, RequestEntity<?> request) {

		try {
			ThreadConfigManager.runAsync(traceId, () -> {

				MultiValueMap<String, String> headers = request.getHeaders();
				String body = (request.getBody() != null) ? request.getBody().toString() : null;

				DegresslyRequest degresslyRequest = DegresslyRequest.builder().type("OUTGOING").traceId(traceId)
						.headers(headers).url(request.getUrl().toString()).body(body).throwException(true).build();

				log.info(JsonUtils.getJsonString(degresslyRequest));
			});
		}
		catch (Exception e) {
			// Do nothing
		}
	}

	private static MultiValueMap<String, String> getMultiValueMapFromMap(Map<String, String> map) {
		MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
		map.forEach((k, v) -> multiValueMap.put(k, Collections.singletonList(v)));
		return multiValueMap;
	}

	private static MultiValueMap<String, String> getHeaders(HttpServletRequest httpServletRequest) {
		var ret = new LinkedMultiValueMap<String, String>();
		var headerNames = httpServletRequest.getHeaderNames();

		while (headerNames.hasMoreElements()) {
			var headerName = headerNames.nextElement();
			var headerValue = CollectionUtils.toArray(httpServletRequest.getHeaders(headerName), new String[] {});
			ret.put(headerName, Arrays.asList(headerValue));
		}

		return ret;
	}

	private static MultiValueMap<String, String> getMultiValueMapFromArrayMap(
			Map<java.lang.String, java.lang.String[]> map) {
		MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
		map.forEach((k, v) -> multiValueMap.put(k, Arrays.asList(v)));
		return multiValueMap;
	}

}
