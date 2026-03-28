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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageIssueException;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class LSMessageConsumer implements MessageConsumer {

  private final WebSocketSession session;
  private final MessageJsonHandler messageJsonHandler;

  @Override
  public void consume(Message message) throws MessageIssueException, JsonRpcException {
    var json = messageJsonHandler.serialize(message);
    log.trace("Output message: {}", json);
    try {
      if (session.isOpen()) {
        session.sendMessage(new TextMessage(json));
      } else {
        log.warn("Session is closed");
        throw new WebSocketConnectionException("Session is closed");
      }
    } catch (IOException e) {
      log.error("Exception occurs during sending message");
      throw new WebSocketException("Exception occurs during sending message", e);
    }
  }
}
