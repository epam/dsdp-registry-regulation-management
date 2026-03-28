/*
 * Copyright 2023 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package com.epam.digital.data.platform.management.validation;

import com.epam.digital.data.platform.management.core.utils.SecureXMLFactory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;

/**
 * Validates change log file against liquibase schemas and liquibase extension schemas
 *
 * @see DDMExtensionChangelogFile
 */
@RequiredArgsConstructor
@Slf4j
public class DDMExtensionChangelogFileValidator implements
    ConstraintValidator<DDMExtensionChangelogFile, String> {

  private static final String SCHEMA_PATH = "/liquibase-schema";
  private static final String DB_CHANGELOG_SCHEMA = SCHEMA_PATH + "/dbchangelog.xsd";
  private static final String LIQUIBASE_EXT_SCHEMA = SCHEMA_PATH + "/liquibase-ext-schema.xsd";

  @Override
  /**
   * FalsePositive
   * The XXE_VALIDATOR warning is suppressed because we use a custom-configured
   * secure factory method, SecureXMLFactory.newValidator(), which creates a validator with XXE
   * protection enabled. Sonar cannot detect the security measures applied
   * due to inter-procedural analysis limitations.
   */
  @SuppressWarnings("findsecbugs:XXE_VALIDATOR")
  public boolean isValid(String changeLogContent, ConstraintValidatorContext context) {
    try (var businessProcessReader = new StringReader(changeLogContent)) {
      var dbChangelog = initValidator(DB_CHANGELOG_SCHEMA);
      dbChangelog.validate(new StreamSource(businessProcessReader));
      // TODO uncomment when fix "Error for type 'whereType'. Multiple elements with name
      //  'condition', with different types, appear in the model group."
      //  var liquibaseExtValidator = initValidator(LIQUIBASE_EXT_SCHEMA)
      //  liquibaseExtValidator.validate(new StreamSource(businessProcessReader))
    } catch (SAXException | IOException e) {
      log.trace("Failed to validate changelog XML file: {}", e.getMessage(), e);
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
      return false;
    }
    return true;
  }

  /**
   * FalsePositive
   * The XXE_SCHEMA_FACTORY warning is suppressed because we use a custom-configured secure
   * factory, SecureXMLFactory.schemaFactory(), which already applies XXE protection by
   * default. Sonar cannot detect the security measures applied
   * due to inter-procedural analysis limitations.
   */
  @SuppressWarnings("findsecbugs:XXE_SCHEMA_FACTORY")
  private Validator initValidator(String dbChangeLogSchema) throws SAXException {
    var resourceDdm = getClass().getResource(dbChangeLogSchema);
    var schema = SecureXMLFactory.schemaFactory()
        .newSchema(new StreamSource(resourceDdm.toExternalForm()));
    return SecureXMLFactory.validator(schema);
  }
}
