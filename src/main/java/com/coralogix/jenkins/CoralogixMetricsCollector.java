package com.coralogix.jenkins;

import com.coralogix.jenkins.model.Log;
import com.coralogix.jenkins.utils.CoralogixAPI;
import hudson.Extension;
import hudson.util.Secret;
import hudson.model.PeriodicWork;
import jenkins.metrics.api.Metrics;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Jenkins metrics collector definition
 *
 * @author Eldar Aliiev
 * @version 1.1.5
 * @since 2020-11-03
 */
@Extension
public class CoralogixMetricsCollector extends PeriodicWork{

    /**
     * Metrics collector logger
     */
    private static final Logger logger = Logger.getLogger(CoralogixMetricsCollector.class.getName());

    /**
     * Metrics collector run delay
     */
    @Override
    public long getInitialDelay() {
        return TimeUnit.SECONDS.toMillis(30);
    }

    /**
     * Metrics collector run interval
     */
    @Override
    public long getRecurrencePeriod() {
        return TimeUnit.SECONDS.toMillis(CoralogixConfiguration.get().getMetricsInterval());
    }

    /**
     * Metrics collector runner
     */
    @Override
    protected synchronized void doRun() {
        if (CoralogixConfiguration.get().getMetricsEnabled()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper().registerModule(new MetricsModule(TimeUnit.MINUTES, TimeUnit.SECONDS, true));
                JsonNode jsonNode = objectMapper.convertValue(Metrics.metricRegistry(), JsonNode.class);

                ((ObjectNode)jsonNode).remove("histograms");

                List<Log> logEntries = new ArrayList<>();
                logEntries.add(new Log(
                    3,
                    objectMapper.writeValueAsString(jsonNode),
                    "metrics",
                    "",
                    "",
                    ""
                ));
                CoralogixAPI.sendLogs(
                    Secret.toString(CoralogixConfiguration.get().getPrivateKey()),
                    CoralogixConfiguration.get().getJenkinsName(),
                    "metrics",
                    logEntries
                );
            } catch (Exception e) {
                logger.log(Level.WARNING, "Cannot send build logs to Coralogix!");
            }
        }
    }
}
