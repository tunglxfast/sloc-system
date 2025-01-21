package funix.sloc_system.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class MinioServiceTest {
    @Value("${minio.bucket}")
    private String bucket;

    @Mock
    private MinioClient minioClient;

    @Spy
    private MinioService minioService;

    @BeforeEach
    public void setUp() {
        minioService.setBucket(bucket);
    }

    @Test
    public void testUploadFile() throws Exception {
        doReturn(minioClient).when(minioService).getMinioClient();
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        doNothing().when(minioClient).makeBucket(any(MakeBucketArgs.class));
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        MockMultipartFile file = new MockMultipartFile("file", "file.pdf", "application/pdf", "file".getBytes());

        String url = minioService.uploadFile(file, "application/pdf");
        assertTrue(url.contains(bucket));
        verify(minioClient, Mockito.times(1)).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient, Mockito.times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    public void testUploadFile_BigFile() throws Exception {
        doReturn(minioClient).when(minioService).getMinioClient();
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        doNothing().when(minioClient).makeBucket(any(MakeBucketArgs.class));
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        MockMultipartFile file = new MockMultipartFile("file", "file.pdf", "application/pdf", new byte[6 * 1024 * 1024]);

        Exception exception = assertThrows(Exception.class,
                () -> minioService.uploadFile(file, "application/pdf"));
        assertTrue(exception.getMessage().contains("File size exceeds the maximum limit."));
    }

    @Test
    public void testUploadFile_BigVideo() throws Exception {
        doReturn(minioClient).when(minioService).getMinioClient();
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        doNothing().when(minioClient).makeBucket(any(MakeBucketArgs.class));
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        MockMultipartFile file = new MockMultipartFile("video", "file.mp4", "video/mp4", new byte[151 * 1024 * 1024]);

        Exception exception = assertThrows(Exception.class,
                () -> minioService.uploadFile(file, "application/pdf"));
        assertTrue(exception.getMessage().contains("File size exceeds the maximum limit."));
    }


    @Test
    public void testUploadFile_NotContentType() throws Exception {
        doReturn(minioClient).when(minioService).getMinioClient();
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        doNothing().when(minioClient).makeBucket(any(MakeBucketArgs.class));
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        MockMultipartFile file = new MockMultipartFile("video", "file.mp4", null, new byte[1]);

        Exception exception = assertThrows(Exception.class,
                () -> minioService.uploadFile(file, "application/pdf"));
        assertTrue(exception.getMessage().contains("File size exceeds the maximum limit."));
    }

    @Test
    public void testUploadFile_FileExtension() throws Exception {
        doReturn(minioClient).when(minioService).getMinioClient();
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        doNothing().when(minioClient).makeBucket(any(MakeBucketArgs.class));
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        MockMultipartFile file = new MockMultipartFile("file", "file", "application/pdf", "file".getBytes());

        String url = minioService.uploadFile(file, "application/pdf");
        assertTrue(url.contains(bucket));
        verify(minioClient, Mockito.times(1)).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient, Mockito.times(1)).putObject(any(PutObjectArgs.class));
    }
}