package org.degressly.helper.interceptor;

import lombok.RequiredArgsConstructor;
import org.degressly.helper.config.AbstractDegresslyConfig;
import org.degressly.helper.logger.RequestResponseLogger;
import org.degressly.helper.wrapper.CustomHttpServletRequestWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
