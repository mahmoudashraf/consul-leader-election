package pl.piomin.services.customer.leader.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.piomin.services.customer.leader.LeaderObserver;

@Component
public class OnLeaderCheckImpl {

    private static final Logger logger = LoggerFactory.getLogger(OnLeaderCheckImpl.class);

    @Autowired
    private LeaderObserver observer;

    public boolean onLeaderModeCheck() {
        if (observer.isGrantedLeader()) {
            logger.info("I'm leader Leader");
            return true;
        }
        logger.info("It's Not Leader");
        return false;
    }
}