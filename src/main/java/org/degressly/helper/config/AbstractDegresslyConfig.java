package org.degressly.helper.config;

public interface AbstractDegresslyConfig {

	/**
	 * @return String containing traceId obtained from ThreadLocal/MDC/etc.
	 */
	String getTraceId();

	/**
	 * @param traceId
	 * @return true/false, depending on whether current trace should be picked. Useful for
	 * performing sampling.
	 */
	default boolean pickTrace(String traceId) {
		return false;
	}

}
