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

package com.epam.digital.data.platform.management.langserver.handler;

import com.epam.digital.data.platform.management.langserver.factory.LanguageServerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler implements SubProtocolCapable {

  private final Map<WebSocketSession, RemoteEndpoint> servers = new ConcurrentHashMap<>();

  @Value("${socket.message-size}")
  private int messageSize;

  private final MessageJsonHandler messageJsonHandler;
  private final LanguageServerFactory languageServerFactory;

  @Override
  public void afterConnectionEstablished(@NonNull WebSocketSession session) {
    log.info("Server connection opened");
    session.setTextMessageSizeLimit(messageSize);
    var remoteEndpoint = languageServerFactory.create(session);
    servers.put(session, remoteEndpoint);
  }

  @Override
  public void afterConnectionClosed(@NonNull WebSocketSession session,
      @NonNull CloseStatus status) {
    log.info("Server connection closed: {}", status);
    servers.remove(session);
  }

  @Override
  public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
    var request = message.getPayload();
    log.trace("Input message: {}", request);
    servers.get(session).consume(messageJsonHandler.parseMessage(request));
  }

  @Override
  public void handleTransportError(@NonNull WebSocketSession session, Throwable exception) {
    log.error("Server transport error: {}", exception.getMessage());
  }

  @Override
  @NonNull
  public List<String> getSubProtocols() {
    return List.of("com.epam.digital.data.platform.management.langserver");
  }
}
