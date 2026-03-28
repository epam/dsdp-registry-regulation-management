/*
 * Copyright 2024 EPAM Systems.
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

package com.epam.digital.data.platform.management.reportexporter.config;

import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.HttpURLConnection;

import static org.assertj.core.api.Assertions.assertThat;

@EnableKubernetesMockClient
@ExtendWith(SpringExtension.class)
class ReportExporterOpenShiftClientTest {

  private ReportExporterOpenShiftClient openShiftClient;

  private KubernetesMockServer server;

  @BeforeEach
  void beforeEach() {
    this.openShiftClient = new ReportExporterOpenShiftClient(server.createClient().getConfiguration());
  }

  @Test
  void expectAdminApiKeyIsRetrievedFromSecret() {
    // Given
    server
        .expect()
        .withPath("/api/v1/namespaces/test/secrets/redash-api-keys")
        .andReturn(
            HttpURLConnection.HTTP_OK,
            new SecretBuilder()
                .withNewMetadata()
                .withName("redash-api-keys")
                .endMetadata()
                .addToData("admin-api-key", "MTIzNA==")
                .build())
        .always();

    // When
    var apiKey = openShiftClient.getApiKey();

    // Then
    assertThat(apiKey).isEqualTo("1234");
  }
}
