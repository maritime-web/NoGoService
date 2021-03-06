/* Copyright (c) 2011 Danish Maritime Authority.
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
package dk.dma.dmiweather;

import com.google.common.base.Predicates;
import dk.dma.dmiweather.service.ForecastConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.List;

/**
 * @author Klaus Groenbaek
 *         Created 03/04/17.
 */
@SpringBootApplication
@EnableSwagger2
public class Application extends WebMvcConfigurerAdapter {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).run(args);
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(Predicates.not(
                        Predicates.or(PathSelectors.regex("/error"),
                                PathSelectors.regex("/ping"),
                                PathSelectors.regex("/debughealth"))
                ))
                .build();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }

    /**
     * The list of forecasts which will be pulled from DMIs FTP server
     * @return a list of ForecastConfiguration which can be autowired (into the FTPLoader)
     */
    @Bean
    public List<ForecastConfiguration> forecasts() {
        return Arrays.asList(ForecastConfiguration.values());
    }

}