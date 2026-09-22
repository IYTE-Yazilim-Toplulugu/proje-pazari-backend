package com.iyte_yazilim.proje_pazari.application.behaviors;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.common.RequestHandlerDelegate;
import com.iyte_yazilim.proje_pazari.application.common.SensitiveRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Pipeline behavior that logs request handling.
 *
 * <p>Logs the start and completion of each request, including execution time and any errors.
 */
@Component
@Order(1)
public class LoggingBehavior<TRequest extends IRequest<TResponse>, TResponse>
        implements IPipelineBehavior<TRequest, TResponse> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingBehavior.class);

    @Override
    public TResponse handle(TRequest request, RequestHandlerDelegate<TResponse> next) {
        String requestName = request.getClass().getSimpleName();
        logger.info("Handling request: {}", requestName);

        long startTime = System.currentTimeMillis();

        try {
            TResponse response = next.handle();
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Request {} completed in {}ms", requestName, duration);
            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            if (request instanceof SensitiveRequest) {
                logger.error(
                        "Sensitive request {} failed after {}ms ({})",
                        requestName,
                        duration,
                        e.getClass().getSimpleName());
            } else {
                logger.error(
                        "Request {} failed after {}ms: {}", requestName, duration, e.getMessage());
            }
            throw e;
        }
    }
}
