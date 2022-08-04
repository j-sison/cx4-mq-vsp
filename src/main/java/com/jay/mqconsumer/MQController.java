package com.jay.mqconsumer;

import static com.jay.mqconsumer.service.util.MqSenderUtil.convertMsgToList;

import com.ibm.mq.MQException;

import com.jay.mqconsumer.service.util.MqManagerConnection;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import java.util.ArrayList;
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

		List<String> messages = new ArrayList<String>();

		messages = retrieveMqMSGS(mq, messages);

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
	private List<String> retrieveMqMSGS(MqManagerConnection mq, List<String> messages) throws IOException
	{
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
			for (String msg : convertMsgToList(allParams.get("msgText")))
			{
				mq.send(msg);
			}
		}
		else
		{
			mq.send(allParams.get("msgText"));
		}

		mq.commit();
		mq.disconnect();

		return "msgSent";
	}

	/**
	 *
	 * Purge MQ Messages
	 *
	 * @param   allParams
	 * @return
	 * @throws  IOException
	 * @throws  NumberFormatException
	 * @throws  MQException
	 */
	@GetMapping({"/purgeMessages"})
	public String purgeMessages(@RequestParam
		Map<String, String> allParams, Model model) throws IOException, NumberFormatException, MQException
	{
		MqManagerConnection mq = MqManagerConnection.connect(allParams.get("host"), allParams.get("manager"),
				allParams.get("channel"), Integer.parseInt(allParams.get("port")), allParams.get("queue"));
		boolean keepReading = true;
		List<String> messages = new ArrayList<>();

		while (keepReading) {
			String message = null;
			try {
				message = mq.read();
			} catch (MQException e) {
				keepReading = false;
			}
			messages.add(message);
		}

		model.addAttribute("messages", messages);
		model.addAttribute("total", Integer.valueOf(messages.size()));
		mq.commit();
		mq.disconnect();
		return "deletedMessages";
	}
}
