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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.apache.flink.table.gateway.SqlGateway;
import org.apache.flink.table.gateway.api.operation.OperationHandle;
import org.apache.flink.table.gateway.api.session.SessionHandle;
import org.apache.flink.table.gateway.service.operation.OperationManager;
import org.apache.flink.table.gateway.service.session.Session;
import org.joor.Reflect;
import org.springframework.context.ApplicationEventPublisher;

public class SqlGatewayMonitor {

    private static final String FILED_SESSION_MANAGER = "sessionManager";
    private static final String FILED_SESSIONS = "sessions";
    private static final String FILED_SUBMITTED_OPERATIONS = "submittedOperations";

    private final SqlGatewayProperties sqlGatewayProperties;
    private final ApplicationEventPublisher publisher;
    private Map<SqlGatewaySession, List<SqlGatewayOperation>> before = new HashMap<>();

    public SqlGatewayMonitor(SqlGatewayProperties sqlGatewayProperties, ApplicationEventPublisher publisher) {
        this.sqlGatewayProperties = sqlGatewayProperties;
        this.publisher = publisher;
    }

    public void monitor(SqlGateway sqlGateway) {
        Executors.newScheduledThreadPool(1)
                .scheduleAtFixedRate(
                        () -> {
                            Map<SessionHandle, Session> sessions = Reflect.on(sqlGateway)
                                    .field(FILED_SESSION_MANAGER)
                                    .get(FILED_SESSIONS);
                            publisher.publishEvent(
                                    new SqlGatewayEvent(computeChanges(collectOperations(sessions)), this));
                        },
                        sqlGatewayProperties.getMonitor().getDelay(),
                        sqlGatewayProperties.getMonitor().getPeriod(),
                        sqlGatewayProperties.getMonitor().getTimeUnit());
    }

    private Map<SqlGatewaySession, List<SqlGatewayOperation>> collectOperations(Map<SessionHandle, Session> sessions) {
        return sessions.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> SqlGatewaySession.builder()
                                .id(entry.getKey().getIdentifier())
                                .build(),
                        entry -> {
                            Map<OperationHandle, OperationManager.Operation> operations = Reflect.on(
                                            entry.getValue().getOperationManager())
                                    .get(FILED_SUBMITTED_OPERATIONS);
                            return operations.entrySet().stream()
                                    .map(op -> {
                                        SqlGatewayOperation sgo = SqlGatewayOperation.builder()
                                                .id(op.getKey().getIdentifier())
                                                .status(op.getValue()
                                                        .getOperationInfo()
                                                        .getStatus()
                                                        .name())
                                                .build();
                                        op.getValue()
                                                .getOperationInfo()
                                                .getException()
                                                .ifPresent(e -> sgo.setError(e.getMessage() + " cause by:"
                                                        + e.getCause().getMessage()));
                                        return sgo;
                                    })
                                    .collect(Collectors.toList());
                        }));
    }

    private Map<SqlGatewaySession, List<SqlGatewayOperation>> computeChanges(
            Map<SqlGatewaySession, List<SqlGatewayOperation>> current) {
        if (before.isEmpty()) {
            before = current;
            return before;
        }

        // first: add new sessions
        Map<SqlGatewaySession, List<SqlGatewayOperation>> changed = current.entrySet().stream()
                .filter(entry -> !before.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // second: add new operations
        Map<SqlGatewaySession, List<SqlGatewayOperation>> candidate = current.entrySet().stream()
                .filter(entry -> before.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        changed.putAll(candidate.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            entry.getValue().removeAll(before.get(entry.getKey()));
            return entry.getValue();
        })));

        before = current;
        return changed;
    }
}
