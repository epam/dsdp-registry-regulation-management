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

package com.epam.digital.data.platform.management.langserver.config;

import net.prominic.groovyls.GroovyLanguageServer;
import org.eclipse.lemminx.XMLLanguageServer;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageHandlerConfig {

  @Bean
  public MessageJsonHandler groovyMessageJsonHandler() {
    return new MessageJsonHandler(ServiceEndpoints.getSupportedMethods(GroovyLanguageServer.class));
  }

  @Bean
  public MessageJsonHandler xmlMessageJsonHandler() {
    return new MessageJsonHandler(ServiceEndpoints.getSupportedMethods(XMLLanguageServer.class));
  }
}
