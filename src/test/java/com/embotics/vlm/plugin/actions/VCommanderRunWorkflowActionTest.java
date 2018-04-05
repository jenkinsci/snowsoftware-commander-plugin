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

import org.junit.Assert;
import org.junit.Test;

import com.embotics.vlm.plugin.AbstractVCommanderActionTest;
import com.embotics.vlm.plugin.VCommanderBuilder;
import com.embotics.vlm.plugin.VCommanderEnvironmentContributingAction;

import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;

/**
 * Unit tests for VCommanderBuilder
 * 
 * @author btarczali
 */
public class VCommanderRunWorkflowActionTest extends AbstractVCommanderActionTest {

    private final String targetType = "VIRTUAL_MACHINE";
    private final String targetName = "TestVM001";
    private final String workflowName = "TestWorkflow";
    
    @Test
    public void build_Successful_Sync() throws Exception {
    	build_Successful(true);
    }
    
    @Test
    public void build_Successful_Async() throws Exception {
    	build_Successful(false);
    }
    
    private void build_Successful(boolean sync) throws Exception {
    	VCommanderRunWorkflowAction action = new VCommanderRunWorkflowAction(targetType, targetName, workflowName, sync, timeout, polling);
    	VCommanderBuilder vCommanderBuilder = new VCommanderBuilder(action);
        project.getBuildersList().add(vCommanderBuilder);
        
        vCommanderClient.setWorkflowDefinitionId(20L);
        vCommanderClient.setRunCommandWorkflowResult("30");
        vCommanderClient.setWorkflowId(40L);
        
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        
        //check if service was created
        jenkins.assertLogContains("Looking up command workflow with name: '" + workflowName + "'", build);
        jenkins.assertLogContains("Running command workflow: '" + workflowName + "' for target: '" + targetName + "' with type: " + targetType, build);
        jenkins.assertLogContains("Command workflow submitted to vCommander. Task ID: " + "30", build);
        if(sync) {
        	jenkins.assertLogContains("Waiting 20 minutes for workflow completion. Checking every 30 seconds ...", build);
        	jenkins.assertLogContains("Workflow successfully completed", build);
        } else {
        	jenkins.assertLogNotContains("Waiting 20 minutes for workflow completion. Checking every 30 seconds ...", build);
        	jenkins.assertLogNotContains("Workflow successfully completed", build);
        }
        
        //check environment variables
        VCommanderEnvironmentContributingAction envAction = build.getAction(VCommanderEnvironmentContributingAction.class);
        Assert.assertNotNull("No environment variables created", envAction);

        jenkins.assertLogContains("Adding new environment variable: VCOMMANDER_WORKFLOW_TASK_ID=30", build);
        Assert.assertEquals("Workflow ID environment variable not mathing", "30", envAction.getEnvValue("VCOMMANDER_WORKFLOW_TASK_ID"));
    }
    
    @Test
    public void build_NotConfigured() throws Exception {
        //pass in an empty workflow name - not configured step
    	VCommanderRunWorkflowAction action = new VCommanderRunWorkflowAction(targetType, targetName, "", true, timeout, polling);
    	VCommanderBuilder vCommanderBuilder = new VCommanderBuilder(action);
        project.getBuildersList().add(vCommanderBuilder);

        QueueTaskFuture<FreeStyleBuild> build = project.scheduleBuild2(0);
        jenkins.assertBuildStatus(Result.FAILURE, build);
        
        //check log
        jenkins.assertLogContains("There is no configuration for the vCommander workflow build step.", build.get());
    }
    
    @Test
    public void build_Failed() throws Exception {
    	VCommanderRunWorkflowAction action = new VCommanderRunWorkflowAction(targetType, targetName, workflowName, true, timeout, polling);
    	VCommanderBuilder vCommanderBuilder = new VCommanderBuilder(action);
        project.getBuildersList().add(vCommanderBuilder);
        
        vCommanderClient.setThrowException(true, "Custom ERROR");
        
        QueueTaskFuture<FreeStyleBuild> build = project.scheduleBuild2(0);
        jenkins.assertBuildStatus(Result.FAILURE, build);
        
        //check if service is being requested
        jenkins.assertLogContains("Looking up command workflow with name: '" + workflowName + "'", build.get());
        jenkins.assertLogContains("Running command workflow: '" + workflowName + "' for target: '" + targetName + "' with type: " + targetType, build.get());
        jenkins.assertLogContains("FATAL: Custom ERROR", build.get());
    }

}