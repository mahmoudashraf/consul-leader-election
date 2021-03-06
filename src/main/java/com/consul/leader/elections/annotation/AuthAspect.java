package com.consul.leader.elections.annotation;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
public class AuthAspect {

    @Autowired
    OnLeaderCheckImpl onleaderCheck;

    private static final Logger logger = LoggerFactory.getLogger(AuthAspect.class);


    @Before("@annotation(com.consul.leader.elections.annotation.OnLeader)")
    public void beforeLeader() throws UnsupportedOperationOnServentMode {
        logger.trace("Before method announated (OnLader)");
        if (!onleaderCheck.onLeaderModeCheck()) {
            throw new UnsupportedOperationOnServentMode();
        }
    }

    @Before("@annotation(com.consul.leader.elections.annotation.OnServent)")
    public void beforeServent() throws UnsupportedOperationOnLeaderMode {
        logger.trace("Before method announated (OnServent)");
        if (onleaderCheck.onLeaderModeCheck()) {
            throw new UnsupportedOperationOnLeaderMode();
        }

    }

}
