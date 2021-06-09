package com.jay.mqconsumer;

import com.ibm.mq.MQException;

import com.jay.mqconsumer.service.util.MqManagerConnection;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

		boolean keepReading = true;
		List<String> messages = new ArrayList<String>();

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
			messages.add(message);
		}

		model.addAttribute("messages", messages);
		model.addAttribute("total", messages.size());

		mq.revert();
		mq.disconnect();

		return "displayMessages";
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

		mq.send(allParams.get("msgText"));

		mq.commit();
		mq.disconnect();

		return "msgSent";
	}
}
