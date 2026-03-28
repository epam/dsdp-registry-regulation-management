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

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import lombok.experimental.UtilityClass;
import org.xml.sax.SAXException;

@UtilityClass
public class SecureXMLFactory {

  /** False Positive
   * This rule geneartes false alert even though the code follows its recommendations.
   * There are open issues in SonarQube to address this, they should be tracked periodically to check if they are resolved:
   * - <a href="https://community.sonarsource.com/t/false-positive-rule-java-s2755-disable-access-to-external-entities-in-xml-parsing/143810/2">...</a>
   * - <a href="https://sonarsource.atlassian.net/browse/JAVASE-47">...</a>
   * - <a href="https://community.sonarsource.com/t/possible-false-positive-with-java-s2755/176919">...</a>
   * Sonar flags SchemaFactory.newInstance() as vulnerable to XXE (java:S2755).
   * FEATURE_SECURE_PROCESSING is enabled to mitigate entity expansion (DoS) attacks.
   * ACCESS_EXTERNAL_DTD and ACCESS_EXTERNAL_SCHEMA cannot be restricted because the SchemaFactory
   * must resolve XSD imports from classpath resources using file: and jar: protocols, which are
   * blocked when these properties are set to empty strings. The residual XXE risk is mitigated by
   * the fact that all schemas are loaded from application classpath resources, not from untrusted
   * external sources.
   */
  @SuppressWarnings("java:S2755")
  public static SchemaFactory schemaFactory() throws SAXException {
    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    return factory;
  }


  /**
   * FalsePositive Sonar flags this method as vulnerable to XXE (java:S2755), but it is a false
   * positive because FEATURE_SECURE_PROCESSING is enabled to prevent entity expansion attacks.
   * See the comment on schemaFactory() for more details.
   */
  @SuppressWarnings("java:S2755")
  public static Validator validator(Schema schema) throws SAXException {
    Validator validator = schema.newValidator();
    validator.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    return validator;
  }

  /** False Positive
   * Sonar flags TransformerFactory.newInstance() as vulnerable to XXE (java:S2755).
   * FEATURE_SECURE_PROCESSING is enabled to mitigate entity expansion (DoS) attacks.
   * ACCESS_EXTERNAL_DTD and ACCESS_EXTERNAL_STYLESHEET cannot be set to empty strings because it
   * breaks schema/stylesheet resolution required by the application. The residual XXE risk is
   * mitigated by the fact that all XML input processed by this transformer originates from internal
   * registry regulation content stored in a controlled Git repository, not from untrusted external
   * sources.
   */
  @SuppressWarnings("java:S2755")
  public static Transformer transformer() throws TransformerConfigurationException {
    TransformerFactory factory = TransformerFactory.newInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    return factory.newTransformer();
  }
}
