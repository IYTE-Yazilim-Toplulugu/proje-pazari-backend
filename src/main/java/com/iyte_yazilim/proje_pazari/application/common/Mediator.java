package com.iyte_yazilim.proje_pazari.application.common;

import com.iyte_yazilim.proje_pazari.application.behaviors.IPipelineBehavior;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;

/**
 * Mediator implementation that discovers handlers and executes pipeline behaviors.
 *
 * <p>This implementation uses Spring's ApplicationContext to discover handlers at runtime and
 * supports pipeline behaviors for cross-cutting concerns like logging, validation, and
 * transactions.
 */
@Service
public class Mediator implements IMediator {

    private final ApplicationContext context;
    private final List<IPipelineBehavior<?, ?>> behaviors;

    public Mediator(ApplicationContext context, List<IPipelineBehavior<?, ?>> behaviors) {
        this.context = context;
        this.behaviors = behaviors != null ? behaviors : new ArrayList<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TResponse> TResponse send(IRequest<TResponse> request) {
        Class<?> requestClass = request.getClass();

        IRequestHandler<IRequest<TResponse>, TResponse> handler =
                (IRequestHandler<IRequest<TResponse>, TResponse>) getHandler(requestClass);

        if (handler == null) {
            throw new IllegalStateException(
                    "No handler found for request: " + requestClass.getName());
        }

        return executePipeline(request, handler);
    }

    @SuppressWarnings("rawtypes")
    private IRequestHandler<?, ?> getHandler(Class<?> requestClass) {
        Map<String, IRequestHandler> handlers = context.getBeansOfType(IRequestHandler.class);

        for (IRequestHandler<?, ?> handler : handlers.values()) {
            ResolvableType handlerType =
                    ResolvableType.forClass(handler.getClass()).as(IRequestHandler.class);

            ResolvableType requestType = handlerType.getGeneric(0);

            if (requestType.resolve() == requestClass) {
                return handler;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private <TRequest extends IRequest<TResponse>, TResponse> TResponse executePipeline(
            TRequest request, IRequestHandler<TRequest, TResponse> handler) {

        if (behaviors.isEmpty()) {
            return handler.handle(request);
        }

        RequestHandlerDelegate<TResponse> handlerDelegate = () -> handler.handle(request);

        for (int i = behaviors.size() - 1; i >= 0; i--) {
            IPipelineBehavior<TRequest, TResponse> behavior =
                    (IPipelineBehavior<TRequest, TResponse>) behaviors.get(i);
            RequestHandlerDelegate<TResponse> next = handlerDelegate;
            handlerDelegate = () -> behavior.handle(request, next);
        }

        return handlerDelegate.handle();
    }
}
