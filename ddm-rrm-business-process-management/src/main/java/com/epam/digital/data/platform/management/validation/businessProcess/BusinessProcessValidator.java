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

package com.epam.digital.data.platform.management.validation.businessProcess;

import com.epam.digital.data.platform.management.core.utils.SecureXMLFactory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;

@Slf4j
public class BusinessProcessValidator implements ConstraintValidator<BusinessProcess, String> {

  public static final String SCHEMA_PATH = "/org/camunda/bpm/model/bpmn/schema/";
  public static final String DDM_BP_SCHEMA = SCHEMA_PATH + "bp-schema.xsd";

  @Override
  /**
   * FalsePositive The XXE_VALIDATOR warning is suppressed because we use a custom-configured
   * secure factory method, SecureXMLFactory.newValidator(), which creates a validator with XXE
   * protection enabled. Sonar cannot detect the security measures applied
   * due to inter-procedural analysis limitations.
   */
  @SuppressWarnings("findsecbugs:XXE_VALIDATOR")
  public boolean isValid(String bpContent, ConstraintValidatorContext constraintValidatorContext) {
    try (var businessProcessReader = new StringReader(bpContent)) {
      var validator = initValidator();
      validator.validate(new StreamSource(businessProcessReader));
    } catch (SAXException | IOException e) {
      log.trace("Failed to validate business process XML: {}", e.getMessage(), e);
      constraintValidatorContext.disableDefaultConstraintViolation();
      constraintValidatorContext.buildConstraintViolationWithTemplate(e.getMessage())
          .addConstraintViolation();
      return false;
    }
    return true;
  }

  /**
   * FalsePositive The XXE_SCHEMA_FACTORY warning is suppressed because we use a custom-configured secure
   * factory, SecureXMLFactory.schemaFactory(), which already applies XXE protection by
   * default. Sonar cannot detect the security measures applied
   * due to inter-procedural analysis limitations.
   */
  @SuppressWarnings("findsecbugs:XXE_SCHEMA_FACTORY")
  private Validator initValidator() throws SAXException, IOException {
    var resourceDdm = getClass().getResource(DDM_BP_SCHEMA);
    var factory = SecureXMLFactory.schemaFactory();
    factory.setResourceResolver(new SchemaResolver(SCHEMA_PATH));
    var schema = factory.newSchema(new StreamSource(resourceDdm.toExternalForm()));
    return SecureXMLFactory.validator(schema);
  }
}
