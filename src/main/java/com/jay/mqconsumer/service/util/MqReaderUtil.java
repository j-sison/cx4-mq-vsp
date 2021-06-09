package com.jay.mqconsumer.service.util;

import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.constants.MQConstants;

import java.io.IOException;


/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class MqReaderUtil
{
	//~ Methods ----------------------------------
	/**
	 * DOCUMENT ME!
	 *
	 * @param   inQueue
	 * @return
	 * @throws  IOException
	 * @throws  MQException
	 */
	public static String read(MQQueue inQueue) throws IOException, MQException
	{
		MQMessage msg = new MQMessage();
		MQGetMessageOptions opt = new MQGetMessageOptions();
		opt.options += MQConstants.MQGMO_SYNCPOINT;
		opt.options += MQConstants.MQGMO_NO_WAIT;
		opt.options += MQConstants.MQGMO_FAIL_IF_QUIESCING;

		inQueue.get(msg, opt);

		// check if the message wasn't already processed
		String msgString = "";

		// Initialize Message Buffer
		byte[] messageBuffer = new byte[msg.getMessageLength()];

		// Read the Message in Bytes from the Queue
		msg.readFully(messageBuffer);
		msgString = new String(messageBuffer);

		return msgString = msgString.replace("\r\n", "\n");
	}
}
