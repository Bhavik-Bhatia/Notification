package com.ab.notification.aop.aspects;

import com.ab.notification.exception.AppException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExceptionAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionAspect.class);

    /**
     * This advice will be executed when any method in the specified package throws an exception
     *
     * @param joinPoint JoinPoint
     * @param ex        Throwable
     */
    @AfterThrowing(pointcut = "execution(* com.ab.notification..*(..))", throwing = "ex")
    public void ExceptionAfterThrowingAdvice(JoinPoint joinPoint, Throwable ex) {
        Object[] objects = joinPoint.getArgs();
        StringBuilder stringBuilder = new StringBuilder();
        String traceId = "";
        for (Object obj : objects) {
            stringBuilder.append(obj).append(", ");
        }
        if (stringBuilder.isEmpty()) {
            stringBuilder.append("No Params");
        }
        if (ex instanceof AppException) {
            traceId = ((AppException) ex).getTraceId();
        }
        LOGGER.error("ExceptionAspect: Exception has been thrown in the class {} and the method is {}, method args: {}, error message is: {}, trace ID is: {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                stringBuilder,
                traceId,
                ex.getMessage(), ex);

        //TODO: Can be used for metrics purpose with spring actuator for critical exceptions, alerts etc.
    }

}
