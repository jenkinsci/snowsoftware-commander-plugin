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

package com.embotics.vlm.plugin.actions;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.embotics.vlm.plugin.AbstractVCommanderActionTest;
import com.embotics.vlm.plugin.VCommanderBuilder;
import com.embotics.vlm.plugin.VCommanderEnvironmentContributingAction;
import com.embotics.vlm.rest.v30.client.model.DeployedComponentInfo;

import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;

/**
 * Unit tests for VCommanderBuilder
 * 
 * @author btarczali
 */
public class VCommanderWaitForRequestNewServiceActionTest extends AbstractVCommanderActionTest {

    private final String requestId = "1";
    
    @Test
    public void build_Successful() throws Exception {
    	VCommanderWaitForRequestNewServiceAction action = new VCommanderWaitForRequestNewServiceAction(requestId, timeout, polling);
    	VCommanderBuilder vCommanderBuilder = new VCommanderBuilder(action);
        project.getBuildersList().add(vCommanderBuilder);
        
        List<DeployedComponentInfo> results = new ArrayList<>();
        results.add(new DeployedComponentInfo(1, 1, "componentType1", "componentName1"));
        results.add(new DeployedComponentInfo(1, 2, "componentType2", "componentName2"));
        vCommanderClient.setNewServiceRequestResult(results);
        
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        
        //check logs
        jenkins.assertLogContains("Waiting 20 minutes for request completion. Checking every 30 seconds ...", build);
        jenkins.assertLogContains("Service request successfully completed", build);
        
        //check environment variables
        VCommanderEnvironmentContributingAction envAction = build.getAction(VCommanderEnvironmentContributingAction.class);
        Assert.assertNotNull("No environment variables created", envAction);

	        jenkins.assertLogContains("Adding new environment variable: VCOMMANDER_REQUESTED_SERVICE1_COMPONENT1_NAME=componentName1", build);
	        jenkins.assertLogContains("Adding new environment variable: VCOMMANDER_REQUESTED_SERVICE1_COMPONENT1_TYPE=componentType1", build);
	        Assert.assertEquals("1st Component Name environment variable not mathing", "componentName1", envAction.getEnvValue("VCOMMANDER_REQUESTED_SERVICE1_COMPONENT1_NAME"));
	        Assert.assertEquals("1st Component Type environment variable not mathing", "componentType1", envAction.getEnvValue("VCOMMANDER_REQUESTED_SERVICE1_COMPONENT1_TYPE"));
	        
	        jenkins.assertLogContains("Adding new environment variable: VCOMMANDER_REQUESTED_SERVICE1_COMPONENT2_NAME=componentName2", build);
	        jenkins.assertLogContains("Adding new environment variable: VCOMMANDER_REQUESTED_SERVICE1_COMPONENT2_TYPE=componentType2", build);
	        Assert.assertEquals("2nd Component Name environment variable not mathing", "componentName2", envAction.getEnvValue("VCOMMANDER_REQUESTED_SERVICE1_COMPONENT2_NAME"));
	        Assert.assertEquals("2nd Component Type environment variable not mathing", "componentType2", envAction.getEnvValue("VCOMMANDER_REQUESTED_SERVICE1_COMPONENT2_TYPE"));
    }
    
    @Test
    public void build_NotConfigured() throws Exception {
        //pass in an empty payload - not configured step
    	VCommanderWaitForRequestNewServiceAction action = new VCommanderWaitForRequestNewServiceAction("", timeout, polling);
    	VCommanderBuilder vCommanderBuilder = new VCommanderBuilder(action);
        project.getBuildersList().add(vCommanderBuilder);

        QueueTaskFuture<FreeStyleBuild> build = project.scheduleBuild2(0);
        jenkins.assertBuildStatus(Result.FAILURE, build);
        
        //check log
        jenkins.assertLogContains("Error in build step configuration. RequestId is not specified.", build.get());
    }
    
    @Test
    public void build_Failed() throws Exception {
    	VCommanderWaitForRequestNewServiceAction action = new VCommanderWaitForRequestNewServiceAction(requestId, timeout, polling);
    	VCommanderBuilder vCommanderBuilder = new VCommanderBuilder(action);
        project.getBuildersList().add(vCommanderBuilder);
        
        vCommanderClient.setThrowException(true, "Custom ERROR");
        
        QueueTaskFuture<FreeStyleBuild> build = project.scheduleBuild2(0);
        jenkins.assertBuildStatus(Result.FAILURE, build);
        
        //check if service is being requested
        jenkins.assertLogContains("FATAL: Custom ERROR", build.get());
    }

}