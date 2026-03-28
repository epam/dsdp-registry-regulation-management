/*
 * Copyright 2023 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.management;

import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.ArrayList;

import static com.epam.digital.data.platform.management.util.LanguageServerTestUtils.sendMessage;
import static org.mockito.ArgumentMatchers.any;

class LangServerGroovyIT extends BaseIT {

  @Test
  @SneakyThrows
  void hasDiagnosticMessagesTest() {
    var resourceContent = context.getResourceContent("/langserver/groovy/groovy-request-incorrect.json");
    var webSocketClient = new StandardWebSocketClient();
    var errors = new ArrayList<>();

    var webSocketSessionListenableFuture = webSocketClient.execute(
        new TextWebSocketHandler() {
          @Override
          protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            String diagnosticMessage = JsonPath.read(message.getPayload(),
                "$.params.diagnostics[0].message");
            if (diagnosticMessage == null || diagnosticMessage
                .contains("Unexpected character: '\\' @ line 1, column 19.")) {
              errors.add("test failed");
            }
          }
        },
        new WebSocketHttpHeaders(),
        URI.create("ws://localhost:" + port + "/groovy"));

    var webSocketSession = webSocketSessionListenableFuture.get();

    sendMessage(resourceContent, webSocketSession);

    Assertions.assertThat(errors).isNotEmpty();
  }

  @Test
  @SneakyThrows
  void emptyDiagnosticMessagesTest() {
    var resourceContent = context.getResourceContent("/langserver/groovy/groovy-request-correct.json");
    var webSocketClient = new StandardWebSocketClient();

    var mock = Mockito.mock(TextWebSocketHandler.class);
    var webSocketSessionListenableFuture = webSocketClient.execute(
        mock,
        new WebSocketHttpHeaders(),
        URI.create("ws://localhost:" + port + "/groovy"));

    var webSocketSession = webSocketSessionListenableFuture.get();

    sendMessage(resourceContent, webSocketSession);
    Mockito.verify(mock, Mockito.never()).handleMessage(any(), any());
  }
}
