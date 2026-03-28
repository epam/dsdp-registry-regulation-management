/*
 * Copyright 2022 EPAM Systems.
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

package com.epam.digital.data.platform.management.langserver.consumer;

import com.epam.digital.data.platform.management.langserver.exception.WebSocketConnectionException;
import com.epam.digital.data.platform.management.langserver.exception.WebSocketException;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@ExtendWith(SpringExtension.class)
public class LSMessageConsumerTest {
  @Mock
  WebSocketSession session;
  @Mock
  MessageJsonHandler jsonHandler;

  @InjectMocks
  LSMessageConsumer consumer;

  @SneakyThrows
  @Test
  void consumeTest() {
    var message = new RequestMessage();
    final String jsonMessage = String.valueOf(new RequestMessage());
    Mockito.when(jsonHandler.serialize(message))
        .thenReturn(jsonMessage);
    Mockito.when(session.isOpen()).thenReturn(true);
    consumer.consume(message);
    Mockito.verify(session).sendMessage(new TextMessage(jsonMessage));
  }

  @SneakyThrows
  @Test
  void consumeSessionClosedTest() {
    var message = new RequestMessage();
    final String jsonMessage = String.valueOf(new RequestMessage());
    Mockito.when(jsonHandler.serialize(message))
        .thenReturn(jsonMessage);
    Mockito.when(session.isOpen()).thenReturn(false);

    Assertions.assertThatCode(() -> consumer.consume(message))
        .isInstanceOf(WebSocketConnectionException.class)
        .hasMessage("Session is closed");
  }

  @SneakyThrows
  @Test
  void consumeIoExceptionTest() {
    var message = new RequestMessage();
    final String jsonMessage = String.valueOf(new RequestMessage());
    Mockito.when(jsonHandler.serialize(message))
        .thenReturn(jsonMessage);
    Mockito.when(session.isOpen()).thenReturn(true);
    Mockito.doThrow(IOException.class).when(session).sendMessage(new TextMessage(jsonMessage));

    Assertions.assertThatCode(() -> consumer.consume(message))
        .isInstanceOf(WebSocketException.class)
        .hasMessage("Exception occurs during sending message");
  }

}
