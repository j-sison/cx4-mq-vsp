package com.jay.mqconsumer.web;

import static com.jay.mqconsumer.business.logic.util.MqSenderUtil.convertMsgToList;

import com.ibm.mq.MQException;

import com.jay.mqconsumer.business.logic.util.MqManagerConnection;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.MediaType;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@Controller
public class MQController
{
	//~ Instance fields --------------------------
	/**  */
	@Value("${cust.mq.channel}")
	private String channel;

	/**  */
	@Value("${cust.mq.host}")
	private String host;

	/**  */
	@Value("${cust.mq.manager}")
	private String manager;

	/**  */
	@Value("${cust.mq.port}")
	private int port;

	/**  */
	@Value("${cust.mq.queue}")
	private String queue;
	//~ Methods ----------------------------------
	/*
	 * public String test(@RequestParam(     name = "name",     required = false,     defaultValue = "World" ) String
	 * name, Model model)
	 */
	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 * @throws  MQException
	 * @throws  IOException
	 * @throws  NumberFormatException
	 */
	@GetMapping("/displayMessage")
	public String displayMessages(@RequestParam
		Map<String, String> allParams, Model model) throws IOException, NumberFormatException, MQException
	{
		MqManagerConnection mq = MqManagerConnection.connect(allParams.get("host"), allParams.get("manager"),
				allParams.get("channel"), Integer.parseInt(allParams.get("port")), allParams.get("queue"));

		List<String> messages = retrieveMqMSGS(mq);

		model.addAttribute("messages", messages);
		model.addAttribute("total", messages.size());

		mq.revert();
		mq.disconnect();

		return "displayMessages";
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @param   mq
	 * @param   messages
	 * @return
	 * @throws  IOException
	 */
	private List<String> retrieveMqMSGS(MqManagerConnection mq) throws IOException
	{
		List<String> messages = new ArrayList<>();

		boolean keepReading = true;

		while (keepReading)
		{
			String message = null;
			try
			{
				message = mq.read();
			}
			catch (MQException e)
			{
				if (e.reasonCode == 2033)
				{
					// LOGGER.trace("No more MQ message found");
				}
				keepReading = false;
			}
			if (message != null)
			{
				messages.add(message);
			}
		}

		return messages;
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @param   allParams
	 * @param   model
	 * @return
	 * @throws  IOException
	 * @throws  NumberFormatException
	 * @throws  MQException
	 */
	@GetMapping("/selectivePurge")
	public String selectivePurge(@RequestParam
		Map<String, String> allParams, Model model) throws IOException, NumberFormatException, MQException
	{
		MqManagerConnection mq = MqManagerConnection.connect(allParams.get("host"), allParams.get("manager"),
				allParams.get("channel"), Integer.parseInt(allParams.get("port")), allParams.get("queue"));

		String textToPurge = allParams.get("textToPurge");
		int maxDeleteCount = Integer.parseInt(allParams.get("deleteCount"));
		int maxSkipCount = Integer.parseInt(allParams.get("skipCount"));
		List<String> purgedMessages = new ArrayList<>();
		List<String> savedMessages = new ArrayList<>();

		int skipCount = 0;
		int deleteCount = 0;
		boolean keepReading = true;
		while (keepReading)
		{
			String message = null;
			try
			{
				message = mq.read();
				boolean isSkip = false;

				if (message.contains(textToPurge) && (maxSkipCount != 0) && (skipCount < maxSkipCount))
				{
					isSkip = true;
					skipCount++;
				}
				if (message.contains(textToPurge) && !((maxDeleteCount != 0) && (deleteCount >= maxDeleteCount))
					&& !isSkip)
				{
					purgedMessages.add(message);
					deleteCount++;
				}
				else
				{
					savedMessages.add(message);
				}
			}
			catch (MQException e)
			{
				if (e.reasonCode == 2033)
				{
					// LOGGER.trace("No more MQ message found");
				}
				keepReading = false;
			}
		}

		model.addAttribute("messages", purgedMessages);
		model.addAttribute("purgedTotal", purgedMessages.size());
		// model.addAttribute("savedCount", savedMessages.size());

		mq.commit();
		mq.disconnect();

		mq = MqManagerConnection.connect(allParams.get("host"), allParams.get("manager"),
				allParams.get("channel"), Integer.parseInt(allParams.get("port")), allParams.get("queue"));

		for (String msg : savedMessages)
		{
			mq.send(msg);
		}

		mq.commit();
		mq.disconnect();

		return "deletedMessages";
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 * @throws  MQException
	 * @throws  IOException
	 * @throws  NumberFormatException
	 */
	@GetMapping("/sendMessage")
	public String sendMessage(@RequestParam
		Map<String, String> allParams, Model model) throws IOException, NumberFormatException, MQException
	{
		MqManagerConnection mq = MqManagerConnection.connect(allParams.get("host"), allParams.get("manager"),
				allParams.get("channel"), Integer.parseInt(allParams.get("port")), allParams.get("queue"));

		if (allParams.get("isMultipleEnabled") != null)
		{
			List<String> messages = convertMsgToList(allParams.get("msgText"));
			for (String message : messages)
			{
				mq.send(message);
			}
			model.addAttribute("messages", messages);
			model.addAttribute("sentTotal", messages.size());
		}
		else
		{
			String message = allParams.get("msgText");
			mq.send(message);

			model.addAttribute("messages", message);
			model.addAttribute("sentTotal", 1);
		}

		mq.commit();
		mq.disconnect();

		return "sentMessages";
	}
	
	/**
	 * Purge MQ Messages
	 *
	 * @param   allParams
	 * @return
	 * @throws  IOException
	 * @throws  NumberFormatException
	 * @throws  MQException
	 */
	@GetMapping({ "/purgeMessages" })
	public String purgeMessages(@RequestParam
		Map<String, String> allParams, Model model) throws IOException, NumberFormatException, MQException
	{
		MqManagerConnection mq = MqManagerConnection.connect(allParams.get("host"), allParams.get("manager"),
				allParams.get("channel"), Integer.parseInt(allParams.get("port")), allParams.get("queue"));
		boolean keepReading = true;
		List<String> messages = new ArrayList<>();

		while (keepReading)
		{
			String message = null;
			try
			{
				message = mq.read();
			}
			catch (MQException e)
			{
				keepReading = false;
			}
			if (message == null)
			{
				continue;
			}
			messages.add(message);
		}

		model.addAttribute("messages", messages);
		model.addAttribute("purgedTotal", Integer.valueOf(messages.size()));
		mq.commit();
		mq.disconnect();

		return "deletedMessages";
	}

	/**
	 * Moves messages from a source MQ queue to a destination MQ queue based on filter criteria.
	 * Messages containing the specified text are moved up to a maximum count, and optionally skipped.
	 * All non-moved messages are restored to the source queue.
	 *
	 * @param allParams Request parameters containing MQ connection and filter details
	 * @param model Spring MVC model for passing data to the view
	 * @return The name of the view to render
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws MQException
	 */
	@GetMapping("/moveMessages")
	public String moveMessages(@RequestParam Map<String, String> allParams, Model model) throws IOException, NumberFormatException, MQException {
		// Connect to the source MQ queue
		MqManagerConnection sourceMq = MqManagerConnection.connect(
			allParams.get("host"),
			allParams.get("manager"),
			allParams.get("channel"),
			Integer.parseInt(allParams.get("port")),
			allParams.get("sourceQueue")
		);

		// Connect to the destination MQ queue
		MqManagerConnection destMq = MqManagerConnection.connect(
			allParams.get("destinationHost"),
			allParams.get("destinationManager"),
			allParams.get("destinationChannel"),
			Integer.parseInt(allParams.get("destinationPort")),
			allParams.get("destinationQueue")
		);
		// Get filter and limit parameters
		String textToMoveKeyword = allParams.get("textToMove");
		int maxMoveCount = 0;
		int maxSkipCount = 0;
		try{
			maxMoveCount = Integer.parseInt(allParams.getOrDefault("moveCount", "0"));
		 	maxSkipCount = Integer.parseInt(allParams.getOrDefault("skipCount", "0"));
		} catch (Exception e){
			// ignore, use 0
		}

		// Messages that are moved
		List<String> movedMessages = new ArrayList<>(); 
		// Messages that are skipped or not moved
		List<String> skippedMessages = new ArrayList<>(); 
		int skipCount = 0;
		int moveCount = 0;
		boolean keepReading = true;
		try {
            while (keepReading) {
                String message = null;
                try {
                    message = sourceMq.read();
                    boolean isSkip = false;
                    if (message != null && textToMoveKeyword != null && message.contains(textToMoveKeyword) && (maxSkipCount != 0) && (skipCount < maxSkipCount)) {
                        isSkip = true;
                        skipCount++;
                    }
                    if (message != null && textToMoveKeyword != null && message.contains(textToMoveKeyword) && !((maxMoveCount != 0) && (moveCount >= maxMoveCount)) && !isSkip) {
                        destMq.send(message);
                        movedMessages.add(message);
                        moveCount++;
                    } else if (message != null) {
                        skippedMessages.add(message);
                    }
                } catch (MQException e) {
                    if (movedMessages.size() == 0 && skippedMessages.size() == 0) {
                        // If no messages were moved, return an error message
                        return "redirect:/mqMoveMessages.html?error=Source%20queue%20is%20empty.%20No%20messages%20to%20move.";
                    }
                    keepReading = false;
                }
            }
            sourceMq.commit();
            sourceMq.disconnect();
            destMq.commit();
            destMq.disconnect();

            // Restore skipped messages to source queue
            sourceMq = MqManagerConnection.connect(
                allParams.get("host"),
                allParams.get("manager"),
                allParams.get("channel"),
                Integer.parseInt(allParams.get("port")),
                allParams.get("sourceQueue")
            );
            for (String msg : skippedMessages) {
                sourceMq.send(msg);
            }
            sourceMq.commit();
            sourceMq.disconnect();
        } catch (Exception ex) {
            // Revert both source and destination if exception occurs during moving process
            try { if (sourceMq != null) sourceMq.revert(); } catch (Exception ignore) {}
            try { if (destMq != null) destMq.revert(); } catch (Exception ignore) {}
            try { if (sourceMq != null) sourceMq.disconnect(); } catch (Exception ignore) {}
            try { if (destMq != null) destMq.disconnect(); } catch (Exception ignore) {}
            // Redirect to static HTML with error message as query param
            return "redirect:/mqMoveMessages.html?error=An%20error%20occurred%20while%20moving%20messages.%20All%20changes%20have%20been%20reverted.";
        }
        model.addAttribute("messages", movedMessages);
        model.addAttribute("movedTotal", movedMessages.size());
        model.addAttribute("skippedTotal", skippedMessages.size());
        return "movedMessages";
	}
}
