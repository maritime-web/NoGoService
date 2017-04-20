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
package dk.dma.nogoservice;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import dk.dma.nogoservice.service.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Klaus Groenbaek
 *         Created 10/03/17.
 */
@EnableSwagger2
@SpringBootApplication
public class Application extends WebMvcConfigurerAdapter {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).profiles(ApiProfiles.PRODUCTION).run(args);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }

    @Bean(destroyMethod = "shutdown")
    public PoolingHttpClientConnectionManager connectionManager() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        cm.setDefaultMaxPerRoute(20);
        return cm;
    }

    @Bean
    public ExecutorService slicingExecutor() {
        return Executors.newFixedThreadPool(2);
    }

    /**
     * Returns a list of QueryArea beans that can be autowired.
     */
    @Bean
    @Profile(ApiProfiles.PRODUCTION)
    public List<QueryArea> fromS3(WeatherService weatherService, NoGoAlgorithmFacade noGoAlgorithm, S3DataLoader dataLoader) throws IOException {
        List<QueryArea> beans = new ArrayList<>();
        ArrayList<String> files = Lists.newArrayList("NorthKattegat_depth.json", "SouthKattegat_depth.json");
        for (String file : files) {
            beans.add(new S3FileBackedQueryArea(dataLoader, file, weatherService, noGoAlgorithm));
        }

        return beans;
    }


    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(Predicates.not(PathSelectors.regex("/error")))
                .build();
    }



}
