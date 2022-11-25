package com.jay.mqconsumer.business.logic.util;

import com.ibm.mq.MQException;
import com.ibm.mq.MQGetMessageOptions;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;

import java.io.IOException;

import java.util.Hashtable;
import java.util.Map;


/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class MqManagerConnection
{
	//~ Instance fields --------------------------
	/**  */
	private MQQueue inQueue;

	/**  */
	private MQQueueManager mqManager;
	//~ Methods ----------------------------------

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public MQQueue getInQueue()
	{
		return inQueue;
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @param   host
	 * @param   manager
	 * @param   channel
	 * @param   port
	 * @param   queue
	 * @return
	 * @throws  MQException
	 */
	public static MqManagerConnection connect(String host, String manager, String channel, int port, String queue)
		throws MQException
	{
		MqManagerConnection conn = new MqManagerConnection();
		conn.connectQueue(host, manager, channel, port, queue);

		return conn;
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @throws  MQException
	 */
	public void disconnect() throws MQException
	{
		if (mqManager != null)
		{
			inQueue.close();
			mqManager.disconnect();
			mqManager.close();
			mqManager = null;
		}
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @throws  MQException
	 */
	public void revert() throws MQException
	{
		mqManager.backout();
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @throws  MQException
	 */
	public void commit() throws MQException
	{
		mqManager.commit();
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 * @throws  IOException
	 * @throws  MQException
	 */
	public String read() throws IOException, MQException
	{
		MQMessage msg = new MQMessage();
		MQGetMessageOptions opt = new MQGetMessageOptions();
		opt.options += MQConstants.MQGMO_SYNCPOINT;
		opt.options += MQConstants.MQGMO_NO_WAIT;
		opt.options += MQConstants.MQGMO_FAIL_IF_QUIESCING;

		inQueue.get(msg, opt);

		String msgString = "";
		byte[] messageBuffer = new byte[msg.getMessageLength()];
		msg.readFully(messageBuffer);
		msgString = new String(messageBuffer);

		return msgString = msgString.replace("\r\n", "\n");
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @param   text
	 * @throws  IOException
	 * @throws  MQException
	 */
	public void send(String text) throws IOException, MQException
	{
		MQMessage msg = new MQMessage();
		msg.format = MQConstants.MQFMT_STRING;
		msg.writeString(text);

		MQPutMessageOptions opt = new MQPutMessageOptions();
		inQueue.put(msg, opt);
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @param   host
	 * @param   manager
	 * @param   channel
	 * @param   port
	 * @param   queue
	 * @return
	 * @throws  MQException
	 */
	private void connectQueue(String host, String manager, String channel, int port, String queue) throws MQException
	{
		mqManager = connect(host, manager, channel, port);
		inQueue = connect(queue);
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @param   host
	 * @param   manager
	 * @param   channel
	 * @param   port
	 * @return
	 * @throws  MQException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private MQQueueManager connect(String host, String manager, String channel, int port) throws MQException
	{
		Map properties = new Hashtable();
		properties.put(MQConstants.HOST_NAME_PROPERTY, host);

		properties.put(MQConstants.CHANNEL_PROPERTY, channel);

		properties.put(MQConstants.PORT_PROPERTY, port);

		MQQueueManager mqManager = new MQQueueManager(manager, (Hashtable) properties);

		return mqManager;
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @param   mqManager
	 * @param   queue
	 * @return
	 * @throws  MQException
	 */
	private MQQueue connect(String queue) throws MQException
	{
		int inboundOpenOptions = MQConstants.MQOO_INPUT_SHARED | MQConstants.MQOO_FAIL_IF_QUIESCING
			| MQConstants.MQOO_INQUIRE | MQConstants.MQOO_OUTPUT | MQConstants.MQOO_FAIL_IF_QUIESCING;
		inQueue = mqManager.accessQueue(queue, inboundOpenOptions, null, null, null);

		return inQueue;
	}
}
