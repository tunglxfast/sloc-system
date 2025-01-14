package funix.sloc_system.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

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

    private static final long MAX_PDF_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_VIDEO_SIZE = 150 * 1024 * 1024; // 150MB

    private MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint(minioEndpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public String uploadFile(MultipartFile file, String contentType) throws Exception {
        if (!checkFileSize(file)) {
            throw new Exception("File size exceeds the maximum limit.");
        }

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

        // Generate presigned URL for file access if secure
//        String url = minioClient.getPresignedObjectUrl(
//            GetPresignedObjectUrlArgs.builder()
//                .bucket(bucket)
//                .object(filename)
//                .method(Method.GET)
//                .expiry(7, TimeUnit.DAYS)
//                .build()
//        );

        // Public file url
        String url = minioEndpoint + "/" + bucket + "/" + filename;

        return url;
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex);
    }

    private boolean checkFileSize(MultipartFile file) throws Exception {
        String contentType = file.getContentType();

        // 5MB for PDF and 150MB for video
        if (contentType == null) {
            return false;
        } else if ("application/pdf".equals(contentType)) {
            if (file.getSize() > MAX_PDF_SIZE) {
                return false;
            }
        } else if (contentType.startsWith("video/")) {
            if (file.getSize() > MAX_VIDEO_SIZE) {
                return false;
            }
        }
        return true;
    }
} 