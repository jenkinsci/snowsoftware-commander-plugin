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

import org.junit.Test;

import com.embotics.vlm.plugin.AbstractVCommanderActionTest;
import com.embotics.vlm.plugin.VCommanderBuilder;

import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;

/**
 * Unit tests for VCommanderBuilder
 * 
 * @author btarczali
 */
public class VCommanderWaitForRunWorkflowActionTest extends AbstractVCommanderActionTest {

    private final String taskId = "30";
    
    @Test
    public void build_Successful() throws Exception {
    	VCommanderWaitForRunWorkflowAction action = new VCommanderWaitForRunWorkflowAction(taskId, timeout, polling);
    	VCommanderBuilder vCommanderBuilder = new VCommanderBuilder(action);
        project.getBuildersList().add(vCommanderBuilder);
        
        vCommanderClient.setWorkflowId(40L);
        
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        
        //check if service was created
    	jenkins.assertLogContains("Waiting 20 minutes for workflow completion. Checking every 30 seconds ...", build);
    	jenkins.assertLogContains("Workflow successfully completed", build);
    }
    
    @Test
    public void build_NotConfigured() throws Exception {
        //pass in an empty taskId - not configured step
    	VCommanderWaitForRunWorkflowAction action = new VCommanderWaitForRunWorkflowAction("", timeout, polling);
    	VCommanderBuilder vCommanderBuilder = new VCommanderBuilder(action);
        project.getBuildersList().add(vCommanderBuilder);

        QueueTaskFuture<FreeStyleBuild> build = project.scheduleBuild2(0);
        jenkins.assertBuildStatus(Result.FAILURE, build);
        
        //check log
        jenkins.assertLogContains("Error in build step configuration. TaskId is not specified.", build.get());
    }
    
    @Test
    public void build_Failed() throws Exception {
    	VCommanderWaitForRunWorkflowAction action = new VCommanderWaitForRunWorkflowAction(taskId, timeout, polling);
    	VCommanderBuilder vCommanderBuilder = new VCommanderBuilder(action);
        project.getBuildersList().add(vCommanderBuilder);
        
        vCommanderClient.setThrowException(true, "Custom ERROR");
        
        QueueTaskFuture<FreeStyleBuild> build = project.scheduleBuild2(0);
        jenkins.assertBuildStatus(Result.FAILURE, build);
        
        //check log
        jenkins.assertLogContains("FATAL: Custom ERROR", build.get());
    }

}