package com.jay.mqconsumer.service.util;

import com.ibm.mq.MQException;

import java.io.IOException;
import java.util.List;

public class MQUtil {
    /**
     * DOCUMENT ME!
     *
     * @param   mq
     * @param   messages
     * @return
     * @throws IOException
     */
    public static List<String> retrieveMqMSGS(MqManagerConnection mq, List<String> messages) throws IOException
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
}
