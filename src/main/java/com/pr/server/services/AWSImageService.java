package com.pr.server.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.hibernate.procedure.internal.PostgresCallableStatementSupport;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

import com.pr.server.services.ImageConverter;
import com.pr.server.services.AWSImageService;

//http://www.javacodegeeks.com/2013/06/working-with-amazon-simple-queue-service-using-java.html
public class AWSImageService {


	public AWSImageService() {
		super();
		initService();
		// TODO Auto-generated constructor stub
	}

	private ClientConfiguration serviceConfig;
	private String msgSended = "send";
	private String msgRecicved = "recived";
	private String msgProccessed = "processed";
	private final String SUFFIX = "/";
	AWSCredentials credentials;
	AmazonS3 s3client;
	Bucket bucket;
	private ObjectListing myObjectList;
	private String processingServiceAdress;
	private String userlogin = "nitager";
	private String secretKey = "***";
	private String accesIDKey = "***";
	private String username = "piotr.rodziewicz";
	private String bucketName = "s3-piotr-rodziewicz-kci";
	private String sqsName = "prodziewicz_kci_SQS";
	private String procesorAdress = "";
	private List<String> keys;
	private AmazonSQSClient sqsClient;
	private String sqsFullUrl = "https://sqs.us-west-2.amazonaws.com/983680736795/prodziewicz_kci_SQS";
	private String sqsUrlRegion = "https://sqs.us-west-2.amazonaws.com/983680736795/";
	private static volatile AWSImageService awsService = new AWSImageService();

	// a AWSImageService(){initService();}
	public void initService() {

		credentials = new BasicAWSCredentials(accesIDKey, secretKey);
		s3client = new AmazonS3Client(credentials);
		sqsClient = new AmazonSQSClient(credentials);
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		sqsClient.setRegion(usWest2);
		sqsClient.setEndpoint(sqsUrlRegion);
	}
	public boolean isExistSQS(){
		
		
		
		
		return false;
	}
	public void sendMessageToQueue(String queueUrl, String message)  {
		this.initService();
		Message msg=new Message();
		msg.setBody(message);
		
		SendMessageRequest smr=new SendMessageRequest();
		smr.setRequestCredentials(credentials);
		smr.setQueueUrl(queueUrl);
		smr.setMessageBody(message);
		SendMessageResult messageResult = this.sqsClient.sendMessage(smr);
		System.out.println(messageResult.toString());
	}

	public List<Message> getMessages() throws Exception {
		List<Message> messages = getMessagesFromQueue(sqsName);
		return messages;
	}
	public void delRecivedMessage(Message msg){ 
		deleteMessageFromQueue(sqsFullUrl, msg);
	}
		
	
public void delRecivedMessage(String msg, List<Message> src){ 
	
	DeleteMessageRequest delrequest;
	if (sqsClient == null) {
		this.initService();
	}
	for (Message m : src) {
		if(m.getBody()==msg){
		delrequest = new DeleteMessageRequest().withQueueUrl(sqsFullUrl).withQueueUrl(m.getReceiptHandle());
		sqsClient.deleteMessage(delrequest);
		}else{ 
			
		}
	}
	
}
	public void delRecivedMessages(List<Message> src) throws Exception {
		DeleteMessageRequest delrequest;
		if (sqsClient == null) {
			this.initService();
		}
		for (Message m : src) {

			delrequest = new DeleteMessageRequest().withQueueUrl(sqsFullUrl).withQueueUrl(m.getReceiptHandle());
			sqsClient.deleteMessage(delrequest);

		}
	}
	public List<Message> getSQSMesssages(){
			List<Message> msgs=new LinkedList<Message>();
			msgs=getMessagesFromQueue(sqsFullUrl);
			
			return msgs;
			}
	public List<Message> getMessagesFromQueue(String queueUrl) {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
		List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).getMessages();
		return messages;
	}

	public ListQueuesResult listQueues() {
		this.initService();
		return this.sqsClient.listQueues();
	}
//	public void listQueus(){
//		ListQueuesResult sqsqueues=listQueues();
//		for(String Url : sqsqueues.getQueueUrls()){
//			
//		}
	public void sendSQSMsg(String msg) {
		//String msg;
		sendMessageToQueue(sqsFullUrl, msg);
	}
		public List<String> listQueus() throws Exception{
			ListQueuesResult sqsqueues=new ListQueuesResult();
			sqsqueues=listQueues();
			return sqsqueues.getQueueUrls();
			
	}
	public void deleteMessageFromQueue(String queueUrl, Message message) {
		String messageRecieptHandle = message.getReceiptHandle();
		System.out.println("message deleted : " + message.getBody() + "." + message.getReceiptHandle());
		sqsClient.deleteMessage(new DeleteMessageRequest(queueUrl, messageRecieptHandle));
	}

	public S3Object getImageByName(String s3key) throws FileNotFoundException {
		S3Object obj = s3client.getObject(bucketName, s3key);
		// String ext=FilenameUtils.getExtension(s3key);

		return obj;
	}

	public String convertImg(String s3key) throws Exception {
		// TODO Auto-generated method stub
		String extension;
		String prefix = "nitager";
		String s3prefix = prefix + "/";
		String imagename = new StringBuilder(s3key.replace(s3prefix, "")).toString();
		extension = FilenameUtils.getExtension(imagename);
		String processedImageName = new StringBuilder("processed_by_sqs" + imagename).toString();
		BufferedImage image = getImageFromS3(bucketName, s3key);

		image = ImageConverter.reverseImage(image);
		// Creating the directory to store file
		String rootPath = System.getProperty("catalina.home");
		File dir = new File(rootPath + File.separator + "tmpFiles");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		// Create the file on server
		File serverFile = new File(dir.getAbsolutePath() + File.separator + processedImageName);

		processedImageName = new StringBuilder(prefix + SUFFIX + processedImageName).toString();

		ImageIO.write(image, extension, serverFile);
		putImageToS3(serverFile, processedImageName);
		return new StringBuilder(serverFile.getAbsolutePath()).toString();

	}

	public void convert(String s3key) throws Exception {
		// TODO Auto-generated method stub
		String extension;
		String prefix = "nitager";
		String s3prefix = "/" + prefix + "/";
		String imagename = new StringBuilder(s3key.replace(s3prefix, "")).toString();
		extension = FilenameUtils.getExtension(imagename);
		String processedImageName = new StringBuilder("processed_" + imagename).toString();
		BufferedImage image = getImageFromS3(bucketName, s3key);

		image = ImageConverter.reverseImage(image);
		// Creating the directory to store file
		String rootPath = System.getProperty("catalina.home");
		File dir = new File(rootPath + File.separator + "tmpFiles");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		// Create the file on server
		File serverFile = new File(dir.getAbsolutePath() + File.separator + processedImageName);

		processedImageName = new StringBuilder(prefix + SUFFIX + processedImageName).toString();

		ImageIO.write(image, extension, serverFile);
		putImageToS3(serverFile, processedImageName);

	}

	private BufferedImage getImageFromS3(String bucket, String key) throws Exception {
		// TODO Auto-generated method stub
		if (this.s3client == null) {
			initService();
		}

		S3Object object = s3client.getObject(bucket, key);
		System.out.println("Downloaded image: " + key);

		File file_temp = new File("temporary.jpg");
		if (!file_temp.exists()) {
			file_temp.createNewFile();
		}

		InputStream in = object.getObjectContent();
		byte[] buf = new byte[1024];
		OutputStream out = new FileOutputStream(file_temp);
		int count;
		while ((count = in.read(buf)) != -1) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			out.write(buf, 0, count);
		}
		out.close();
		in.close();

		File image = file_temp;
		BufferedImage in2 = ImageIO.read(image);
		return in2;
	}

	public void getFiles() {

	}

	public List<String> getKeyList() {
		getKeys();
		return this.keys;

	}

	public void getKeys() {
		this.keys = new LinkedList<String>();
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName)
				.withPrefix("nitager");

		ObjectListing objectListing;

		do {
			objectListing = s3client.listObjects(listObjectsRequest);
			for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
				String key = objectSummary.getKey();

				if (key.endsWith(".jpg") | key.endsWith(".jpeg") | key.endsWith(".png") | key.endsWith(".bmp")) {
					this.keys.add(new StringBuilder(key).toString());
				}
			}
			listObjectsRequest.setMarker(objectListing.getNextMarker());
		} while (objectListing.isTruncated());

		this.myObjectList = objectListing;

	}

	

	public List<S3ObjectSummary> getUserImagesS3URL() {
		// foldery na buckecie nazywane są od loginu użytkownika
		List<S3ObjectSummary> fileList = s3client.listObjects(bucketName, username).getObjectSummaries();
		return fileList;
	}

	public void putImageToS3Public(File file, String s3FileName) {
		try {
			s3client.putObject(new PutObjectRequest(bucketName, s3FileName, file)
					.withCannedAcl(CannedAccessControlList.PublicRead));
			;
		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which " + "means your request made it "
					+ "to Amazon S3, but was rejected with an error response" + " for some reason.");
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which " + "means the client encountered "
					+ "an internal error while trying to " + "communicate with S3, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}

	public void createFolder(String bucketName, String folderName, AmazonS3 client) {
		// create meta-data for your folder and set content-length to 0
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(0);
		// create empty content
		InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
		// create a PutObjectRequest passing the folder name suffixed by /
		PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, folderName + SUFFIX, emptyContent,
				metadata);
		// send request to S3 to create folder
		client.putObject(putObjectRequest);
	}

	public void putImageToS3(File file, String s3FileName) {
		try {
			s3client.putObject(new PutObjectRequest(bucketName, s3FileName, file)
					.withCannedAcl(CannedAccessControlList.PublicRead));
			;
		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which " + "means your request made it "
					+ "to Amazon S3, but was rejected with an error response" + " for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which " + "means the client encountered "
					+ "an internal error while trying to " + "communicate with S3, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}

	public void deleteImage(String name) {
		s3client.deleteObject(bucketName, name);

	}

	public void deleteFolder(String bucketName, String folderName, AmazonS3 client) {
		List<S3ObjectSummary> fileList = client.listObjects(bucketName, folderName).getObjectSummaries();

		for (S3ObjectSummary file : fileList) {
			client.deleteObject(bucketName, file.getKey());
		}
		client.deleteObject(bucketName, folderName);
	}

}
