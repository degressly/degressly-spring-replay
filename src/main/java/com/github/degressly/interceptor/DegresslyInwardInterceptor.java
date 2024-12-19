package com.github.degressly.interceptor;

import lombok.RequiredArgsConstructor;
import com.github.degressly.config.AbstractDegresslyConfig;
import com.github.degressly.logger.RequestResponseLogger;
import com.github.degressly.wrapper.CustomHttpServletRequestWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class DegresslyInwardInterceptor implements HandlerInterceptor {

	private final AbstractDegresslyConfig degresslyConfig;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		var customHttpServletRequestWrapper = new CustomHttpServletRequestWrapper(request);

		RequestResponseLogger.logIncomingRequest(degresslyConfig.getTraceId(), customHttpServletRequestWrapper);
		return true;
	}

}
