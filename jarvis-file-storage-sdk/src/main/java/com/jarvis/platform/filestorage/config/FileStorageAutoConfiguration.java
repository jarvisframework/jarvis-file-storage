package com.jarvis.platform.filestorage.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.jarvis.platform.filestorage.sdk.FileStorageSdkService;
import com.jarvis.platform.filestorage.sdk.impl.LocalStorageSdkServiceImpl;
import com.jarvis.platform.filestorage.sdk.impl.MinioStorageSdkServiceImpl;

import io.minio.MinioClient;

/**
 *
 * @author qiucs
 * @version 1.0.0 2022年7月13日
 */
@Configuration
@EnableConfigurationProperties(FileStorageProperties.class)
public class FileStorageAutoConfiguration {

    @Bean
    FileStorageSdkService localFileStorageSdkService() {
        return new LocalStorageSdkServiceImpl();
    }

    @ConditionalOnProperty(prefix = "gdda.fs", name = "type", havingValue = "MINIO")
    @Bean
    @Primary
    FileStorageSdkService minioFileStorageSdkService(FileStorageProperties properties) {

        final MinioClient client = MinioClient.builder().endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();

        return new MinioStorageSdkServiceImpl(client, properties.isOnlyOneBucket());
    }
}
