package org.degressly.helper.aspect;

import org.degressly.helper.config.AbstractDegresslyConfig;
import org.degressly.helper.logger.RequestResponseLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Slf4j
@Order(1)
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("degressly.enable.logging")
public class DegresslyRestTemplateLoggerAspect {

	private final AbstractDegresslyConfig degresslyConfig;

	@PostConstruct
	public void init() {
		log.info("Here");
	}

	@Around("execution(* org.springframework.web.client.RestTemplate.exchange(..))")
	public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {

		if (!degresslyConfig.pickTrace(degresslyConfig.getTraceId())) {
			return joinPoint.proceed();
		}

		Optional<RequestEntity<?>> requestEntity = Optional.empty();

		try {
			Object[] args = joinPoint.getArgs();
			requestEntity = getRequestEntity(args);
			ResponseEntity<?> response;

			response = (ResponseEntity<?>) joinPoint.proceed();
			requestEntity.ifPresent(request -> RequestResponseLogger.logOutgoingRequest(degresslyConfig.getTraceId(),
					request, response.getStatusCodeValue(), response.getHeaders(), response.getBody()));
			return response;
		}
		catch (Throwable e) {
			requestEntity.ifPresent(
					(entity) -> RequestResponseLogger.logFailedOutgoingRequest(degresslyConfig.getTraceId(), entity));
			throw e;
		}
	}

	private Optional<RequestEntity<?>> getRequestEntity(Object[] args) {
		try {
			for (Object arg : args) {
				if (arg instanceof RequestEntity) {
					return Optional.of((RequestEntity<?>) arg);
				}
			}
		}
		catch (Exception e) {
			// Do nothing
		}
		return Optional.empty();
	}

}
