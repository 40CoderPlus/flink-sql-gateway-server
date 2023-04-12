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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.flink.table.gateway.api.operation.OperationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SqlGatewayChangeComputerTest {

    SqlGatewayChangeComputer computer;

    // id(s)
    UUID id1;
    UUID id2;
    UUID id3;
    UUID id4;

    // sessions
    SqlGatewaySession session1;
    SqlGatewaySession session2;

    @BeforeEach
    void setUp() {
        computer = new SqlGatewayChangeComputer();

        id1 = UUID.randomUUID();
        id2 = UUID.randomUUID();
        id3 = UUID.randomUUID();

        session1 = SqlGatewaySession.builder().id(id1).build();
        session2 = SqlGatewaySession.builder().id(id2).build();
    }

    @Test
    void compute() {
        assertEquals(0, computer.compute(Map.of()).size());

        Map<SqlGatewaySession, List<SqlGatewayOperation>> first = new HashMap<>();
        first.put(session1, new ArrayList<>());
        assertEquals(1, computer.compute(first).size());
        assertTrue(computer.compute(first).isEmpty());

        Map<SqlGatewaySession, List<SqlGatewayOperation>> second = new HashMap<>();
        second.put(session1, new ArrayList<>() {
            {
                add(SqlGatewayOperation.builder()
                        .id(id3)
                        .status(OperationStatus.INITIALIZED.name())
                        .build());
            }
        });

        Map<SqlGatewaySession, List<SqlGatewayOperation>> secondChanges = computer.compute(second);
        assertEquals(1, secondChanges.size());
        assertEquals(1, secondChanges.get(session1).size());
        assertTrue(computer.compute(second).isEmpty());

        Map<SqlGatewaySession, List<SqlGatewayOperation>> third = new HashMap<>();
        third.put(session1, new ArrayList<>() {
            {
                add(SqlGatewayOperation.builder()
                        .id(id3)
                        .status(OperationStatus.RUNNING.name())
                        .build());
            }
        });

        Map<SqlGatewaySession, List<SqlGatewayOperation>> thirdChanges = computer.compute(third);
        assertEquals(1, thirdChanges.size());
        assertEquals(1, thirdChanges.get(session1).size());
        assertTrue(computer.compute(third).isEmpty());

        Map<SqlGatewaySession, List<SqlGatewayOperation>> fouth = new HashMap<>();
        fouth.put(session1, new ArrayList<>() {
            {
                add(SqlGatewayOperation.builder()
                        .id(id3)
                        .status(OperationStatus.FINISHED.name())
                        .build());
                add(SqlGatewayOperation.builder()
                        .id(id4)
                        .status(OperationStatus.INITIALIZED.name())
                        .build());
            }
        });

        Map<SqlGatewaySession, List<SqlGatewayOperation>> fouthChanges = computer.compute(fouth);
        assertEquals(1, fouthChanges.size());
        assertEquals(2, fouthChanges.get(session1).size());
        assertTrue(computer.compute(fouth).isEmpty());

        assertTrue(computer.compute(new HashMap<>()).isEmpty());
    }
}
