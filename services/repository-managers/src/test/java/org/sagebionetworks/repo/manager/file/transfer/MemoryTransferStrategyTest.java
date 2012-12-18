package org.sagebionetworks.repo.manager.file.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.file.S3FileMetadata;
import org.sagebionetworks.repo.util.FixedMemoryPool;
import org.sagebionetworks.repo.util.FixedMemoryPool.BlockConsumer;
import org.sagebionetworks.repo.util.FixedMemoryPool.NoBlocksAvailableException;
import org.sagebionetworks.repo.web.ServiceUnavailableException;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.amazonaws.util.BinaryUtils;
import com.amazonaws.util.StringInputStream;

/**
 * Unit test for MemoryTransferStrategy
 * 
 * @author jmhill
 *
 */
public class MemoryTransferStrategyTest {
	
	
	public static final int FIVE_MB = ((int) Math.pow(2, 20))*5;
	public static final double NANOSECONDS_PER_MILLISECOND = 1000000;
	
	AmazonS3Client mockS3Client;
	FixedMemoryPool memoryPool;
	MemoryTransferStrategy strategy;
	TransferRequest transferRequest;
	InputStream inputStream;
	String inputStreamContent;
	String expectedMD5;
	Long expectedContentLength;
	byte[] block;
	
	@Before
	public void before() throws UnsupportedEncodingException, NoSuchAlgorithmException{
		mockS3Client = Mockito.mock(AmazonS3Client.class);
		memoryPool = Mockito.mock(FixedMemoryPool.class);
		strategy = new MemoryTransferStrategy(mockS3Client, memoryPool);
		inputStreamContent = "This will be our simple stream";
		// Calculate the expected MD5 of the content.
		MessageDigest md5Digets =  MessageDigest.getInstance("MD5");
		byte[] contentBytes = inputStreamContent.getBytes();
		expectedContentLength = new Long(contentBytes.length);
		md5Digets.update(contentBytes);
		expectedMD5 = BinaryUtils.toHex(md5Digets.digest());
		
		inputStream = new StringInputStream(inputStreamContent);
		transferRequest = new TransferRequest();
		transferRequest.setS3bucketName("bucket");
		transferRequest.setS3key("key");
		transferRequest.setInputStream(inputStream);
		transferRequest.setContentType("contentType");
		transferRequest.setContentMD5(null);
		transferRequest.setFileName("foo.txt");
		// How long does it take to allocate 5 MB?
		int blockSize = ((int) Math.pow(2, 20))*5;
		long start = System.nanoTime();
		block = new byte[blockSize];
		double elapseMS = ((double)(System.nanoTime() - start))/NANOSECONDS_PER_MILLISECOND;
		System.out.println("Allocated 5 MB block in "+elapseMS+" MS");
		
		// Setup the S3 calls
		InitiateMultipartUploadResult result = new InitiateMultipartUploadResult();
		result.setBucketName(transferRequest.getS3bucketName());
		result.setKey(transferRequest.getS3key());
		when(mockS3Client.initiateMultipartUpload(any(InitiateMultipartUploadRequest.class))).thenReturn(result);
		UploadPartResult uploadPart = new UploadPartResult();
		uploadPart.setETag("etag");
		when(mockS3Client.uploadPart(any(UploadPartRequest.class))).thenReturn(uploadPart);
		
	}
	
	/**
	 * Since the minimum size of a of an S3 multi-part upload part is 5 MB we need to ensure that a block of at least that size is
	 * provided.
	 * 
	 * @throws IOException
	 */
	@Test (expected=IllegalArgumentException.class)
	public void testTransferToS3BlockSizeTooSmall() throws IOException{
		// Use a block size 5MB - 1
		int blockSize = ((int) Math.pow(2, 20))*5 - 1;
		block = new byte[blockSize];
		strategy.transferToS3(transferRequest, block);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testTransferToS3RequestNull() throws IOException{
		// Pass a null request
		strategy.transferToS3(null, block);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testTransferToS3BufferNull() throws IOException{
		strategy.transferToS3(transferRequest, null);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testTransferToS3RequestNullBucket() throws IOException{
		transferRequest.setS3bucketName(null);
		strategy.transferToS3(transferRequest, block);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testTransferToS3RequestNullKey() throws IOException{
		transferRequest.setS3key(null);
		strategy.transferToS3(transferRequest, block);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testTransferToS3RequestNullInput() throws IOException{
		transferRequest.setInputStream(null);
		strategy.transferToS3(transferRequest, block);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testTransferToS3RequestNullName() throws IOException{
		transferRequest.setFileName(null);
		strategy.transferToS3(transferRequest, block);
	}
	
	@Test
	public void testTransferToS3() throws IOException{
		// This call should work and fill in all of the data based on what is passed.
		S3FileMetadata meta = strategy.transferToS3(transferRequest, block);
		assertNotNull(meta);
		assertEquals(transferRequest.getS3bucketName(), meta.getBucketName());
		assertEquals(transferRequest.getS3key(), meta.getKey());
		assertEquals(expectedMD5, meta.getContentMd5());
		assertEquals(expectedContentLength, meta.getContentSize());
		assertEquals(transferRequest.getContentType(), meta.getContentType());
		assertEquals(transferRequest.getFileName(), meta.getFileName());
	}
	
	/**
	 * When the memory pool is exhausted, a ServiceUnavailableException should be thrown.
	 * @throws Exception 
	 */
	@Test (expected=ServiceUnavailableException.class)
	public void testTransferToS3PoolExhausted() throws Exception{
		// Simulate the case where the memory pool is exhausted.
		when(memoryPool.checkoutAndUseBlock(any(BlockConsumer.class))).thenThrow(new NoBlocksAvailableException());
		strategy.transferToS3(transferRequest);
	}

}
