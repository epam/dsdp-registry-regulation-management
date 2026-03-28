/*
 * Copyright 2024 EPAM Systems.
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

package com.epam.digital.data.platform.management.i18n.service;

import com.epam.digital.data.platform.management.i18n.exception.I18nAlreadyExistsException;
import com.epam.digital.data.platform.management.i18n.exception.I18nNotFoundException;
import com.epam.digital.data.platform.management.i18n.model.I18nInfoDto;
import java.util.List;

/**
 * The I18nService interface defines the methods for retrieving, updating, and deleting
 * internationalization resources.
 */
public interface I18nService {

  /**
   * Retrieves a list of internationalization information (I18nInfoDto) for a given version.
   *
   * @param versionName the name of the version
   * @return a list of I18nInfoDto objects representing internationalization information
   */
  List<I18nInfoDto> getI18nListByVersion(String versionName);

  /**
   * Retrieves a list of internationalization information (I18nInfoDto) for a given version that
   * have changed.
   *
   * @param versionName the name of the version
   * @return a list of I18nInfoDto objects representing internationalization information for the
   * given version that have changed
   */
  List<I18nInfoDto> getChangedI18nListByVersion(String versionName);


  /**
   * Creates a new internationalization resource with the specified name and content in specified
   * version.
   *
   * @param i18nName    the name of the internationalization resource
   * @param content     the content of the internationalization resource
   * @param versionName the name of the version in which the internationalization resource is
   *                    created
   * @throws I18nAlreadyExistsException if the internationalization resource with the specified name
   *                                    already exists
   */
  void createI18n(String i18nName, String content, String versionName)
      throws I18nAlreadyExistsException;

  /**
   * Retrieves the content of an internationalization resource for the specified name in specified
   * version.
   *
   * @param i18nName    the name of the internationalization resource
   * @param versionName the name of the version
   * @return the content of the internationalization resource
   *
   * @throws I18nNotFoundException if the internationalization resource is not found
   */
  String getI18nContent(String i18nName, String versionName) throws I18nNotFoundException;

  /**
   * Updates the content of an internationalization resource with the specified content, i18n name,
   * version name, and ETag.
   *
   * @param content     the new content of the internationalization resource
   * @param i18nName    the name of the internationalization resource
   * @param versionName the name of the version to update the internationalization resource in
   * @param eTag        the ETag of the internationalization resource for optimistic concurrency
   *                    control
   */
  void updateI18n(String content, String i18nName, String versionName, String eTag);

  /**
   * Rolls back the internationalization resource with the given i18nName and versionName. This
   * method reverts the content of the internationalization resource to master version.
   *
   * @param i18nName    the name of the internationalization resource to rollback
   * @param versionName the name of the version to rollback the internationalization resource to
   * @throws I18nNotFoundException if the internationalization resource is not found in both master
   *                               and candidate
   */
  void rollbackI18n(String i18nName, String versionName) throws I18nNotFoundException;

  /**
   * Deletes an internationalization resource with the given name and version.
   *
   * @param i18nName    the name of the internationalization resource to be deleted
   * @param versionName the name of the version that contains the internationalization resource
   * @param eTag        the ETag of the internationalization resource for optimistic concurrency
   *                    control
   * @throws I18nNotFoundException if the internationalization resource is not found
   */
  void deleteI18n(String i18nName, String versionName, String eTag) throws I18nNotFoundException;
}