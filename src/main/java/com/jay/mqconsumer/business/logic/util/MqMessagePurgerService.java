package com.jay.mqconsumer.business.logic.util;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.MQConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;


/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class MqMessagePurgerService
{
	//~ Static fields/initializers ---------------
	/**  */
	private static final Logger LOGGER = LogManager.getLogger(MqMessagePurgerService.class);
	//~ Instance fields --------------------------
	/**  */
	private String lastMessage;
	//~ Methods ----------------------------------
	/** DOCUMENT ME! */
	public void purge()
	{
		Map properties = new Hashtable();
		properties.put(MQConstants.HOST_NAME_PROPERTY, "cs-v-mqdev1.champ.aero");

		properties.put(MQConstants.CHANNEL_PROPERTY, "CS.SVRCONN");

		properties.put(MQConstants.PORT_PROPERTY, 1414);

		try
		{
			MQQueueManager mqManager = new MQQueueManager("CCSLUXD1", (Hashtable) properties);
			int inboundOpenOptions = MQConstants.MQOO_INPUT_SHARED | MQConstants.MQOO_FAIL_IF_QUIESCING
				| MQConstants.MQOO_INQUIRE;
			MQQueue inQueue = mqManager.accessQueue("CS.DEV.TEST2.OUT", inboundOpenOptions,
					null, // default queue manager.
					null, // no dynamic queue name
					null); // no alternate user ID

			String msgString = MqReaderUtil.read(inQueue);

			String logDate = new Date() + "\r\n";
			LOGGER.info("************RECEIVE***********");
			LOGGER.info(logDate);
			LOGGER.info(msgString);
			LOGGER.info("\r\n");
			lastMessage = msgString;

			mqManager.backout();
			inQueue.close();
			if (mqManager != null)
			{
				mqManager.disconnect();
				mqManager.close();
				mqManager = null;
			}
			// mqManager.commit();
		}
		catch (MQException | IOException e)
		{
			LOGGER.error(e);
		} // end try-catch
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public String getLastMessage()
	{
		return lastMessage;
	}
	

	/**
	 * DOCUMENT ME!
	 *
	 * @param   allParams
	 * @return
	 */
	public static MqMessagePurgerService createInstance(Map<String, String> allParams)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
