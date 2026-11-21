package com.example.sis2.common.aop;

import org.aspectj.lang.annotation.Pointcut;

public final class Pointcuts {

    private Pointcuts() {
    }

    @Pointcut("execution(* com.example.sis2..app..*(..))")
    public void serviceLayer() {
    }
}
