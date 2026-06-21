package com.grey.myblog.utils;

import com.grey.myblog.enums.UploadEnum;
import com.grey.myblog.exception.FileUploadException;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MinIO 文件上传工具类
 *
 * @author grey
 */
@Slf4j
@Component
public class MinioUtils {

    @Resource
    private MinioClient minioClient;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucketName}")
    private String bucketName;

    /**
     * 上传文件
     *
     * @param uploadEnum 文件类型枚举
     * @param file       文件
     * @return 上传后的文件访问 URL
     */
    public String upload(UploadEnum uploadEnum, MultipartFile file) {
        // 校验文件
        validateFile(uploadEnum, file);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + extension;
        String objectName = uploadEnum.getDir() + fileName;

        try (InputStream stream = file.getInputStream()) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .contentType(file.getContentType())
                    .object(objectName)
                    .stream(stream, file.getSize(), -1)
                    .build();

            minioClient.putObject(args);

            // 返回访问 URL
            return endpoint + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new FileUploadException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 上传文件（指定文件名）
     *
     * @param uploadEnum 文件类型枚举
     * @param file       文件
     * @param fileName   文件名（不带后缀）
     * @return 上传后的文件访问 URL
     */
    public String upload(UploadEnum uploadEnum, MultipartFile file, String fileName) {
        validateFile(uploadEnum, file);

        String extension = getFileExtension(file.getOriginalFilename());
        String objectName = uploadEnum.getDir() + fileName + extension;

        try (InputStream stream = file.getInputStream()) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .contentType(file.getContentType())
                    .object(objectName)
                    .stream(stream, file.getSize(), -1)
                    .build();

            minioClient.putObject(args);

            return endpoint + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new FileUploadException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除单个文件
     *
     * @param fileUrl 文件完整 URL
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileUrl) {
        try {
            String objectName = extractObjectName(fileUrl);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("文件删除成功: {}", objectName);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量删除文件
     *
     * @param fileUrls 文件完整 URL 列表
     * @return 是否全部删除成功
     */
    public boolean deleteFiles(List<String> fileUrls) {
        List<DeleteObject> deleteObjects = fileUrls.stream()
                .map(this::extractObjectName)
                .map(DeleteObject::new)
                .toList();

        try {
            RemoveObjectsArgs args = RemoveObjectsArgs.builder()
                    .bucket(bucketName)
                    .objects(deleteObjects)
                    .build();

            Iterable<Result<DeleteError>> results = minioClient.removeObjects(args);
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                log.error("文件删除失败: {}, 错误: {}", error.objectName(), error.message());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("批量删除文件失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param objectName 对象名称（含路径）
     * @return 是否存在
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取目录下所有文件
     *
     * @param dir 目录路径
     * @return 文件名列表
     */
    public List<String> listFiles(String dir) {
        dir = dir.endsWith("/") ? dir : dir + "/";

        ListObjectsArgs args = ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(dir)
                .build();

        List<String> fileNames = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(args);

        for (Result<Item> result : results) {
            try {
                Item item = result.get();
                fileNames.add(item.objectName());
            } catch (Exception e) {
                log.error("获取文件列表失败: {}", e.getMessage(), e);
            }
        }

        return fileNames;
    }

    /**
     * 校验文件
     */
    private void validateFile(UploadEnum uploadEnum, MultipartFile file) {
        // 校验文件是否为空
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("上传文件不能为空");
        }

        // 校验文件大小
        double fileSizeMB = file.getSize() / (1024.0 * 1024.0);
        if (fileSizeMB > uploadEnum.getLimitSize()) {
            throw new FileUploadException("文件大小超过限制，最大允许 " + uploadEnum.getLimitSize() + "MB");
        }

        // 校验文件格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new FileUploadException("文件名不能为空");
        }

        boolean formatValid = uploadEnum.getFormat().stream()
                .anyMatch(ext -> originalFilename.toLowerCase().endsWith(ext));
        if (!formatValid) {
            throw new FileUploadException("不支持的文件格式，仅支持: " + uploadEnum.getFormat());
        }
    }

    /**
     * 获取文件后缀
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 从完整 URL 中提取对象名称
     */
    private String extractObjectName(String fileUrl) {
        // URL 格式: {endpoint}/{bucketName}/{objectName}
        String prefix = endpoint + "/" + bucketName + "/";
        if (fileUrl.startsWith(prefix)) {
            return fileUrl.substring(prefix.length());
        }
        // 如果传入的是相对路径，直接返回
        return fileUrl;
    }
}
