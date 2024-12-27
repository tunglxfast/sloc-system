package funix.sloc_system.service;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {
    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucket;

    private MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint(minioEndpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public String uploadFile(MultipartFile file, String contentType) throws Exception {
        // Generate unique filename
        String filename = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
        
        // Create bucket if it doesn't exist
        MinioClient minioClient = getMinioClient();
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }

        // Upload file
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(filename)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(contentType)
                    .build()
            );
        }

        // Generate presigned URL for file access
        String url = minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .bucket(bucket)
                .object(filename)
                .method(Method.GET)
                .expiry(7, TimeUnit.DAYS)
                .build()
        );

        return url;
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex);
    }
} 