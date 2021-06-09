package com.jay.mqconsumer;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import org.springframework.stereotype.Component;


/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
@Component
public class HelloWorld implements ApplicationRunner
{
	//~ Methods ----------------------------------

	/** @see  org.springframework.boot.ApplicationRunner#run(org.springframework.boot.ApplicationArguments) */
	@Override
	public void run(ApplicationArguments args) throws Exception
	{
		// TODO Auto-generated method stub
		System.out.println("Hello WOrld");
	}
}
