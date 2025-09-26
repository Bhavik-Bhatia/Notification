package com.ab.notification.resource;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;


/**
 * This interceptor is used to log system resources
 * //TODO 1) Can you use Spring Actuator for this purpose? If yes then how?
 */
@Configuration
public class ResourceInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceInterceptor.class);

    private OperatingSystemMXBean osBean;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        osBean = ManagementFactory.getOperatingSystemMXBean();
        request.setAttribute("startTime", System.currentTimeMillis());
        request.setAttribute("startSysAverageLoad", osBean.getSystemLoadAverage());
        request.setAttribute("startFreeMemory", Runtime.getRuntime().freeMemory());
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        long endTimeMs = System.currentTimeMillis();
        double endSysAverageLoad = osBean.getSystemLoadAverage();
        long endFreeMemory = Runtime.getRuntime().freeMemory();
        long startFreeMemory = (long) request.getAttribute("startFreeMemory");
        double startSysAverageLoad = (double) request.getAttribute("startSysAverageLoad");
        LOGGER.info("Time taken {} By API {} || Average System Load : {}  || Average Memory Used: {} bytes || available process : {}", endTimeMs - (long) request.getAttribute("startTime"), request.getRequestURI(), (startSysAverageLoad + endSysAverageLoad) / 2, (startFreeMemory + endFreeMemory) / 2, osBean.getAvailableProcessors());
    }

}
