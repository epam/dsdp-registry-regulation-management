/*
 * Copyright 2021 EPAM Systems.
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

import com.epam.digital.data.platform.management.reportexporter.config.feign.FeignErrorDecoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.Retryer.Default;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class FeignConfig {

  private final ReportExporterOpenShiftClient client;

  public FeignConfig(ReportExporterOpenShiftClient client) {
    this.client = client;
  }

  @Bean
  public RequestInterceptor requestInterceptor() {
    return requestTemplate ->
        requestTemplate.header("Authorization", client.getApiKey());
  }

  @Bean
  public Decoder feignDecoder(@Qualifier("reportExporterMapper") ObjectMapper reportExporterMapper) {
    var jacksonConverter = new MappingJackson2HttpMessageConverter(reportExporterMapper);
    ObjectFactory<HttpMessageConverters> objectFactory =
        () -> new HttpMessageConverters(jacksonConverter);
    return new ResponseEntityDecoder(new SpringDecoder(objectFactory));
  }

  @Bean
  public ErrorDecoder errorDecoder() {
    return new FeignErrorDecoder();
  }

  @Bean
  public Retryer retryer() {
    return new Default();
  }
}
