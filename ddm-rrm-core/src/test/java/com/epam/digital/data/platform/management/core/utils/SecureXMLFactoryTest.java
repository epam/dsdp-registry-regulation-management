/*
 * Copyright 2026 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.management.core.utils;

import static org.assertj.core.api.Assertions.assertThat;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerConfigurationException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

class SecureXMLFactoryTest {

  @Test
  void shouldCreateSchemaFactoryWithSecureProcessing() throws SAXException {
    var factory = SecureXMLFactory.schemaFactory();

    assertThat(factory).isNotNull();
    assertThat(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING)).isTrue();
  }

  @Test
  void shouldCreateTransformerWithSecureProcessing() throws TransformerConfigurationException {
    var transformer = SecureXMLFactory.transformer();

    assertThat(transformer).isNotNull();
    assertThat(transformer.getOutputProperties()).isNotNull();
  }

  @Test
  void shouldCreateValidatorWithSecureProcessing() throws SAXException {
    var schemaFactory = SecureXMLFactory.schemaFactory();
    var validator = SecureXMLFactory.validator(schemaFactory.newSchema());

    assertThat(validator).isNotNull();
    assertThat(validator.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING)).isTrue();
  }
}