package com.simon816.minecraft.firewall;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ipfilter.AbstractRemoteAddressFilter;
import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;
import net.minecraft.network.NetworkManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

// Copied from io.netty.handler.ipfilter.RuleBasedIpFilter but allowing rules to be changed
@Sharable
public class MutableRuleFilter extends AbstractRemoteAddressFilter<InetSocketAddress> {

    // If more than 5 messages are logged each less than 2 seconds apart,
    // suppress output for 10 seconds
    private static final long RAPID_THRESHOLD = TimeUnit.SECONDS.toNanos(2);
    private static final int RAPID_COUNT_THRESHOLD = 5;
    private static final long COOLDOWN_PERIOD = TimeUnit.SECONDS.toNanos(10);

    private final Logger logger;

    private IpFilterRule[] rules;
    private long lastConn;
    private int limitCount;
    private long cooldown;

    public MutableRuleFilter(Logger logger) {
        this.logger = logger;
    }

    public List<IpFilterRule> getRules() {
        return Arrays.asList(this.rules);
    }

    public void setRules(List<IpFilterRule> rules) {
        this.rules = rules.toArray(new IpFilterRule[0]);
    }

    @Override
    protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) throws Exception {
        // local rules in case setRules replaces it
        IpFilterRule[] rules = this.rules;
        boolean accept = true;
        if (rules != null) {
            for (IpFilterRule rule : rules) {
                if (rule.matches(remoteAddress)) {
                    accept = rule.ruleType() == IpFilterRuleType.ACCEPT;
                    break;
                }
            }
        }
        long now = System.nanoTime();
        if (this.cooldown == 0) {
            if (now - this.lastConn < RAPID_THRESHOLD) {
                this.limitCount++;
            } else {
                this.limitCount = 0;
            }
            if (this.limitCount > RAPID_COUNT_THRESHOLD) {
                this.cooldown = now + COOLDOWN_PERIOD;
            }
        } else if (now >= this.cooldown) {
            this.cooldown = 0;
            this.limitCount = 0;
        }
        if (this.cooldown == 0) {
            this.logger.info("Connection from {} {}", remoteAddress, accept ? "accepted" : "rejected");
        }
        this.lastConn = now;
        return accept;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // Dammit Mojang
        // Fix NPE
        NetworkManager networkManager = (NetworkManager) ctx.pipeline().get("packet_handler");
        networkManager.channel = ctx.channel();
    }

}
