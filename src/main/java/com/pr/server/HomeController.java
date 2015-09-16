package com.pr.server;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import com.pr.server.services.AWSImageService;
import com.amazonaws.services.sqs.model.Message;
/**
 * Handles requests for the application home page.
 */
@Controller
@RequestMapping(value = { "/", " * ", "home" })
public class HomeController {
	private static final String SUFFIX = "/";
	AWSImageService service;
//	private List<ImageFile> s3Files = new LinkedList<ImageFile>();
	private String message = "";
	private List<String> s3keys;
	private String selectedKey = "";
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	//motko bosko nukleorno
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/")
	public void home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		if(service==null){service=new AWSImageService();}
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		service.initService();
		model.addAttribute("serverTime", formattedDate );
		
		while (true) {try{
			  List<Message> msgs = service.getMessages();

			  if (msgs.size() > 0) {
			   for(Message m:msgs){
				   String processedFilename=service.convertImg(m.getBody());
			   System.out.println("The message is " + m.getBody());
			   logger.info("Wiadomość :", m.getBody());
			   logger.info(" url :",processedFilename);
			   service.delRecivedMessage(m);
			   logger.info("Usunięto wiadomość z kolejki :",processedFilename);
			   }
			  } else {
			    System.out.println("nothing found, trying again in 30 seconds");
			    Thread.sleep(3000); 
			  }
			}catch(Exception e){
				logger.info("Wiadomość :", e.getMessage());
			}
		
		}
		
	//	return "home";
	}
	
}
