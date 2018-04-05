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

import org.junit.Before;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

import com.embotics.vlm.plugin.VCommanderConfig;
import com.embotics.vlm.plugin.mock.VCommanderClientMock;

import hudson.model.FreeStyleProject;
import jenkins.model.GlobalConfiguration;

/**
 * Base class to share UT code for VCommander Actions
 * 
 * @author btarczali
 */
public abstract class AbstractVCommanderActionTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();
    
    protected VCommanderClientMock vCommanderClient;
    protected FreeStyleProject project;

    protected final Long timeout = 20l;
    protected final Long polling = 30l;
    
    @Before
    public void setup() throws IOException {
    	// mock the vCommander rest client
    	vCommanderClient = new VCommanderClientMock();
    	project = jenkins.createFreeStyleProject();
    	
    	// init the configuration
    	VCommanderConfig commanderPluginConfig = jenkins.jenkins.getDescriptorList(GlobalConfiguration.class).get(VCommanderConfig.class);
    	commanderPluginConfig.setvCommanderClient(vCommanderClient);
    	commanderPluginConfig.save();
    }
    
}