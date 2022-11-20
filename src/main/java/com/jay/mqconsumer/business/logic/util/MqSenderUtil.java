package com.jay.mqconsumer.business.logic.util;

import java.util.ArrayList;
import java.util.List;


/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class MqSenderUtil
{
	//~ Methods ----------------------------------

	/**
	 * DOCUMENT ME!
	 *
	 * @param   messages
	 * @return
	 */
	public static List<String> convertMsgToList(String messages)
	{
		List<String> list = new ArrayList<>();
		String[] splitMsg = messages.split("\n");

		for (String msg : splitMsg)
		{
			list.add(msg);
		}

		return list;
	}
}
