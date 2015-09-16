package com.pr.server;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.model.Message;
import com.pr.server.services.AWSImageService;

public class ServiceThread implements Runnable{
	private static final String SUFFIX = "/";
	AWSImageService service;
//	private List<ImageFile> s3Files = new LinkedList<ImageFile>();
	private String message = "";
	private List<String> s3keys;
	private String selectedKey = "";
	private static final Logger logger = LoggerFactory.getLogger(ServiceThread.class);
	
	
	public void run(){
		if(service==null){service=new AWSImageService();}
	while(true){
		try{
		//Pobranie kluczy do przeróbki z kolejki
		List<Message> s3KeyList=service.getMessages();
		for(Message m : s3KeyList){
			String imgkey=m.getBody();
			logger.info("Image key processed : ", imgkey);
		//	String fileUrl = service.convertImg(imgkey);
			
			
		}
		
		
		
	}catch(Exception e){  logger.info("Wyjątek : ", e.toString());}
	finally{
	
	}}
	}
}
