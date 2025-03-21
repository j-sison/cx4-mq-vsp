package com.jay.mqconsumer.rest.api;

import com.ibm.mq.MQException;

import com.jay.mqconsumer.business.logic.service.MqMessageService;
import com.jay.mqconsumer.business.logic.service.SequenceGeneratorService;
import com.jay.mqconsumer.business.logic.util.MQUtil;
import com.jay.mqconsumer.business.logic.util.MqManagerConnection;
import com.jay.mqconsumer.business.logic.util.MqSenderUtil;
import com.jay.mqconsumer.data.entity.MqMessage;
import com.jay.mqconsumer.payload.dto.MqMessageDto;

import org.modelmapper.ModelMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.MediaType;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

import java.text.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
// @RestController
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
	@Autowired
	private ModelMapper modelMapper;

	/** @Autowired */
	private MqMessageService mqMessageService;

	/**  */
	@Value("${cust.mq.port}")
	private int port;

	/**  */
	@Value("${cust.mq.queue}")
	private String queue;

	/**  */
	@Autowired
	private SequenceGeneratorService sequenceGenerator;
	//~ Constructors -----------------------------
	/**
	 * Creates a new MQRestController object.
	 *
	 * @param  mqMessageService
	 */
	public MQRestController(MqMessageService mqMessageService)
	{
		super( );
		this.mqMessageService = mqMessageService;
	}
	//~ Methods ----------------------------------
	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	@GetMapping("/getMqMessages")
	private List<String> retrievedMqMessages(@RequestParam
		Map<String, String> allParams) throws IOException, NumberFormatException, MQException
	{
		List<String> messages = new ArrayList<String>();

		try
		{
			MqManagerConnection mq = MqManagerConnection.connect(allParams.get("host"), allParams.get("manager"),
					allParams.get("channel"), Integer.parseInt(allParams.get("port")), allParams.get("queue"));

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
	@GetMapping(
		path = "/sendMQMessage",
		produces = MediaType.APPLICATION_JSON_VALUE
	)
	@ResponseBody
	public String sendMQMessages(@RequestParam
		Map<String, String> allParams) throws IOException, NumberFormatException, MQException, ParseException
	{
		MqManagerConnection mq = MqManagerConnection.connect(allParams.get("host"), allParams.get("manager"),
				allParams.get("channel"), Integer.parseInt(allParams.get("port")), allParams.get("queue"));

		if ((allParams.get("isMultipleEnabled") != null) && allParams.get("isMultipleEnabled").equals("true"))
		{
			for (String msg : MqSenderUtil.convertMsgToList(allParams.get("msgText")))
			{
				mq.send(msg);
				MqMessageDto mqMessageDto = new MqMessageDto(0, msg);
				saveMqMessage(mqMessageDto);
			}
		}
		else
		{
			String message = allParams.get("msgText");
			mq.send(message);
			MqMessageDto mqMessageDto = new MqMessageDto(0, message);
			saveMqMessage(mqMessageDto);
		}
		mq.commit();
		mq.disconnect();

		return "{\"res\":\"success\"}";
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
			messages.add(message);
		}
		model.addAttribute("messages", messages);
		model.addAttribute("total", Integer.valueOf(messages.size()));
		mq.commit();
		mq.disconnect();

		return "deletedMessages";
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	@GetMapping(
		path = "/getAllMqMessages",
		produces = MediaType.APPLICATION_JSON_VALUE
	)
	private List<MqMessage> getAllMQMessages()
	{
		return getMqMessages();
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @param   mqMessageDto
	 * @throws  ParseException
	 */
	private void saveMqMessage(MqMessageDto mqMessageDto) throws ParseException
	{
		mqMessageService.createMessage(convertToEntity(mqMessageDto));
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	private List<MqMessage> getMqMessages()
	{
		List<MqMessage> mqMessages = mqMessageService.getMqMessages();
		List<MqMessageDto> mqMessagesDto = Collections.emptyList();

		return mqMessageService.getMqMessages();
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @param   mqMessage
	 * @return
	 */
	private MqMessageDto convertToDto(MqMessage mqMessage)
	{
		MqMessageDto mqMessageDto = modelMapper.map(mqMessage, MqMessageDto.class);

		return mqMessageDto;
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @param   mqMessageDto
	 * @return
	 * @throws  ParseException
	 */
	private MqMessage convertToEntity(MqMessageDto mqMessageDto) throws ParseException
	{
		MqMessage mqMessage = modelMapper.map(mqMessageDto, MqMessage.class);
		mqMessage.setId(sequenceGenerator.getSequenceNumber(MqMessage.SEQUENCE_NAME));

		return mqMessage;
	}
}
