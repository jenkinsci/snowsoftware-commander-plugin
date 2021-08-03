package com.embotics.vlm.plugin;

import java.util.HashMap;
import java.util.Map;

import hudson.model.Action;

/**
 *	Stores environmental variables that are scoped to a particular run. This class is simply a container to capture the variable information and is then saved against the 
 * 	current run. {@link VCommanderEnvironmentContributor} is called by Jenkins itself and this class updates the environmental variable list of this run.
 *
 *	This is only used for the Jenkins project is in Pipelie mode. For Freestyle project, see {@link VCommanderEnvironmentContributingAction}.
 */
public class VCommanderActionRunEnvironmentVariables implements Action {
	
	private final Map<String, String> customEnvMapping = new HashMap<String, String>();

	public void add(String k, String v) {
		customEnvMapping.put(k, v);
	}
	
	public String getEnvValue(String key) {
		return customEnvMapping.get(key);
	}
	
	public Map<String, String> getVars() {
		return customEnvMapping;
	}

	@Override
	public String getIconFileName() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public String getUrlName() {
		return null;
	}
	
	

}