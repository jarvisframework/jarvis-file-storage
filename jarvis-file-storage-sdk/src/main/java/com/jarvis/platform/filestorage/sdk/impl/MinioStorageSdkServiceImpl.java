package com.jarvis.platform.filestorage.sdk.impl;

import com.jarvis.framework.constant.SymbolConstant;
import com.jarvis.framework.core.exception.BusinessException;
import com.jarvis.platform.filestorage.sdk.FileStorageSdkService;
import io.minio.BucketExistsArgs;
import io.minio.ComposeObjectArgs;
import io.minio.ComposeSource;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.DownloadObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectArgs.Builder;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteArgs;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.UploadObjectArgs;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @author Doug Wang
 * @version 1.0.0 2022年7月8日
 */
@Slf4j
public class MinioStorageSdkServiceImpl implements FileStorageSdkService {

    private static final String DATA_BUCKET = "data";

    private final boolean isOnlyOneBucket;

    private final MinioClient client;

    /**
     * @param client 客户端
     */
    public MinioStorageSdkServiceImpl(MinioClient client) {
        this(client, true);
    }

    /**
     * @param client          客户端
     * @param isOnlyOneBucket 是否只使用一个桶
     */
    public MinioStorageSdkServiceImpl(MinioClient client, boolean isOnlyOneBucket) {
        super();
        this.client = client;
        this.isOnlyOneBucket = isOnlyOneBucket;
        init(client);
    }

    private void init(MinioClient client) {
        if (isOnlyOneBucket) {
            checkBucket(DATA_BUCKET);
        }
    }

    private void checkBucket(String bucket) {
        try {
            final boolean bucketExists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!bucketExists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (final Exception e) {
            throw new BusinessException("创建MINIO桶" + bucket + "出错", e);
        }
    }

    /**
     *
     * @see com.jarvis.platform.filestorage.sdk.FileStorageSdkService#makeLocation(java.lang.String)
     */
    @Override
    public void makeLocation(String location) {
        if (isOnlyOneBucket) {
            makedir("", location);
        } else {

            checkBucket(toBucket(location));
        }
    }

    /**
     *
     * @see com.jarvis.platform.filestorage.sdk.FileStorageSdkService#makedir(java.lang.String, java.lang.String)
     */
    @Override
    public void makedir(String location, String dir) {
        if (!isEndsWithSplit(dir)) {
            dir += SymbolConstant.SLASH;
        }
        putObject(location, dir, new ByteArrayInputStream(new byte[] {}));
    }

    /**
     *
     * @see com.jarvis.platform.filestorage.sdk.FileStorageSdkService#upload(java.lang.String, java.lang.String,
     *      java.io.File)
     */
    @Override
    public boolean upload(String location, String path, File file) {
        return uploadObject(location, path, file.getAbsolutePath());
    }

    /**
     *
     * @see com.jarvis.platform.filestorage.sdk.FileStorageSdkService#upload(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public boolean upload(String location, String path, String filename) {
        return uploadObject(location, path, filename);
    }

    /**
     *
     * @see com.jarvis.platform.filestorage.sdk.FileStorageSdkService#upload(java.lang.String, java.lang.String,
     *      java.io.InputStream)
     */
    @Override
    public boolean upload(String location, String path, InputStream is) {
        return putObject(location, path, is);
    }

    /**
     *
     * @see com.jarvis.platform.filestorage.sdk.FileStorageSdkService#download(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public File download(String location, String path, String downloadFilePath) {
        return downloadObject(location, path, downloadFilePath);
    }

    /**
     *
     * @see com.jarvis.platform.filestorage.sdk.FileStorageSdkService#download(java.lang.String,
     *      java.lang.String, long, long, java.io.OutputStream)
     */
    @Override
    public void download(String location, String path, OutputStream os) {
        download(location, path, -1, -1, os);
    }

    /**
     *
     * @see com.jarvis.platform.filestorage.sdk.FileStorageSdkService#download(java.lang.String,
     *      java.lang.String, long, long, java.io.OutputStream)
     */
    @Override
    public void download(String location, String path, long start, long end, OutputStream os) {
        InputStream is = null;
        try {
            long len = (end - start + 1);
            if (start == -1 && end == -1) {
                len = -1;
            } else {
                if (start < 0) {
                    throw new BusinessException("下载文件开始位置不能小于0");
                }
                if (end < start) {
                    throw new BusinessException("下载文件结束位置不能小于开始位置");
                }
            }
            is = getObject(location, path, start, len);
            StreamUtils.copy(is, os);
        } catch (final Exception e) {
            throw new BusinessException(String.format("下载本地文件出错"), e);
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (final IOException e) {
                    log.error("关闭文件流出错", e);
                }
                is = null;
            }
        }
    }

    /**
     *
     * @see com.jarvis.platform.filestorage.sdk.FileStorageSdkService#download(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public InputStream download(String location, String path) {
        return getObject(location, path, -1, -1);
    }

    /**
     *
     * @see com.jarvis.platform.filestorage.sdk.FileStorageSdkService#delete(java.lang.String,
     *      java.lang.String[])
     */
    @Override
    public void delete(String location, String... paths) {
        final RemoveObjectsArgs args = RemoveObjectsArgs.builder()
                .bucket(toBucket(location))
                .objects(Stream.of(paths)
                        .map(p -> new DeleteObject(toObjectName(location, p))).collect(Collectors.toList()))
                .build();
        final Iterable<Result<DeleteError>> it = client.removeObjects(args);

        if (log.isInfoEnabled()) {
            it.forEach(r -> {
                try {
                    final DeleteError error = r.get();
                    log.info("删除桶[{}]下对象[{}]出错: {}", error.bucketName(), error.objectName(), error.message());
                } catch (final Exception e) {
                    log.error("删除对象出错日志记录出错！", e);
                }
            });
        }
    }

    /**
     *
     * @see com.jarvis.platform.filestorage.sdk.FileStorageSdkService#copy(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void copy(String location, String path, String toPath) {
        copy(location, path, location, toPath);
    }

    /**
     *
     * @see com.jarvis.platform.filestorage.sdk.FileStorageSdkService#copy(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void copy(String location, String path, String toLocation, String toPath) {
        try {
            final ObjectWriteResponse response = client.copyObject(CopyObjectArgs.builder()
                    .bucket(toBucket(toLocation))
                    .object(toObjectName(toLocation, toPath))
                    .source(
                            CopySource.builder()
                                    .bucket(toBucket(location))
                                    .object(toObjectName(location, path))
                                    .build())
                    .build());
            if (log.isDebugEnabled()) {
                log.debug("复制成功：{}", response);
            }
        } catch (final Exception e) {
            throw new BusinessException(String.format("复制桶[%s]下对象[%s]出错", location, path), e);
        }
    }

    /**
     *
     * @see com.jarvis.platform.filestorage.sdk.FileStorageSdkService#move(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void move(String location, String path, String toPath) {
        copy(location, path, toPath);
        delete(location, path);

    }

    /**
     *
     * @see com.jarvis.platform.filestorage.sdk.FileStorageSdkService#move(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void move(String location, String path, String toLocation, String toPath) {
        copy(location, path, toLocation, toPath);
        delete(toLocation, path);
    }

    /**
     * @see com.jarvis.platform.filestorage.sdk.FileStorageSdkService#merge(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.util.Comparator)
     */
    @Override
    public void merge(String location, String dir, String toLocation, String toPath, Comparator<String> comparator) {
        String objectDir = dir;
        if (!isEndsWithSplit(dir)) {
            objectDir += '/';
        }

        final Iterable<Result<Item>> objects = client.listObjects(
                ListObjectsArgs.builder()
                        .bucket(toBucket(location))
                        .prefix(toObjectName(location, objectDir))
                        .recursive(false)
                        .build());
        try {
            final List<DeleteObject> deleteObjects = new ArrayList<>();
            client.composeObject(ComposeObjectArgs.builder()
                    .bucket(toBucket(toLocation))
                    .object(toObjectName(toLocation, toPath))
                    .sources(StreamSupport.stream(objects.spliterator(), false)
                            .sorted((a, b) -> {
                                try {
                                    return comparator.compare(a.get().objectName(), b.get().objectName());
                                } catch (final Exception e) {
                                    throw new BusinessException(String.format("排序合并桶[%s]下对象[%s]出错", location, dir), e);
                                }
                            })
                            .map(r -> {
                                try {
                                    final String objectName = r.get().objectName();
                                    deleteObjects.add(new DeleteObject(objectName));
                                    return ComposeSource.builder().bucket(toBucket(location)).object(objectName)
                                            .build();
                                } catch (final Exception e) {
                                    throw new BusinessException(String.format("生成合并桶[%s]下对象[%s]出错", location, dir), e);
                                }
                            }).collect(Collectors.toList()))
                    .build());
            client.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(toBucket(location))
                    .objects(deleteObjects)
                    .build());
        } catch (final Exception e) {
            throw new BusinessException(String.format("合并桶[%s]下对象[%s]出错", location, dir), e);
        }
    }

    private String toBucket(String location) {
        if (isOnlyOneBucket) {
            return DATA_BUCKET;
        }
        return location;
    }

    private boolean isEndsWithSplit(String str) {
        return str.endsWith("/") || str.endsWith("\\");
    }

    private boolean isStartsWithSplit(String str) {
        return str.startsWith("/") || str.startsWith("\\");
    }

    private String toObjectName(String location, String path) {
        if (!isOnlyOneBucket) {
            return path;
        }
        if (!isEndsWithSplit(location) && !isStartsWithSplit(path)) {
            return location + SymbolConstant.SLASH + path;
        }
        return location + path;
    }

    private File downloadObject(String location, String path, String downloadFilePath) {
        try {
            final File file = new File(downloadFilePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            final DownloadObjectArgs args = DownloadObjectArgs.builder()
                    .bucket(toBucket(location))
                    .object(toObjectName(location, path))
                    .filename(downloadFilePath)
                    .build();
            client.downloadObject(args);
            return file;
        } catch (final Exception e) {
            throw new BusinessException(String.format("MINIO下载文件[%s]出错", path), e);
        }
    }

    private InputStream getObject(String location, String path, long offset, long length) {
        try {
            final Builder builder = GetObjectArgs.builder();
            builder.bucket(toBucket(location)).object(toObjectName(location, path));
            if (offset > -1) {
                builder.offset(offset);
            }
            if (length > 0) {
                builder.length(length);
            }

            return client.getObject(builder.build());
        } catch (final Exception e) {
            throw new BusinessException(String.format("MINIO下载文件流[%s]出错", path), e);
        }
    }

    private boolean uploadObject(String location, String path, String filename) {
        try {
            final UploadObjectArgs args = UploadObjectArgs.builder().bucket(toBucket(location))
                    .object(toObjectName(location, path))
                    .filename(filename)
                    .build();
            final ObjectWriteResponse response = client.uploadObject(args);
            if (log.isDebugEnabled()) {
                log.debug(response.object());
            }
        } catch (final Exception e) {
            throw new BusinessException(String.format("MINIO上传文件[%s]出错", filename), e);
        }
        return true;
    }

    private boolean putObject(String location, String path, InputStream is) {
        try {
            final PutObjectArgs args = PutObjectArgs.builder().bucket(toBucket(location))
                    .object(toObjectName(location, path))
                    .stream(is, is.available(), ObjectWriteArgs.MIN_MULTIPART_SIZE)
                    .build();
            final ObjectWriteResponse response = client.putObject(args);
            if (log.isDebugEnabled()) {
                log.debug(response.object());
            }
        } catch (final Exception e) {
            throw new BusinessException("MINIO流式上传出错", e);
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (final IOException e) {
                    log.error("关闭流出错", e);
                }
            }
        }
        return true;
    }

    /**
     *
     * @see com.jarvis.platform.filestorage.sdk.FileStorageSdkService#list(java.lang.String)
     */
    @Override
    public List<String> list(String location) {
        final ListObjectsArgs args = ListObjectsArgs.builder()
                .bucket(toBucket(location))
                .prefix(toObjectName(location, SymbolConstant.SLASH))
                .recursive(false)
                .build();
        return list(args);
    }

    /**
     *
     * @see com.jarvis.platform.filestorage.sdk.FileStorageSdkService#list(java.lang.String, java.lang.String)
     */
    @Override
    public List<String> list(String location, String dir) {
        String objectDir = dir;
        if (!isEndsWithSplit(dir)) {
            objectDir += '/';
        }
        final ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                .bucket(toBucket(location))
                .prefix(toObjectName(location, objectDir))
                .recursive(false)
                .build();
        return list(listObjectsArgs);
    }

    private List<String> list(ListObjectsArgs args) {
        final List<String> files = new ArrayList<>();
        final Iterable<Result<Item>> listObjects = client.listObjects(args);
        final Iterator<Result<Item>> iterator = listObjects.iterator();
        try {
            final int start = args.prefix().length();
            while (iterator.hasNext()) {
                final Result<Item> result = iterator.next();
                final Item item = result.get();
                files.add(item.objectName().substring(start));
            }
        } catch (final Exception e) {
            throw BusinessException.create(e, "罗列目录下所有文件夹和文件出错");
        }
        return files;
    }

}
