package org.degressly.helper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DegresslyRequest {

	private String type;

	private String traceId;

	private String method;

	private String url;

	private int statusCode;

	private MultiValueMap<String, String> headers;

	private MultiValueMap<String, String> params;

	private Object body;

	private MultiValueMap<String, String> responseHeaders;

	private Object responseBody;

	private boolean throwException;

	private long responseTime;

	public void setUrl(String url) {
		if (url.contains("?")) {
			this.setParams(getParamsFromUrl(url));
			this.url = url.split("\\?")[0];
			return;
		}
		this.url = url;
	}

	private MultiValueMap<String, String> getParamsFromUrl(String url) {
		String paramsString = url.split("\\?")[1];
		List<NameValuePair> nameValuePairList = URLEncodedUtils.parse(paramsString, StandardCharsets.UTF_8);
		MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
		nameValuePairList.forEach(nvp -> multiValueMap.put(nvp.getName(), Collections.singletonList(nvp.getValue())));
		return new LinkedMultiValueMap<>(multiValueMap);
	}

	public static class DegresslyRequestBuilder {

		public DegresslyRequestBuilder url(String url) {
			DegresslyRequest tempRequest = new DegresslyRequest();
			tempRequest.setUrl(url);
			this.url = tempRequest.getUrl();
			this.params = tempRequest.getParams();
			return this;
		}

	}

}