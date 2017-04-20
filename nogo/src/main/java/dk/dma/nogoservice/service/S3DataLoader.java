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
package dk.dma.nogoservice.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import dk.dma.nogoservice.ApiProfiles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Class for loading S3 data, also caches the data locally for faster development boot
 * @author Klaus Groenbaek
 *         Created 20/04/17.
 */
@Profile(ApiProfiles.PRODUCTION)
@Component
@Slf4j
public class S3DataLoader {

    private static final String DATA_BUCKET = "maritime-web-nogo";
    private final File tempDir;
    private boolean cacheLocally;
    private final AmazonS3 amazonS3;

    @Autowired
    public S3DataLoader(@Value("${s3dataloader.tempdir:#{null}}") String tempDirLocation) {
        tempDirLocation = tempDirLocation != null ? tempDirLocation : System.getProperty("java.io.tmpdir");
        tempDir = new File(tempDirLocation);
        if (!tempDir.exists()) {
            cacheLocally = tempDir.mkdirs();
        } else {
            cacheLocally = true;
        }

        AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
        try {
            credentialsProvider.getCredentials();
        } catch (SdkClientException e) {
            throw new IllegalStateException("You must define AWS credentials in USER_HOME/.aws/credentials file on the machine. When running on AWS container service, " +
                    "the TaskRole should have permissions to read from S3");
        }

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
        builder.setCredentials(credentialsProvider);
        builder.setRegion("eu-west-1");
        amazonS3 = builder.build();
        Preconditions.checkArgument(amazonS3.doesBucketExist(DATA_BUCKET), "No AWS S3 bucket named " + DATA_BUCKET + " this bucket must exist");
    }


    <T> T loadData(String key, Class<T> clazz) throws IOException{
        Stopwatch stopwatch = Stopwatch.createStarted();
        S3Object object = amazonS3.getObject(S3DataLoader.DATA_BUCKET, key);

        File cacheFile = new File(tempDir, object.getObjectMetadata().getETag() + key);
        ObjectMapper objectMapper = new ObjectMapper();
        if (cacheLocally) {
            if (cacheFile.exists()) {
                log.info("Using local cached file {}", cacheFile.getAbsolutePath());
                return objectMapper.readValue(cacheFile, clazz);
            }
        }

        try (S3ObjectInputStream objectContent = object.getObjectContent()) {
            T data = objectMapper.readValue(objectContent, clazz);
            if (cacheLocally) {
                log.info("caching S3 file locally in {}", cacheFile.getAbsolutePath());
                objectMapper.writeValue(cacheFile, data);
            }
            return data;
        } finally {
            log.info("Loaded file {} from Amazon S3 in {} ms", key, stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }
}
