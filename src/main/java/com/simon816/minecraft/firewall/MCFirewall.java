package com.simon816.minecraft.firewall;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MCFirewall {

    private final Logger logger;
    private final Path configFile;
    final MutableRuleFilter filter;

    public MCFirewall(Logger logger, Path configFile) {
        this.logger = logger;
        this.configFile = configFile;
        this.filter = new MutableRuleFilter(logger);
    }

    public void load() {
        List<IpFilterRule> rules = readConfigFile();
        this.filter.setRules(rules);
    }

    public MutableRuleFilter getFilter() {
        return this.filter;
    }

    public ChannelHandler connectionIntercepter() {
        // Found out that the server pipeline initially contains ServerBootstrapAcceptor
        // This is given the child channel in channelRead. We need to intercept that channel
        // and add our handler
        return new ChannelInboundHandlerAdapter() {

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                Channel child = (Channel) msg;
                child.pipeline().addFirst(MCFirewall.this.filter);
                ctx.fireChannelRead(msg);
            }
        };
    }

    private List<IpFilterRule> readConfigFile() {
        List<IpFilterRule> rules = new ArrayList<>();
        List<String> lines;
        try {
            Files.createFile(this.configFile);
            this.logger.info("Created empty firewall.txt");
            lines = Collections.emptyList();
        } catch (FileAlreadyExistsException ignored) {
            try {
                lines = Files.readAllLines(this.configFile);
                this.logger.info("Reading firewall.txt");
            } catch (IOException e) {
                this.logger.error("Failed to read firewall.txt, no rules loaded!", e);
                return rules;
            }
        } catch (IOException e) {
            this.logger.error("Failed to create firewall.txt", e);
            return rules;
        }
        int lineno = 0;
        for (String line : lines) {
            lineno++;
            line = line.trim();
            if (line.isEmpty() || line.charAt(0) == '#') {
                continue;
            }
            String[] split = line.split("\\s+", 2);
            if (split.length != 2) {
                this.logger.warn("Malformed firewall entry on line {}: {}", lineno, line);
                continue;
            }
            String action = split[1].toUpperCase(Locale.ENGLISH);
            IpFilterRuleType ruleType;
            switch (action) {
                case "ALLOW":
                case "ACCEPT":
                    ruleType = IpFilterRuleType.ACCEPT;
                    break;
                case "BLOCK":
                case "REJECT":
                    ruleType = IpFilterRuleType.REJECT;
                    break;
                default:
                    this.logger.warn("Invalid action on line {}: {}", lineno, action);
                    continue;
            }
            String[] ipsplit = split[0].split("/", 2);
            if (ipsplit.length != 2) {
                this.logger.warn("Invalid IP range specification on line {}: {}", lineno, split[0]);
                continue;
            }
            int cidrLength;
            try {
                cidrLength = Integer.parseInt(ipsplit[1]);
            } catch (NumberFormatException e) {
                this.logger.warn("Invalid CIDR number on line {}: {}", lineno, ipsplit[1]);
                continue;
            }
            IpFilterRule rule;
            try {
                rule = new IpSubnetFilterRule(ipsplit[0], cidrLength, ruleType);
            } catch (IllegalArgumentException e) {
                this.logger.warn("Invalid IP range specification on line {}: {}", lineno, ipsplit[0], e);
                continue;
            }
            rules.add(rule);
        }
        this.logger.info("Loaded {} firewall rules", rules.size());
        return rules;
    }

}
