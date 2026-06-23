package com.steven.solomon.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.steven.solomon.enums.StorageCapability;
import com.steven.solomon.graphics2D.entity.FileUpload;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

class AbstractFileServiceMetricsTest {

    @Test
    void shouldRecordStorageOperationMetrics() throws Exception {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        TestFileService service = new TestFileService();
        service.setMeterRegistry(meterRegistry);

        String result = service.trackedOperation("bucket-a");

        assertThat(result).isEqualTo("ok");
        assertThat(meterRegistry.counter(
                "solomon.s3.operation.total",
                "provider", "TestFileService",
                "operation", "test",
                "bucket", "bucket-a",
                "outcome", "success").count()).isEqualTo(1.0);
        assertThat(meterRegistry.timer(
                "solomon.s3.operation.duration",
                "provider", "TestFileService",
                "operation", "test",
                "bucket", "bucket-a",
                "outcome", "success").count()).isEqualTo(1);
    }

    private static class TestFileService extends AbstractFileService {

        String trackedOperation(String bucketName) throws Exception {
            return recordStorageOperation("test", bucketName, () -> "ok");
        }

        @Override
        public Set<StorageCapability> capabilities() {
            return EnumSet.allOf(StorageCapability.class);
        }

        @Override
        protected void multipartUpload(MultipartFile file, String bucketName, long fileSize, String uploadId,
                                       String filePath, int partCount) {
        }

        @Override
        protected void upload(MultipartFile file, String bucketName, String filePath) {
        }

        @Override
        protected void delete(String bucketName, String filePath) {
        }

        @Override
        protected String shareUrl(String bucketName, String filePath, long expiry) {
            return "https://example.test/" + filePath;
        }

        @Override
        protected InputStream getObject(String bucketName, String filePath) {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        protected void createBucket(String bucketName) {
        }

        @Override
        public boolean bucketExists(String bucketName) {
            return true;
        }

        @Override
        protected boolean checkObjectExist(String bucketName, String objectName) {
            return true;
        }

        @Override
        protected void copyFile(String sourceBucket, String targetBucket, String sourceObjectName,
                                String targetObjectName) {
        }

        @Override
        protected void abortMultipartUpload(String uploadId, String bucketName, String filePath) {
        }

        @Override
        protected String initiateMultipartUploadTask(String bucketName, String objectName) {
            return "upload-id";
        }

        @Override
        public void deleteBucket(String bucketName) {
        }

        @Override
        public List<String> getBucketList() {
            return List.of("bucket-a");
        }

        @Override
        public List<String> listObjects(String bucketName, String key) {
            return List.of();
        }
    }
}
