/*
 * (c) Copyright 2017-2022 40CoderPlus. All rights reserved.
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

package com.fortycoderplus.flink.ext;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "sql-gateway")
public class SqlGatewayProperties {

    private Session session = new Session();
    private Worker worker = new Worker();
    private Endpoint endpoint = new Endpoint();

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
}
