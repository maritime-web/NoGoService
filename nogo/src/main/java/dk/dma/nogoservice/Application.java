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

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dk.dma.nogoservice.service.S3FileBackedQueryArea.DATA_BUCKET;

/**
 * @author Klaus Groenbaek
 *         Created 10/03/17.
 */
@EnableSwagger2
@SpringBootApplication
public class Application extends WebMvcConfigurerAdapter {

    @Value("${aws_access_key_id:#{null}}")
    private String aws_access_key_id;

    @Value("${aws_secret_access_key:#{null}}")
    private String aws_secret_access_key;

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
     * creates an S3 client, so we can load data from S3
     * In development credentials can be loaded through the default chain, which looks at the file USER_HOME/.was/credentials
     * if this is not present, you must provide property configuration for
     * @return a working S3 client where passwords have been checked
     */
    @Bean
    @Profile(ApiProfiles.PRODUCTION)
    public AmazonS3 amazonS3() {
        AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
        try {
            credentialsProvider.getCredentials();
        } catch (SdkClientException e) {
            Preconditions.checkNotNull(aws_access_key_id, "aws_access_key_id property must be set when there is no USER_HOME/.aws/credentials file on the machine");
            Preconditions.checkNotNull(aws_access_key_id, "aws_secret_access_key property must be set when there is no USER_HOME/.aws/credentials file on the machine");
            credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(aws_access_key_id, aws_secret_access_key));
        }

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
        builder.setCredentials(credentialsProvider);
        builder.setRegion("eu-west-1");
        AmazonS3 amazonS3 = builder.build();
        Preconditions.checkArgument(amazonS3.doesBucketExist(DATA_BUCKET), "No AWS S3 bucket named " + DATA_BUCKET + " this bucket must exist");

        return amazonS3;
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
