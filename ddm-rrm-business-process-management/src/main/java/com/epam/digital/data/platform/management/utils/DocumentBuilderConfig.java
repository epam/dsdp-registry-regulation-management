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

package com.epam.digital.data.platform.management.utils;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DocumentBuilderConfig {

  private static final String DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";
  private static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
  private static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
  private static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

  public static DocumentBuilder documentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    documentBuilderFactory.setFeature(DISALLOW_DOCTYPE_DECL, true);
    documentBuilderFactory.setFeature(EXTERNAL_GENERAL_ENTITIES, false);
    documentBuilderFactory.setFeature(EXTERNAL_PARAMETER_ENTITIES, false);
    documentBuilderFactory.setFeature(LOAD_EXTERNAL_DTD, false);
    documentBuilderFactory.setXIncludeAware(false);
    documentBuilderFactory.setNamespaceAware(true);
    documentBuilderFactory.setExpandEntityReferences(false);
    return documentBuilderFactory.newDocumentBuilder();
  }
}
