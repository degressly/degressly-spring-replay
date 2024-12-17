package org.degressly.helper.wrapper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.StringUtils;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private String body;

	private Map<String, String> headerMap;

	private Map<String, String> paramMap;

	private JsonNode jsonBody;

	private boolean xFormUrlEncoded;

	private String contentType;

	public CustomHttpServletRequestWrapper(HttpServletRequest servletRequest)
			throws JsonProcessingException, IOException {
		super(servletRequest);
		contentType = servletRequest.getHeader("content-type");
		xFormUrlEncoded = StringUtils.isNotBlank(contentType) && contentType.contains("x-www-form-urlencoded");

		StringBuilder stringBuilder = new StringBuilder("");
		BufferedReader bufferedReader = null;

		if (!xFormUrlEncoded) {
			try {
				InputStream inputStream = servletRequest.getInputStream();

				if (inputStream != null) {
					bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

					char[] charBuffer = new char[128];
					int bytesRead = -1;

					while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
						stringBuilder.append(charBuffer, 0, bytesRead);
					}
				}
				else {
					stringBuilder.append("");
				}
			}
			catch (IOException ex) {
				logger.error("Error reading the request body...{}", ex);
			}
			finally {
				if (bufferedReader != null) {
					try {
						bufferedReader.close();
					}
					catch (IOException ex) {
						logger.error("Error closing bufferedReader... {}", ex);
					}
				}
			}
		}
		headerMap = new HashMap<>();
		paramMap = new HashMap<>();
		Enumeration headerEnum = servletRequest.getHeaderNames();
		while (headerEnum.hasMoreElements()) {
			String str = (String) headerEnum.nextElement();
			headerMap.put(str, servletRequest.getHeader(str));
		}
		Map<String, String[]> map = servletRequest.getParameterMap();
		String temp = null;
		for (String key : map.keySet()) {
			temp = map.get(key)[0];
			paramMap.put(key, temp);
			if (xFormUrlEncoded) {
				stringBuilder.append(key).append("=").append(temp).append("&");
			}
		}
		body = stringBuilder.toString();

		if (StringUtils.isNotBlank(contentType) && StringUtils.containsIgnoreCase(contentType, "application/json")
				&& StringUtils.isNotBlank(body)) {

			try {
				jsonBody = new ObjectMapper().readTree(body);
			}
			catch (JsonParseException e) {
				logger.error("Could not parse into Json {}", e.getLocalizedMessage());
				jsonBody = null;
			}
		}
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());

		ServletInputStream inputStream = new ServletInputStream() {
			@Override
			public boolean isFinished() {
				return false;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(ReadListener readListener) {

			}

			public int read() throws IOException {
				return byteArrayInputStream.read();
			}
		};

		return inputStream;
	}

	public String[] getParameterValues(String parameter) {
		String[] values = super.getParameterValues(parameter);
		if (values == null) {
			return null;
		}
		int count = values.length;
		String[] encodedValues = new String[count];
		for (int i = 0; i < count; i++) {
			encodedValues[i] = cleanXSS(values[i]);
		}
		return encodedValues;
	}

	public String getParameter(String parameter) {
		String value = super.getParameter(parameter);

		if (value == null && StringUtils.isNotBlank(contentType)
				&& StringUtils.containsIgnoreCase(contentType, "application/json") && jsonBody != null
				&& jsonBody.get(parameter) != null) {

			return getRequestParamForApplicationJson(parameter);
		}

		if (value == null) {
			return null;
		}

		return cleanXSS(value);
	}

	private String getRequestParamForApplicationJson(String parameter) {

		String value = jsonBody.get(parameter).textValue();

		if (value == null) {

			try {
				logger.trace("json param: {}", jsonBody.get(parameter));
				value = new ObjectMapper().writeValueAsString(jsonBody.get(parameter));
				logger.trace("json param value {}", value);
			}
			catch (JsonProcessingException e) {
				logger.debug("Exception {}", e);
			}
		}

		return value;
	}

	public String getHeader(String name) {
		String value = super.getHeader(name);
		if (value == null) {
			return null;
		}
		return cleanXSS(value);
	}

	private String cleanXSS(String value) {
		// logger.debug("Before encoding : "+ value);
		value = Encode.forHtml(value);
		// logger.debug("After encoding : "+ value);
		return value;
	}

	public void addHeader(String key, String value) {
		headerMap.put(key, value);
	}

	public String getBody() {

		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Map<String, String> getHeaderMap() {
		return headerMap;
	}

	public Map<String, String> getParamMap() {
		return paramMap;
	}

	public JsonNode getJsonBody() {
		return jsonBody;
	}

	public void setJsonBody(JsonNode jsonBody) {
		this.jsonBody = jsonBody;
	}

	public boolean isxFormUrlEncoded() {
		return xFormUrlEncoded;
	}

	public void setxFormUrlEncoded(boolean xFormUrlEncoded) {
		this.xFormUrlEncoded = xFormUrlEncoded;
	}

}
