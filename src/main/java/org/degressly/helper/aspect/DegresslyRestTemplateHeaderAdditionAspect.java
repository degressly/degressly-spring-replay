package org.degressly.helper.aspect;

import org.degressly.helper.config.AbstractDegresslyConfig;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;

@Aspect
@Order(2)
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("degressly.caller.id")
public class DegresslyRestTemplateHeaderAdditionAspect {

	@Value("${degressly.caller.id}")
	private String DEGRESSLY_CALLER_ID;

	private final AbstractDegresslyConfig degresslyConfig;

	@Around("execution(* org.springframework.web.client.RestTemplate.exchange(..))")
	public Object addHeadersToExchange(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();

		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg instanceof RequestEntity) {
				RequestEntity<?> entity = (RequestEntity<?>) arg;
				HttpHeaders headers = new HttpHeaders();
				headers.addAll(entity.getHeaders());

				// Add the required headers
				headers.add("x-degressly-trace-id", degresslyConfig.getTraceId());
				headers.add("x-degressly-caller", DEGRESSLY_CALLER_ID);

				// Create a new HttpEntity with the updated headers
				RequestEntity<?> newEntity = new RequestEntity<>(entity.getBody(), headers, entity.getMethod(),
						entity.getUrl());

				// Replace the old entity with the new one
				args[i] = newEntity;
			}
		}

		return joinPoint.proceed(args);
	}

}
