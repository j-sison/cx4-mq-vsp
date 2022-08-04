package com.jay.mqconsumer;

import com.ibm.mq.MQException;
import com.jay.mqconsumer.service.util.MQUtil;
import com.jay.mqconsumer.service.util.MqManagerConnection;
import com.jay.mqconsumer.service.util.MqSenderUtil;
import org.springframework.beans.factory.annotation.Value;
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
@RestController
public class MQRestController
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
	
	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	@GetMapping("/getMqMessages")
	private List<String> retrievedMqMes()
	{
		List<String> messages = new ArrayList<String>();

		try
		{
			MqManagerConnection mq = MqManagerConnection.connect(host, manager, channel, port, queue);
			messages = MQUtil.retrieveMqMSGS(mq, messages);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		catch (MQException e)
		{
			throw new RuntimeException(e);
		}

		return messages;
	}


	/**
	 * DOCUMENT ME!
	 *
	 * @param   allParams
	 * @return
	 * @throws  IOException
	 * @throws  NumberFormatException
	 * @throws  MQException
	 */
	@GetMapping(path = "/sendMQMessage")
	public String sendMQMessages(@RequestParam
		Map<String, String> allParams) throws IOException, NumberFormatException, MQException
	{
		MqManagerConnection mq = MqManagerConnection.connect(allParams.get("host"), allParams.get("manager"),
				allParams.get("channel"), Integer.parseInt(allParams.get("port")), allParams.get("queue"));

		if (allParams.get("isMultipleEnabled") != null)
		{
			for (String msg : MqSenderUtil.convertMsgToList(allParams.get("msgText")))
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

		return "success";
	}

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
