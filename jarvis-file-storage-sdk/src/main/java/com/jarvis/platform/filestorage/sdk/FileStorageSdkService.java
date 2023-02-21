package com.jarvis.platform.filestorage.sdk;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author qiucs
 * @version 1.0.0 2022年7月8日
 */
public interface FileStorageSdkService {

    /**
     * 创建存储位置
     *
     * @param location 存储位置 如：archive-type
     */
    void makeLocation(String location);

    /**
     * 创建存储下目录
     *
     * @param location 存储位置 如：archive-type
     * @param dir      存储下目录 如：data/logs/
     */
    void makedir(String location, String dir);

    /**
     * 上传文件
     *
     * @param location 存储位置 如：archive-type
     * @param path     存储下的相对路径，如： sh001/2000/1/001/xxx.pdf
     * @param filename 上传的文件名称（绝对路径）
     * @return boolean
     */
    boolean upload(String location, String path, String filename);

    /**
     * 上传文件
     *
     * @param location 存储位置 如：archive-type
     * @param path     存储下的相对路径，如： sh001/2000/1/001/xxx.pdf
     * @param file     上传的文件对象
     * @return boolean
     */
    boolean upload(String location, String path, File file);

    /**
     * 上传文件
     *
     * @param location 存储位置 如：archive-type
     * @param path     存储下的相对路径，如： sh001/2000/1/001/xxx.pdf
     * @param is       上传的文件流
     * @return boolean
     */
    boolean upload(String location, String path, InputStream is);

    /**
     * 下载文件流（分段下载），注：如果是文件流，则确保文件对应文件夹的已存在，否则下载不成功
     *
     * @param location 存储位置 如：archive-type
     * @param path     存储下的相对路径，如： sh001/2000/1/001/xxx.pdf
     * @param os       输出流（流关闭，需要调用者手动关闭）
     */
    void download(String location, String path, OutputStream os);

    /**
     * 下载文件流（分段下载），注：如果是文件流，则确保文件对应文件夹的已存在，否则下载不成功
     *
     * @param location 存储位置 如：archive-type
     * @param path     存储下的相对路径，如： sh001/2000/1/001/xxx.pdf
     * @param start    开始位置
     * @param end      结束位置
     * @param os       输出流（流关闭，需要调用者手动关闭）
     */
    void download(String location, String path, long start, long end, OutputStream os);

    /**
     * 下载文件流
     *
     * @param location 存储位置 如：archive-type
     * @param path     存储下的相对路径，如： sh001/2000/1/001/xxx.pdf
     */
    InputStream download(String location, String path);

    /**
     * 下载文件（注：确保文件对应文件夹的已存在，否则下载不成功）
     *
     * @param location         存储位置 如：archive-type
     * @param path             存储下的相对路径，如： sh001/2000/1/001/xxx.pdf
     * @param downloadFilePath 下载下来的文件路径 如：C:/temp/aa.pdf
     * @return File
     */
    File download(String location, String path, String downloadFilePath);

    /**
     * 删除
     *
     * @param location 存储位置 如：archive-type
     * @param paths    存储下的相对路径，如： sh001/2000/1/001/xxx.pdf
     */
    void delete(String location, String... paths);

    /**
     * 相同存储间的复制
     *
     * @param location 存储位置 如：archive-type
     * @param path     存储下的相对路径，如： sh001/2000/1/001/xxx.pdf
     * @param toPath   存储下复制到的相对路径，如： sh002/2000/1/001/xxx.pdf
     */
    void copy(String location, String path, String toPath);

    /**
     * 不同存储间的复制
     *
     * @param location   存储位置 如：archive-tmp
     * @param path       存储下的相对路径，如： sh001/2000/1/001/xxx.pdf
     * @param toLocation 复制到的存储 如：archive-type
     * @param toPath     存储下复制到的相对路径，如： sh002/2000/1/001/xxx.pdf
     */
    void copy(String location, String path, String toLocation, String toPath);

    /**
     * 相同存储间的移动
     *
     * @param location 存储位置 如：archive-type
     * @param path     存储下的相对路径，如： sh001/2000/1/001/xxx.pdf
     * @param toPath   存储下移动到的相对路径，如： sh002/2000/1/001/xxx.pdf
     */
    void move(String location, String path, String toPath);

    /**
     * 不同存储间的移动
     *
     * @param location   存储位置 如：archive-tmp
     * @param path       存储下的相对路径，如： sh001/2000/1/001/xxx.pdf
     * @param toLocation 移动到的存储 如：archive-type
     * @param toPath     存储下移动到的相对路径，如： sh002/2000/1/001/xxx.pdf
     */
    void move(String location, String path, String toLocation, String toPath);

    /**
     * 合并文件，合并完后会删除原目录及下面文件
     *
     * @param location   存储位置 如：archive-tmp
     * @param dir        存储下的相对目录路径，如： sh001/2000/1/001/
     * @param toLocation 合并到的存储 如：archive-type
     * @param toPath     存储下合并到的相对路径，如： sh002/2000/1/001/xxx.pdf
     * @param comparator 对目录dir下的文件进行排序
     */
    void merge(String location, String dir, String toLocation, String toPath, Comparator<String> comparator);

    /**
     * 获取存储位置下文件或文件夹
     *
     * <pre>
     *
     *  存储位置
     *  |-- b1
     *  |-- b2
     *  |-- t1.txt
     *
     *  结果：b1/, b2/, t1.txt （文件夹以'/'结束）
     * </pre>
     *
     * @param location 存储位置 如：archive-tmp
     * @return List
     */
    List<String> list(String location);

    /**
     * 获取存储位置指定目录下文件或文件夹
     *
     * <pre>
     *
     *  存储位置/指定目录
     *  |-- b1
     *  |-- b2
     *  |-- t1.txt
     *
     *  结果：b1/, b2/, t1.txt （文件夹以'/'结束）
     * </pre>
     *
     * @param location 存储位置 如：archive-tmp
     * @param dir      存储下的相对目录路径，如： sh001/2000/1/001/
     * @return List
     */
    List<String> list(String location, String dir);

}
