package com.jarvis.platform.filestorage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.jarvis.platform.filestorage.constant.FileStorageTypeEnum;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author qiucs
 * @version 1.0.0 2022年7月8日
 */
@Setter
@Getter
@ToString
@ConfigurationProperties(prefix = "gdda.fs")
public class FileStorageProperties {

    /** 存储类型：LOCAL/MINIO */
    private FileStorageTypeEnum type;

    /** 对象存储地址 **/
    private String endpoint = "http://127.0.0.1:9000";

    /** 对象存储用户名 **/
    private String accessKey = "minio";

    /** 对象存储密码 **/
    private String secretKey = "00000000";

    /** 对象存储是否只使用一个桶 **/
    private boolean onlyOneBucket = true;
}
