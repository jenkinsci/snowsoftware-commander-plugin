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
import java.net.ConnectException;
import java.util.Collections;
import java.util.List;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.embotics.vlm.rest.v30.client.model.VCommanderException;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

/**
 * vCommander Jenkins Build Step implementation
 * The wrapper for all vCommander Actions
 * 
 * @author btarczali
 */
public class VCommanderBuilder extends Builder implements SimpleBuildStep {
	private static final String PROPERTY_ACTION = "action";
	
	private final VCommanderAction action;

	
	@DataBoundConstructor
	public VCommanderBuilder(VCommanderAction action) {
		this.action = action;
	}

    public VCommanderAction getAction() {
        return action;
    }

	@Override
	public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
		listener.getLogger().println();
		listener.getLogger().println("========== Starting Commander build step: " + action.getDescriptor().getDisplayName() + " ==========");
		try {
			action.perform(run, listener);
		} catch(Exception e) {
			if(e.getCause() !=null && e.getCause() instanceof ConnectException) {
				throw new VCommanderException(Messages.VCommanderConfig_connection_failedConnection());
			}
			throw e;
		}
		listener.getLogger().println("========== Finished Commander build step: " + action.getDescriptor().getDisplayName() + " ==========");
		listener.getLogger().println();
	}

	@Extension
	@Symbol("vCommander")
	public static final class VCommanderBuilderDescriptor extends BuildStepDescriptor<Builder> {
		
        @Override
        public VCommanderBuilder newInstance(StaplerRequest req, JSONObject formData) throws hudson.model.Descriptor.FormException {
        	VCommanderAction action = bindJSONWithDescriptor(req, formData, PROPERTY_ACTION, VCommanderAction.class);
            return new VCommanderBuilder(action);
        }
        
		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return Messages.VCommanderBuilder_displayName();
		}

        public List<Descriptor<VCommanderAction>> getActions() {
        	Jenkins jenkinsInstance = Jenkins.getInstance();
        	return jenkinsInstance!=null ? jenkinsInstance.getDescriptorList(VCommanderAction.class) : Collections.<Descriptor<VCommanderAction>>emptyList();
        }
		
		////////////////////
		// form validations

		
		///////////////////
		// helper methods
		
	    private static <T> T bindJSONWithDescriptor(StaplerRequest req, JSONObject formData, String fieldName, Class<T> expectedClazz) throws hudson.model.Descriptor.FormException {
	        formData = formData.getJSONObject(fieldName);
	        if (formData == null || formData.isNullObject()) {
	            return null;
	        }
	        String clazzName = formData.optString("$class", null);
	        if (clazzName == null) {
	          // Fall back on the legacy stapler-class attribute.
	          clazzName = formData.optString("stapler-class", null);
	        }
	        if (clazzName == null) {
	            throw new FormException("No $class or stapler-class is specified", fieldName);
	        }
	        try {
	        	// To prevent build warning: Possible null pointer dereference
	        	Jenkins jenkinsInstance = Jenkins.getInstance();
	        	if(jenkinsInstance==null) {
	        		return null;
	        	}
	        	
	            @SuppressWarnings("unchecked")
	            Class<? extends Describable<?>> clazz = (Class<? extends Describable<?>>)jenkinsInstance.getPluginManager().uberClassLoader.loadClass(clazzName);
	            Descriptor<?> d = jenkinsInstance.getDescriptorOrDie(clazz);
	            @SuppressWarnings("unchecked")
	            T ret = (T)d.newInstance(req, formData);
	            return ret;
	        } catch(ClassNotFoundException e) {
	            throw new FormException(String.format("Failed to instantiate: class not found %s", clazzName), e, fieldName);
	        } catch(ClassCastException e) {
	            throw new FormException(String.format("Failed to instantiate: instantiated as %s but expected %s", clazzName, expectedClazz.getName()), e, fieldName);
	        }
	    }
		
	}

}
