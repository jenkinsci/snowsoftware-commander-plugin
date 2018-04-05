package com.embotics.vlm.rest.v30.client.model;

/**
 * Data class used in response received from vCommander when a service request is deployed
 * 
 * @author btarczali
 */
public class DeployedComponentInfo {
	private final int serviceIndex;
	private final int componentIndex;
	private final String componentType;
	private final String componentName;
	
	public DeployedComponentInfo(int serviceIndex, int componentIndex, String componentType, String componentName) {
		this.serviceIndex = serviceIndex;
		this.componentIndex = componentIndex;
		this.componentType = componentType;
		this.componentName = componentName;
	}

	/**
	 * @return The index of the service within the request (1 based index)
	 */
	public int getServiceIndex() {
		return serviceIndex;
	}

	/**
	 * @return The index of the component within the service (1 based index)
	 */
	public int getComponentIndex() {
		return componentIndex;
	}

	/**
	 * @return The type of the component, example: VIRTUAL_MACHINE
	 */
	public String getComponentType() {
		return componentType;
	}

	/**
	 * @return The name of the component
	 */
	public String getComponentName() {
		return componentName;
	}
}
