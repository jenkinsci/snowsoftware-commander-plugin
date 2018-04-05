/*************************************************************************
 *
 *	Copyright (c) 2017 Embotics Corporation. All Rights Reserved.
 *
 *	No part of this software may be reproduced, used in any
 *	information storage and retrieval system, or transmitted in
 *	any form or by any means, electronic, mechanical or otherwise,
 *	without the written permission of Embotics Corporation.
 *
 ***************************************************************************/

package com.embotics.vlm.plugin;

import java.io.IOException;

import hudson.model.Describable;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * Interface to be implemented by all vCommander actions
 * 
 * @author btarczali
 */
public interface VCommanderAction extends Describable<VCommanderAction> {
	
	/**
	 * Perform the configured build step action
	 * 
	 * @param run		run context
	 * @param listener	listener context, used for logging
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	void perform(Run<?, ?> run, TaskListener listener) throws InterruptedException, IOException;

}
