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
import com.embotics.vlm.plugin.actions.VCommanderRequestNewServiceAction;
import com.embotics.vlm.rest.v30.client.model.DeployedComponentInfo;

import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;

/**
 * Unit tests for VCommanderBuilder
 * 
 * @author btarczali
 */
public class VCommanderRequestNewServiceActionTest extends AbstractVCommanderActionTest {

    private final String payload = "{ \"service_id\": 876028 }";
    
    @Test
    public void buildStepConfig_LookupServiceByID() throws Exception {
    	//with a proper payload
    	vCommanderClient.setPublishedServiceName("TestService");
    	VCommanderRequestNewServiceAction action = new VCommanderRequestNewServiceAction(payload, true, timeout, polling);
    	Assert.assertEquals("Published service name not matching.", "TestService", action.getServiceName());
    	
    	//with an invalid payload
    	vCommanderClient.setThrowException(true, "Custom ERROR");
    	action = new VCommanderRequestNewServiceAction("service_id\": 876028 }", true, timeout, polling);
    	Assert.assertEquals("Published service name should be blank.", "", action.getServiceName());
    }

    @Test
    public void build_Successful_Sync() throws Exception {
    	build_Successful(true);
    }
    
    @Test
    public void build_Successful_Async() throws Exception {
    	build_Successful(false);
    }
    
    private void build_Successful(boolean sync) throws Exception {
    	VCommanderRequestNewServiceAction action = new VCommanderRequestNewServiceAction(payload, sync, timeout, polling);
    	VCommanderBuilder vCommanderBuilder = new VCommanderBuilder(action);
        project.getBuildersList().add(vCommanderBuilder);
        
        vCommanderClient.setRequestID(1L);
        List<DeployedComponentInfo> results = new ArrayList<>();
        results.add(new DeployedComponentInfo(1, 1, "componentType1", "componentName1"));
        results.add(new DeployedComponentInfo(1, 2, "componentType2", "componentName2"));
        vCommanderClient.setNewServiceRequestResult(results);
        
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        
        //check if service was created
        jenkins.assertLogContains("Creating new service request with payload: " + payload, build);
        jenkins.assertLogContains("Service was succesfuly requested. RequestID: 1", build);
        if(sync) {
            jenkins.assertLogContains("Waiting 20 minutes for request completion. Checking every 30 seconds ...", build);
            jenkins.assertLogContains("Service request successfully completed", build);
        } else {
            jenkins.assertLogNotContains("Waiting 20 minutes for request completion. Checking every 30 seconds ...", build);
            jenkins.assertLogNotContains("Service request successfully completed", build);
        }

        
        //check environment variables
        VCommanderEnvironmentContributingAction envAction = build.getAction(VCommanderEnvironmentContributingAction.class);
        Assert.assertNotNull("No environment variables created", envAction);

        jenkins.assertLogContains("Adding new environment variable: VCOMMANDER_REQUESTED_SERVICE_ID=1", build);
        Assert.assertEquals("Request ID environment variable not mathing", "1", envAction.getEnvValue("VCOMMANDER_REQUESTED_SERVICE_ID"));
        
        if(sync) {
	        jenkins.assertLogContains("Adding new environment variable: VCOMMANDER_REQUESTED_SERVICE1_COMPONENT1_NAME=componentName1", build);
	        jenkins.assertLogContains("Adding new environment variable: VCOMMANDER_REQUESTED_SERVICE1_COMPONENT1_TYPE=componentType1", build);
	        Assert.assertEquals("1st Component Name environment variable not mathing", "componentName1", envAction.getEnvValue("VCOMMANDER_REQUESTED_SERVICE1_COMPONENT1_NAME"));
	        Assert.assertEquals("1st Component Type environment variable not mathing", "componentType1", envAction.getEnvValue("VCOMMANDER_REQUESTED_SERVICE1_COMPONENT1_TYPE"));
	        
	        jenkins.assertLogContains("Adding new environment variable: VCOMMANDER_REQUESTED_SERVICE1_COMPONENT2_NAME=componentName2", build);
	        jenkins.assertLogContains("Adding new environment variable: VCOMMANDER_REQUESTED_SERVICE1_COMPONENT2_TYPE=componentType2", build);
	        Assert.assertEquals("2nd Component Name environment variable not mathing", "componentName2", envAction.getEnvValue("VCOMMANDER_REQUESTED_SERVICE1_COMPONENT2_NAME"));
	        Assert.assertEquals("2nd Component Type environment variable not mathing", "componentType2", envAction.getEnvValue("VCOMMANDER_REQUESTED_SERVICE1_COMPONENT2_TYPE"));
        } else {
	        jenkins.assertLogNotContains("Adding new environment variable: VCOMMANDER_REQUESTED_SERVICE1_COMPONENT1_NAME=componentName1", build);
	        jenkins.assertLogNotContains("Adding new environment variable: VCOMMANDER_REQUESTED_SERVICE1_COMPONENT1_TYPE=componentType1", build);
	        Assert.assertEquals("1st Component Name environment variable not mathing", null, envAction.getEnvValue("VCOMMANDER_REQUESTED_SERVICE1_COMPONENT1_NAME"));
	        Assert.assertEquals("1st Component Type environment variable not mathing", null, envAction.getEnvValue("VCOMMANDER_REQUESTED_SERVICE1_COMPONENT1_TYPE"));
	        
	        jenkins.assertLogNotContains("Adding new environment variable: VCOMMANDER_REQUESTED_SERVICE1_COMPONENT2_NAME=componentName2", build);
	        jenkins.assertLogNotContains("Adding new environment variable: VCOMMANDER_REQUESTED_SERVICE1_COMPONENT2_TYPE=componentType2", build);
	        Assert.assertEquals("2nd Component Name environment variable not mathing", null, envAction.getEnvValue("VCOMMANDER_REQUESTED_SERVICE1_COMPONENT2_NAME"));
	        Assert.assertEquals("2nd Component Type environment variable not mathing", null, envAction.getEnvValue("VCOMMANDER_REQUESTED_SERVICE1_COMPONENT2_TYPE"));

        }
    }
    
    @Test
    public void build_NotConfigured() throws Exception {
        //pass in an empty payload - not configured step
    	VCommanderRequestNewServiceAction action = new VCommanderRequestNewServiceAction("", true, timeout, polling);
    	VCommanderBuilder vCommanderBuilder = new VCommanderBuilder(action);
        project.getBuildersList().add(vCommanderBuilder);

        QueueTaskFuture<FreeStyleBuild> build = project.scheduleBuild2(0);
        jenkins.assertBuildStatus(Result.FAILURE, build);
        
        //check log
        jenkins.assertLogContains("There is no configuration for the vCommander service request build step.", build.get());
    }
    
    @Test
    public void build_Failed() throws Exception {
    	VCommanderRequestNewServiceAction action = new VCommanderRequestNewServiceAction(payload, true, timeout, polling);
    	VCommanderBuilder vCommanderBuilder = new VCommanderBuilder(action);
        project.getBuildersList().add(vCommanderBuilder);
        
        vCommanderClient.setThrowException(true, "Custom ERROR");
        
        QueueTaskFuture<FreeStyleBuild> build = project.scheduleBuild2(0);
        jenkins.assertBuildStatus(Result.FAILURE, build);
        
        //check if service is being requested
        jenkins.assertLogContains("Creating new service request with payload: " + payload, build.get());
        jenkins.assertLogContains("FATAL: Custom ERROR", build.get());
    }
    
    // Tests for build step configurations are not working currently
    // More investigation required, to check if we can test these 

//	@Test
//	public void buildStepConfig_Correct() throws Exception {
//		VCommanderRequestNewServiceAction action = new VCommanderRequestNewServiceAction(payload);
//		VCommanderBuilder vCommanderBuilder = new VCommanderBuilder(action, timeout, polling);
//		project.getBuildersList().add(vCommanderBuilder);
//
//		project = jenkins.configRoundtrip(project);
//		jenkins.assertEqualDataBoundBeans(new VCommanderBuilder(action, timeout, polling),
//				project.getBuildersList().get(0));
//	}
//    
//    @Test
//    public void buildStepConfig_IncorrectPayload() throws Exception {
//        //passing not well formated json payload
//        //wrong timeout and polling
//        VCommanderBuilder vCommanderBuilder = new VCommanderBuilder("service_id\": 876028 }", -1, -1);
//        project.getBuildersList().add(vCommanderBuilder);
//        
//        project = jenkins.configRoundtrip(project);
//        //expect that payload to be defaulted to not configured (empty string)
//        //expect timeout and polling will be defaulted to 10 / 15
//        jenkins.assertEqualDataBoundBeans(new VCommanderBuilder("", 10, 15), project.getBuildersList().get(0));
//    }


}