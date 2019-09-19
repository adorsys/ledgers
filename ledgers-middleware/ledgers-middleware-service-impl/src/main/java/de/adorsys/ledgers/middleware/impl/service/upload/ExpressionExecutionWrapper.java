package de.adorsys.ledgers.middleware.impl.service.upload;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
class ExpressionExecutionWrapper {
    private static final String ERROR_MESSAGE = "Invalid invocation";

    private ExpressionExecutionWrapper() {
    }

    public static void execute(Runnable runnable) {
        try {
            runnable.run();
        } catch (RuntimeException e) {
            log.error(ERROR_MESSAGE);
        }
    }

    public static <T> T execute(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            log.error(ERROR_MESSAGE);
        }
        return null;
    }
}
