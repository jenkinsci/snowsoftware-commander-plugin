package com.embotics.vlm.plugin;

import java.io.IOException;
import java.util.Map;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * Jenkins will call this contributor when it's time to save environmental variables. In PluginUtils we save any variables to an instance of {@link VCommanderActionRunEnvironmentVariables}. We now retrieve
 * this instance from the run and persist the variables for the scope of this run.
 * 
 * Note that if there are global environmental variables with same name, these run scoped variables will not override.
 * 
 * This is used for the Jenkins project is in Pipeline mode. For Freestyle project, see {@link VCommanderEnvironmentContributingAction}.
 * 
 * Note the @Extension annotation. Without this, the buildEnvironmentFor will not be called.
 */
@Extension
public class VCommanderEnvironmentContributor extends EnvironmentContributor {
	
	@Override
	public void buildEnvironmentFor(Run run, EnvVars envs, TaskListener listener)	throws IOException, InterruptedException {
		
		VCommanderActionRunEnvironmentVariables runVariables = run.getAction(VCommanderActionRunEnvironmentVariables.class);
		if (runVariables != null) {
			Map<String, String> vars = runVariables.getVars();
			for (Map.Entry<String,String> entry : vars.entrySet()) {
				envs.put(entry.getKey(), entry.getValue()); 
			}
		} 
		
		super.buildEnvironmentFor(run, envs, listener);
	}

}
