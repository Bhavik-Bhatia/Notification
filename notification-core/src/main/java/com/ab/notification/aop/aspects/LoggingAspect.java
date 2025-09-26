package com.ab.notification.aop.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * This advice will be executed around the methods annotated with @Log
     *
     * @param proceedingJoinPoint ProceedingJoinPoint
     * @return Object
     * @throws Throwable if any error occurs
     */
    @Around("@annotation(com.ab.notification.annotation.Log)")
    public Object aroundLoggingAdvice(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] objects = proceedingJoinPoint.getArgs();
        StringBuilder args = new StringBuilder();
        for (Object object : objects) {
            args.append(object).append(", ");
        }
        LOGGER.debug("Enter in {}, params {}", proceedingJoinPoint.getSignature().getName(), !args.isEmpty() ? args : "No Params");
        long startMs = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        long endMs = System.currentTimeMillis();
        LOGGER.debug("Exit from {}, total time taken {} ms", proceedingJoinPoint.getSignature().getName(), (endMs - startMs));
        return result;
    }
}
