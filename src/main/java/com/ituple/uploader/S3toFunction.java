package com.ituple.uploader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.AliasConfiguration;
import com.amazonaws.services.lambda.model.CreateAliasRequest;
import com.amazonaws.services.lambda.model.CreateAliasResult;
import com.amazonaws.services.lambda.model.DeleteFunctionRequest;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListAliasesRequest;
import com.amazonaws.services.lambda.model.ListAliasesResult;
import com.amazonaws.services.lambda.model.ListVersionsByFunctionRequest;
import com.amazonaws.services.lambda.model.ListVersionsByFunctionResult;
import com.amazonaws.services.lambda.model.UpdateAliasRequest;
import com.amazonaws.services.lambda.model.UpdateAliasResult;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.ituple.uploader.constants.Response;
import com.ituple.uploader.constants.StatusCodes;

public class S3toFunction implements RequestStreamHandler {
	private static Connection connection = null;

	public void upload(InputStream inputStream, OutputStream outputStream, Context context) throws Exception {

		Response response = new Response();
		try {
			String payload = IOUtils.toString(inputStream, "UTF-8");
			Map<String, Object> payloadObject = (Map) new ObjectMapper().readValue(payload, Map.class);

			String bucketName = "";
			String bucketKey = "";
			String functionName = "";

			System.out.println(payload.toString());

			Map<String, String> mainPayload = (Map<String, String>) payloadObject.get("body");

			for (Entry<String, String> e : mainPayload.entrySet()) {

				if (e.getKey().toString().equals("bucket_name")) {
					bucketName = e.getValue().toString();
				} else if (e.getKey().toString().equals("bucket_key")) {
					bucketKey = e.getValue().toString();
				} else if (e.getKey().toString().equals("function_name")) {
					functionName = e.getValue().toString();
				}
			}

			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Bucket Name = " + bucketName);
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Bucket Key = " + bucketKey);

			AmazonS3 s3client = new AmazonS3Client();

			if (!s3client.doesBucketExist(bucketName) || !s3client.doesObjectExist(bucketName, bucketKey)) {
				response.setCode(StatusCodes.OK);
				response.setMessage("either bucket or object does not exist");
				response.setData(null);
			}

			else {

				ObjectMetadata objectMetadata = s3client.getObjectMetadata(bucketName, bucketKey);
				Map userMetadataMap = objectMetadata.getUserMetadata();

				System.out
						.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>metadata key" + userMetadataMap.keySet());

				String uploadToLambda = "" + userMetadataMap.get("function-name");
				String uploadFileName = "" + userMetadataMap.get("file-name");
				String deleteVersion = "" + userMetadataMap.get("delete-version-flag");
				String alias = "" + userMetadataMap.get("point-me");
				String description = "" + userMetadataMap.get("description");

				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>File Name = " + uploadFileName);
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Function Name = " + uploadToLambda);
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Alias Name = " + alias);
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Delete Version Flag = " + deleteVersion);
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Alias Description = " + description);

				upload(inputStream, bucketName, bucketKey, functionName, alias, deleteVersion, description);

				response.setCode(StatusCodes.OK);
				response.setMessage("object uploaded successfully");
				response.setData(null);
			}
		} catch (Exception exception) {
			response.setCode(StatusCodes.OK);
			response.setMessage("some exception occured " + exception);
			response.setData(null);
		}

		String responseString = new Gson().toJson(response);
		outputStream.write(responseString.getBytes());
	}

	private void upload(Object inputStream, String bucketName, String bucketKey, String functionName, String alias,
			String deleteFunctionFlag, String description) throws Exception {
		System.out.println("<---------------------------uploading starts-------------------------->");
		String version = null;
		AWSLambdaClient lambdaClient = new AWSLambdaClient();
		UpdateFunctionCodeRequest ufcr = new UpdateFunctionCodeRequest();

		System.out.println("Lambda Name -----------------------------------------------------> " + functionName);

		System.out.println("Bucket Name -----------------------------------------------------> " + bucketName);
		ufcr.withS3Bucket(bucketName);

		System.out.println(
				"Uploading jar file to -----------------------------------------------------> " + functionName);
		ufcr.withFunctionName(functionName);

		System.out.println("S3 key uploading -----------------------------------------------------> " + bucketKey);
		ufcr.withS3Key(bucketKey);

		ufcr.withPublish(true);

		UpdateFunctionCodeResult updateFunctionCodeResult = lambdaClient.updateFunctionCode(ufcr);

		version = updateFunctionCodeResult.getVersion();

		System.out.println("######################################### new version created : " + version);

		// updateAliasName(functionName, alias, description,
		// version,lambdaClient);
		// delete all the versions not ..................... :)
		// deleteFlag(functionName, alias, deleteFunctionFlag, version,
		// lambdaClient);
		// delete complete

		String request = inputStream.toString();

		System.out.println(request);

		System.out.println("<---------------------------@@uploaded successfully@@-------------------------->");
	}

	private void updateAliasName(String functionName, String alias, String description, String version,
			AWSLambdaClient lambdaClient) throws Exception {
		if (alias != null && !alias.isEmpty()) {
			updateAlias(functionName, version, alias, lambdaClient, description);
		}
	}

	private void deleteFlag(String functionName, String alias, String deleteFunctionFlag, String version,
			AWSLambdaClient lambdaClient) {
		if (deleteFunctionFlag.equals("true")) {

			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>deleteFunctionFlag.equals(\"true\") ");

			List<Map<String, String>> listAliasAndMappedFunction = listAliasAndMappedFunction(lambdaClient,
					functionName, alias, version);

			List<String> qualifierExclude = new ArrayList<>();
			for (Map<String, String> eachAliasAndMappedFunction : listAliasAndMappedFunction) {
				qualifierExclude.add(eachAliasAndMappedFunction.get("version"));
			}

			deleteFunctionVersion(lambdaClient, functionName, qualifierExclude);
		}
	}

	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * @param functionName
	 * @param version
	 * @param alias
	 * @param lambdaClient
	 * @throws Exception
	 */
	public void updateAlias(String functionName, String version, String alias, AWSLambdaClient lambdaClient,
			String description) throws Exception {

		if (!aliasExists(lambdaClient, functionName, alias)) {

			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>alias does not exist and Creating Alias");
			createAlias(lambdaClient, functionName, alias, version);

			if (!aliasExists(lambdaClient, functionName, alias)) {
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>alias neither available nor be created");
				throw new Exception("alias missing and could not be created");
			}
		}

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>inside updateAlias");
		UpdateAliasRequest updateAliasRequest = new UpdateAliasRequest();

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Pointing Alias : " + functionName);
		updateAliasRequest.withName(alias);
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Update Alias Description : " + description);
		updateAliasRequest.withDescription(description);
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>to Version : " + version);
		updateAliasRequest.withFunctionVersion(version);
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>for Function : " + functionName);
		updateAliasRequest.withFunctionName(functionName);
		UpdateAliasResult updateAliasResult = lambdaClient.updateAlias(updateAliasRequest);

	}

	/**
	 * return boolean result true if Alias exists else false
	 * 
	 * 
	 * @param lambdaClient
	 * @param functionName
	 * @param alias
	 * @return
	 */
	public boolean aliasExists(AWSLambdaClient lambdaClient, String functionName, String alias) {
		ListAliasesRequest listAliasRequest = new ListAliasesRequest();

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>for Function : " + functionName);
		listAliasRequest.withFunctionName(functionName);

		ListAliasesResult listAliasResult = lambdaClient.listAliases(listAliasRequest);

		List<AliasConfiguration> listAliasConfiguration = listAliasResult.getAliases();

		for (AliasConfiguration aliasConfiguration : listAliasConfiguration) {
			if (aliasConfiguration.getName().equals(alias)) {
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Alias exists");
				return true;
			}
		}
		return false;
	}

	/**
	 * return a List containing Alias and corresponding Version
	 * 
	 * 
	 * @param lambdaClient
	 * @param functionName
	 * @param alias
	 * @param version
	 * @return
	 */
	public List<Map<String, String>> listAliasAndMappedFunction(AWSLambdaClient lambdaClient, String functionName,
			String alias, String version) {

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>starts listAliasAndMappedFunction");

		List<Map<String, String>> listAliasFunction = new ArrayList<>();

		ListAliasesRequest listAliasRequest = new ListAliasesRequest();

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>for Function : " + functionName);
		listAliasRequest.withFunctionName(functionName);

		ListAliasesResult listAliasResult = lambdaClient.listAliases(listAliasRequest);

		List<AliasConfiguration> listAliasConfiguration = listAliasResult.getAliases();

		for (AliasConfiguration aliasConfiguration : listAliasConfiguration) {

			Map<String, String> mapAliasVersion = new HashMap<>();

			mapAliasVersion.put("alias", aliasConfiguration.getName());
			mapAliasVersion.put("version", aliasConfiguration.getFunctionVersion());

			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>alias = " + aliasConfiguration.getName()
					+ "    version = " + aliasConfiguration.getFunctionVersion());

			listAliasFunction.add(mapAliasVersion);
		}
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>ends listAliasAndMappedFunction");
		return listAliasFunction;
	}

	/**
	 * list all the versions of a function for given functionName
	 * 
	 * 
	 * @param lambdaClient
	 * @param functionName
	 * @param qualifierExclude
	 * @return
	 */
	public List<String> listFunctionVersion(AWSLambdaClient lambdaClient, String functionName) {

		List<String> listFunctionVersion = new ArrayList<>();

		ListVersionsByFunctionRequest listVersionByFunctionRequest = new ListVersionsByFunctionRequest()
				.withFunctionName(functionName);

		ListVersionsByFunctionResult listVersionByFunctionResult = lambdaClient
				.listVersionsByFunction(listVersionByFunctionRequest);

		List<FunctionConfiguration> listFunctionConfiguration = listVersionByFunctionResult.getVersions();

		for (FunctionConfiguration eachFunctionConfiguration : listFunctionConfiguration) {
			String version = eachFunctionConfiguration.getVersion();

			listFunctionVersion.add(version);

		}

		return listFunctionVersion;
	}

	/**
	 * delete the all the function version and exclude qualifierExclude versions
	 * only
	 * 
	 * @param lambdaClient
	 * @param functionName
	 * @param qualifierExclude
	 */
	public void deleteFunctionVersion(AWSLambdaClient lambdaClient, String functionName,
			List<String> qualifierExclude) {

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>starts deleteFunctionVersion");
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>qalifier exclude" + qualifierExclude);

		List<String> listFunctionVersion = listFunctionVersion(lambdaClient, functionName);

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>List Function Version : " + listFunctionVersion);

		for (String qualifier : listFunctionVersion) {
			if (!qualifierExclude.contains(qualifier) && !qualifier.equals("$LATEST")) {

				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>deleting qalifier " + qualifier
						+ " in function " + functionName);

				lambdaClient.deleteFunction(
						new DeleteFunctionRequest().withFunctionName(functionName).withQualifier(qualifier));
			}
		}

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>ends deleteFunctionVersion");
	}

	/**
	 * create an Alias and attach it to given functionVersion
	 * 
	 * 
	 * @param lambdaClient
	 * @param functionName
	 * @param createAliasName
	 * @param functionVersion
	 */
	public void createAlias(AWSLambdaClient lambdaClient, String functionName, String createAliasName,
			String functionVersion) {
		// alias name "DEV,QA,PROD"
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>creating alias");
		CreateAliasRequest createAliasRequest = new CreateAliasRequest();
		createAliasRequest.withFunctionName(functionName);
		createAliasRequest.withName(createAliasName);
		createAliasRequest.withFunctionVersion(functionVersion);
		CreateAliasResult createAliasResut = lambdaClient.createAlias(createAliasRequest);
	}

	/**
	 * *
	 * <p>
	 * Deletes the specified Lambda function code and configuration.
	 * </p>
	 * <p>
	 * If you are using the versioning feature and you don't specify a function
	 * version in your <code>DeleteFunction</code> request, AWS Lambda will
	 * delete the function, including all its versions, and any aliases pointing
	 * to the function versions. To delete a specific function version, you must
	 * provide the function version via the <code>Qualifier</code> parameter.
	 * 
	 * 
	 * @param lambdaClient
	 * @param functionName
	 * @param qualifier
	 */
	public void deleteVersion(AWSLambdaClient lambdaClient, String functionName, String qualifier) {
		DeleteFunctionRequest deleteFunctioRequest = new DeleteFunctionRequest();
		deleteFunctioRequest.withFunctionName(functionName);
		if (qualifier != null && !qualifier.isEmpty()) {
			System.out.println(
					"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Function Qaulifier is not empty and deleteting qualifier "
							+ qualifier);
			deleteFunctioRequest.withQualifier(qualifier);
		}
		lambdaClient.deleteFunction(deleteFunctioRequest);
	}

	public void listKeys(String bucketName) throws Exception {

		// String bucketName = "aws-build-ioanyt";

		AmazonS3 s3client = new AmazonS3Client();

		try {
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Listing objects");
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

					// String uploadLambdaName =
					// getUploadLambda(userMetadataMap, "upload-to-lambda");

					Set set1 = userMetadataMap.entrySet();

					// check set values
					System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Set values: " + set1);

					Map rowMetadataMap = objectMetadata.getRawMetadata();
					System.out.println(
							"##################################################################################################################"
									+ rowMetadataMap);
					Set set2 = rowMetadataMap.entrySet();
					// check set values
					System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Set values: " + set2);

				}
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Next Continuation Token : "
						+ result.getNextContinuationToken());
				req.setContinuationToken(result.getNextContinuationToken());
			} while (result.isTruncated() == true);

		} catch (AmazonServiceException ase) {
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Caught an AmazonServiceException, "
					+ "which means your request made it " + "to Amazon S3, but was rejected with an error response "
					+ "for some reason.");
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Error Message:    " + ase.getMessage());
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>HTTP Status Code: " + ase.getStatusCode());
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>AWS Error Code:   " + ase.getErrorCode());
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Error Type:       " + ase.getErrorType());
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Caught an AmazonClientException, "
					+ "which means the client encountered " + "an internal error while trying to communicate"
					+ " with S3, " + "such as not being able to access the network.");
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~>Error Message: " + ace.getMessage());
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
