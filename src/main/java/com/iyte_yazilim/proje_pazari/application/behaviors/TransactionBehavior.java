package com.iyte_yazilim.proje_pazari.application.behaviors;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.common.RequestHandlerDelegate;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Pipeline behavior that wraps command handling in a transaction.
 *
 * <p>Commands (requests implementing ICommand) are automatically executed within a transaction.
 * Queries are not wrapped in transactions.
 */
@Component
@Order(3)
public class TransactionBehavior<TRequest extends IRequest<TResponse>, TResponse>
        implements IPipelineBehavior<TRequest, TResponse> {

    private final TransactionTemplate transactionTemplate;

    public TransactionBehavior(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public TResponse handle(TRequest request, RequestHandlerDelegate<TResponse> next) {
        if (isCommand(request)) {
            return executeInTransaction(next);
        }
        return next.handle();
    }

    private boolean isCommand(TRequest request) {
        return request instanceof ICommand;
    }

    private TResponse executeInTransaction(RequestHandlerDelegate<TResponse> next) {
        return transactionTemplate.execute(status -> next.handle());
    }
}
