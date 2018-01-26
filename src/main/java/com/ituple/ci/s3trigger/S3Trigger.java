package com.ituple.ci.s3trigger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3Trigger implements RequestStreamHandler {
	private static Connection connection = null;

	public Object customHandleRequest(Object inputStream, Context context) throws Exception {

//		String requestAsString = IOUtils.toString((InputStream) inputStream, "UTF-8");
//		LambdaTriggerDTO lambdaTriggerDTO = new ObjectMapper().readValue(requestAsString, LambdaTriggerDTO.class);

		LinkedHashMap<String,Object> lambdaTrigger=(LinkedHashMap<String, Object>) inputStream;
		
		Set set=lambdaTrigger.entrySet();
		System.out.println("set =   "+set);
		
		Map<String,Object> data=(Map<String, Object>) lambdaTrigger.get("Records");
		
		System.out.println("key set =  "+data.keySet());
		
		// AWSLambdaClient lambdaClient = new AWSLambdaClient();
		// UpdateFunctionCodeRequest ufcr = new UpdateFunctionCodeRequest();
		// String bucketName = "aws-build-ioanyt";
		//
		// listKeys(bucketName);
		//
		// ufcr.withS3Bucket(bucketName);
		//
		// ufcr.withFunctionName("addDevice");
		//
		// ufcr.withS3Key("gps/development/paymentgateway/paymentgatewaycallback/paymentgatewaycallbacklambda-latest.jar");
		//
		// lambdaClient.updateFunctionCode(ufcr);
		//
		// String request = inputStream.toString();
		//
		//
		// System.out.println(request);
		return null;
	}

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		// TODO Auto-generated method stub

	}

	public void listKeys(String bucketName) throws Exception {

		// String bucketName = "aws-build-ioanyt";

		AmazonS3 s3client = new AmazonS3Client();

		try {
			System.out.println("Listing objects");
			final ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(2);
			ListObjectsV2Result result;
			do {
				result = s3client.listObjectsV2(req);

				for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
					System.out.println(
							" - " + objectSummary.getKey() + "  " + "(size = " + objectSummary.getSize() + ")");
					ObjectMetadata objectMetadata = s3client.getObjectMetadata(bucketName, objectSummary.getKey());
					Map userMetadataMap = objectMetadata.getUserMetadata();
					System.out.println(
							"##################################################################################################################"
									+ userMetadataMap);

					String uploadLambdaName = getUploadLambda(userMetadataMap, "upload-to-lambda");

					Set set1 = userMetadataMap.entrySet();

					// check set values
					System.out.println("Set values: " + set1);

					Map rowMetadataMap = objectMetadata.getRawMetadata();
					System.out.println(
							"##################################################################################################################"
									+ rowMetadataMap);
					Set set2 = rowMetadataMap.entrySet();
					// check set values
					System.out.println("Set values: " + set2);

				}
				System.out.println("Next Continuation Token : " + result.getNextContinuationToken());
				req.setContinuationToken(result.getNextContinuationToken());
			} while (result.isTruncated() == true);

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, " + "which means your request made it "
					+ "to Amazon S3, but was rejected with an error response " + "for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, " + "which means the client encountered "
					+ "an internal error while trying to communicate" + " with S3, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}

	private String getUploadLambda(Map userMetadataMap, String key) throws Exception {

		if (userMetadataMap != null && userMetadataMap.containsKey(key) && !("" + userMetadataMap.get(key)).isEmpty())
			return "" + userMetadataMap.get(key);

		else
			throw new Exception("upload-to-lambda key missing");

		// TODO Auto-generated method stub

	}

}
