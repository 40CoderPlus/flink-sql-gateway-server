/*
 * (c) Copyright 2023 40CoderPlus. All rights reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fortycoderplus.flink.ext.sqlgateway.server;

import static org.apache.flink.util.TimeUtils.formatWithHighestUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "sql-gateway")
public class SqlGatewayProperties {

    // for Flink SQL Gateway
    private Session session = new Session();
    private Worker worker = new Worker();
    private Endpoint endpoint = new Endpoint();

    // for Flink SQL Gateway Monitor
    private Monitor monitor = new Monitor();

    @Data
    public static class Session {

        private Duration checkInterval = Duration.of(1, ChronoUnit.MINUTES);
        private Duration idleTimeout = Duration.of(10, ChronoUnit.MINUTES);
        private int maxNum = 1000000;
    }

    @Data
    public static class Worker {

        private Duration keepaliveTime = Duration.of(5, ChronoUnit.MINUTES);
        private Threads threads = new Threads();
    }

    @Data
    public static class Threads {

        private int max = 500;
        private int min = 5;
    }

    @Data
    public static class Endpoint {

        private Rest rest = new Rest();
    }

    @Data
    public static class Rest {
        private String address = "127.0.0.1";
        private String bindAddress;
        private int port = 8083;
        private String bindPort = "8083";
    }

    @Data
    public static class Monitor {

        private int delay = 0;
        private int period = 10;
        private TimeUnit timeUnit = TimeUnit.SECONDS;
    }

    public Properties dynamicConfig() {
        Properties dynamicConfig = new Properties();
        // rest
        dynamicConfig.setProperty(
                "sql-gateway.endpoint.rest.address",
                this.getEndpoint().getRest().getAddress());
        dynamicConfig.setProperty(
                "sql-gateway.endpoint.rest.port",
                String.valueOf(this.getEndpoint().getRest().getPort()));
        dynamicConfig.setProperty(
                "sql-gateway.endpoint.rest.bind-port",
                this.getEndpoint().getRest().getBindPort());
        if (Objects.nonNull(this.getEndpoint().getRest().getBindAddress())) {
            dynamicConfig.setProperty(
                    "sql-gateway.endpoint.rest.bind-address",
                    this.getEndpoint().getRest().getBindAddress());
        }

        // session
        dynamicConfig.setProperty(
                "sql-gateway.session.check-interval",
                formatWithHighestUnit(this.getSession().getCheckInterval()));
        dynamicConfig.setProperty(
                "sql-gateway.session.idle-timeout",
                formatWithHighestUnit(this.getSession().getIdleTimeout()));
        dynamicConfig.setProperty(
                "sql-gateway.session.max-num", String.valueOf(this.getSession().getMaxNum()));

        // worker
        dynamicConfig.setProperty(
                "sql-gateway.worker.keepalive-time",
                formatWithHighestUnit(this.getWorker().getKeepaliveTime()));
        dynamicConfig.setProperty(
                "sql-gateway.worker.threads.max",
                String.valueOf(this.getWorker().getThreads().getMax()));
        dynamicConfig.setProperty(
                "sql-gateway.worker.threads.min",
                String.valueOf(this.getWorker().getThreads().getMin()));
        return dynamicConfig;
    }
}
