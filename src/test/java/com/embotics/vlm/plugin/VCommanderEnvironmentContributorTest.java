package com.embotics.vlm.plugin;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.util.LogTaskListener;

public class VCommanderEnvironmentContributorTest {
	
	private static final Logger LOGGER = Logger.getLogger(Run.class.getName());
	
	@Rule
    public JenkinsRule jenkins = new JenkinsRule();
	
	@Test
	public void addEnvVarsFromAction() throws IOException, InterruptedException {
		
		final String var1Name = "var1";
		final String var2Name = "var2";
		final String var1Value = "value1";
		final String var2Value = "value2";
				
		// Setup the project to run		
		FreeStyleProject project = jenkins.createFreeStyleProject();
		LogTaskListener listener = new LogTaskListener(LOGGER, Level.INFO);
		FreeStyleBuild run = new FreeStyleBuild(project);
		
		// Create our custom action and assign some variables to it. Attach it to the run.
		VCommanderActionRunEnvironmentVariables envVarsfromAction = new VCommanderActionRunEnvironmentVariables();
		envVarsfromAction.add(var1Name, var1Value);
		envVarsfromAction.add(var2Name, var2Value);
		run.addAction(envVarsfromAction);
		
		EnvVars envVars = new EnvVars();
		VCommanderEnvironmentContributor contributor = new VCommanderEnvironmentContributor();		
		contributor.buildEnvironmentFor(run, envVars, listener);
		
		// Make sure the variables we added in the custom action are set on the environmental variable param.
		String actualValue1 = envVars.get(var1Name);
		String actualValue2 = envVars.get(var2Name);
		Assert.assertEquals(var1Value, actualValue1);
		Assert.assertEquals(var2Value, actualValue2);
		
	}

}
