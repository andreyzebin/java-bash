package io.github.zebin.javabash.frontend;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.MatchingFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class StdoutLimiterFilter extends MatchingFilter {


    AtomicInteger counter = new AtomicInteger();
    String lastCmd;
    int limit = 3;
    String groupFilter;
    Marker bottom = new BasicMarkerFactory().getMarker("bottom");
    String groupingMDC;
    String bottomMsg = "...";

    public void setBottomMsg(String bottomMsg) {
        this.bottomMsg = bottomMsg;
    }

    public void setGroupingMDC(String groupingMDC) {
        this.groupingMDC = groupingMDC;
    }

    public void setGroupFilter(String groupFilter) {
        this.groupFilter = groupFilter;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }


    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {

        if (!isStarted()) {
            return FilterReply.NEUTRAL;
        }
        if (bottom == marker) {
            return FilterReply.NEUTRAL;
        }

        String cmd = MDC.get(groupingMDC);
        if (level == Level.DEBUG) {
            if (cmd != null && cmd.matches(groupFilter)) {
                if (cmd.equals(lastCmd)) {
                    int i = counter.incrementAndGet();

                    if (i == limit + 1) {
                        logger.debug(bottom, bottomMsg, params);
                    }
                    if (i > limit) {
                        return FilterReply.DENY;
                    }
                } else {
                    counter.set(0);
                }
            }
        }
        lastCmd = cmd;

        return FilterReply.NEUTRAL;
    }

    @Override
    public void start() {
        int errorCount = 0;
        if (groupFilter == null) {
            addError("'groupFilter' parameter is mandatory. Cannot start.");
            errorCount++;
        }
        if (groupingMDC == null) {
            addError("'groupingMDC' parameter is mandatory. Cannot start.");
            errorCount++;
        }

        if (errorCount == 0) {
            super.start();
        }

    }
}