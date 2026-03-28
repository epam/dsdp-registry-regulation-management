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

package com.epam.digital.data.platform.management.langserver.factory;

import com.epam.digital.data.platform.management.langserver.consumer.LSMessageConsumer;
import lombok.RequiredArgsConstructor;
import net.prominic.groovyls.GroovyLanguageServer;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.eclipse.lsp4j.services.LanguageClient;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class GroovyLangServerFactory implements LanguageServerFactory {

  private final MessageJsonHandler groovyMessageJsonHandler;

  @Override
  public RemoteEndpoint create(@NonNull WebSocketSession session) {
    var groovyLanguageServer = new GroovyLanguageServer();
    var remoteEndpoint = new RemoteEndpoint(new LSMessageConsumer(session, groovyMessageJsonHandler),
        ServiceEndpoints.toEndpoint(groovyLanguageServer));
    var languageClient = ServiceEndpoints.toServiceObject(remoteEndpoint, LanguageClient.class);
    groovyLanguageServer.connect(languageClient);
    return remoteEndpoint;
  }
}
