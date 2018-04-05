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

import java.util.HashMap;
import java.util.Map;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;

/**
 * Extension for Jenkins to inject Environment variables at runtime, 
 * which are visible for other build steps
 * 
 * @author btarczali
 */
public class VCommanderEnvironmentContributingAction implements EnvironmentContributingAction {
	private final Map<String, String> customEnvMapping = new HashMap<String, String>();

	public void add(String k, String v) {
		customEnvMapping.put(k, v);
	}

	@Override
	public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
		env.putAll(customEnvMapping);
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
	
	/**
	 * @return the value associated the the key parameter
	 */
	public String getEnvValue(String key) {
		return customEnvMapping.get(key);
	}

}