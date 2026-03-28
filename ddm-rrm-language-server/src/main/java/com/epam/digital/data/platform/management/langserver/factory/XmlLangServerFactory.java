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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.lemminx.XMLLanguageServer;
import org.eclipse.lemminx.customservice.XMLLanguageClientAPI;
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.stream.Stream;


@RequiredArgsConstructor
@Component
public class XmlLangServerFactory implements LanguageServerFactory {

  private final MessageJsonHandler xmlMessageJsonHandler;

  @Override
  public RemoteEndpoint create(@NonNull WebSocketSession session) {
    var xmlLanguageServer = new XMLLanguageServer();
    var remoteEndpoint = new RemoteEndpoint(new LSMessageConsumer(session, xmlMessageJsonHandler),
        ServiceEndpoints.toEndpoint(xmlLanguageServer));
    var languageClient = ServiceEndpoints.toServiceObject(remoteEndpoint,
        XMLLanguageClientAPI.class);
    xmlLanguageServer.setClient(languageClient);
    initializeXmlLanguageService(xmlLanguageServer);
    return remoteEndpoint;
  }


  /*
  Initializes XMLLanguageService from input XMLLanguageServer in Tomcat thread with Spring
  classloader before websocket message initializing.
  WS initialize message cannot initialize server properly as it's executing in ForkJoinPool thread
  which uses system classloader that can't load org.eclipse.lemminx.services.extensions.IXMLExtension
  interface and its inheritors.

  But every initializing registers a bunch of participants (that are used in XML file analyzing)
  without duplicate checking, so this method clears all participants from the service after
  initializing. Only list of languageService.getExtensions() remains untouched as it used in
  WS message initializing for participants registration.
   */
  private static void initializeXmlLanguageService(XMLLanguageServer xmlLanguageServer) {
    var languageService = xmlLanguageServer.getXMLLanguageService();
    languageService.initializeIfNeeded();
    Stream.of(
        languageService.getCompletionParticipants(),
        languageService.getHoverParticipants(),
        languageService.getDiagnosticsParticipants(),
        languageService.getCodeActionsParticipants(),
        languageService.getDocumentLinkParticipants(),
        languageService.getDefinitionParticipants(),
        languageService.getTypeDefinitionParticipants(),
        languageService.getReferenceParticipants(),
        languageService.getCodeLensParticipants(),
        languageService.getHighlightingParticipants(),
        languageService.getRenameParticipants(),
        languageService.getFormatterParticipants(),
        languageService.getSymbolsProviderParticipants(),
        languageService.getWorkspaceServiceParticipants(),
        languageService.getDocumentLifecycleParticipants()
    ).forEach(Collection::clear);
  }
}
