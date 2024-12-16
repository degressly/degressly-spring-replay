package org.degressly.helper.config;

import org.slf4j.MDC;

import java.util.concurrent.*;

public class ThreadConfigManager {

	public static ExecutorService DEGRESSLY_REQUEST_RESPONSE_POOL = new ThreadPoolExecutor(1, 1, 0L,
			TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

	public static void runAsync(String seqNo, Runnable runnable) {
		ExecutorService executor = DEGRESSLY_REQUEST_RESPONSE_POOL;
		Runnable overRiddenRunnable = getOverriddenRunnable(seqNo, runnable);
		CompletableFuture.runAsync(overRiddenRunnable, executor);

	}

	private static Runnable getOverriddenRunnable(String seqNo, Runnable runnable) {
		return () -> {
			try {
				MDC.put("seq-no", seqNo);
				runnable.run();
			}
			finally {
				MDC.clear();
			}
		};
	}

}
