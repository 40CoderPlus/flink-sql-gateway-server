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

package com.fortycoderplus.flink.ext.server;

import com.fortycoderplus.flink.ext.SqlGatewayMonitor;
import org.apache.flink.table.gateway.SqlGateway;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class FlinkSqlGatewayEmbeddedServer {
    public static void main(String[] args) {
        SpringApplication.run(FlinkSqlGatewayEmbeddedServer.class, args);
    }

    @Component
    public static class FlinkSqlGatewayStartup implements CommandLineRunner, ApplicationContextAware {

        private final SqlGateway sqlGateway;
        private final SqlGatewayMonitor sqlGatewayMonitor;

        private ApplicationContext ctx;

        public FlinkSqlGatewayStartup(SqlGateway sqlGateway, SqlGatewayMonitor sqlGatewayMonitor) {
            this.sqlGateway = sqlGateway;
            this.sqlGatewayMonitor = sqlGatewayMonitor;
        }

        @Override
        public void run(String... args) {
            try {
                sqlGateway.start();
                sqlGatewayMonitor.monitor(sqlGateway);
            } catch (Throwable ex) {
                ex.printStackTrace();
                ((ConfigurableApplicationContext) ctx).close();
            }
        }

        @Override
        public void setApplicationContext(ApplicationContext ctx) throws BeansException {
            this.ctx = ctx;
        }
    }
}
