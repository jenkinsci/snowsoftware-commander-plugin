package com.embotics.vlm.plugin.actions;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.embotics.vlm.plugin.VCommanderEnvironmentContributingAction;

import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.VariableResolver;

/**
 * Utility class with helper methods for the Jenkins plugin 
 * @author btarczali
 *
 */
public class PluginUtils {
	
	/**
	 *	Adds a new environment variable to the jenkins build context
	 *	The scope of the varibale is the current run only. 
	 *
	 * @param run		context run
	 * @param listener	context listener
	 * @param key		environment variable name
	 * @param value		environment variable value
	 */
	static void addEnvVariable(Run<?, ?> run, TaskListener listener, String key, String value) {
		listener.getLogger().println("Adding new environment variable: " + key + "=" + value);

		VCommanderEnvironmentContributingAction contributorAction = run.getAction(VCommanderEnvironmentContributingAction.class);
		if (contributorAction == null) {
			contributorAction = new VCommanderEnvironmentContributingAction();
			run.addAction(contributorAction);
		}
		contributorAction.add(key, value);
	}
	
	static boolean isNumericOrVariable(String value) {
		return StringUtils.isNotBlank(value) && (PluginUtils.hasVariable(value) || NumberUtils.isDigits(value));
	}
	
	static boolean hasVariable(String input) {
		VariableChecker resolver = new VariableChecker();
	    Util.replaceMacro(input, resolver);
	    return resolver.wasCalled();
	}
	
	/**
	 * A helper class, which tracks if variable substitution was triggered
	 */
	private static class VariableChecker implements VariableResolver<String> {
		boolean called = false;
		
        public String resolve(String name) {
        	called = true;
            return null;
        }
        
        public boolean wasCalled() {
        	return called;
        }
    };
	
}
