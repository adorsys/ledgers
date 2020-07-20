package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.rest.mapper.AuthMapper;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class SecurityExpressionAdapter extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
    protected final MiddlewareAccountManagementService accountService;
    protected final MiddlewarePaymentService paymentService;
    protected final MiddlewareUserManagementService userManagementService;
    protected final AuthMapper authMapper;

    private Object filterObject;
    private Object returnObject;
    private Object target;

    public SecurityExpressionAdapter(Authentication authentication, MiddlewareAccountManagementService accountService, MiddlewarePaymentService paymentService, MiddlewareUserManagementService userManagementService, AuthMapper authMapper) {
        super(authentication);
        this.accountService = accountService;
        this.paymentService = paymentService;
        this.userManagementService = userManagementService;
        this.authMapper = authMapper;
    }

    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return returnObject;
    }

    /**
     * Sets the "this" property for use in expressions. Typically this will be the "this"
     * property of the {@code JoinPoint} representing the method invocation which is being
     * protected.
     *
     * @param target the target object on which the method in is being invoked.
     */
    void setThis(Object target) {
        this.target = target;
    }

    @Override
    public Object getThis() {
        return target;
    }
}
