package com.embotics.vlm.rest.v30.client.model;

/**
 * A list of workflow target types
 * It is used by the run command workflow functionality 
 * 
 * @author btarczali
 */
public enum WorkflowTargetType {
	
    VIRTUAL_MACHINE,
    VIRTUAL_APP,
    STACK,
    DB_INSTANCE,
    LOAD_BALANCER,
    AUTO_SCALING_GROUP,
    MANAGEMENTSERVER,
    NO_INVENTORY_TARGET;

	// we cannot add the "ANY_INVENTORY_TYPE" to the enum, since this enum is for a concrete target
	// the "ANY_INVENTORY_TYPE" is required to validate this target type against a workflow definition which is compatible with ANY_INVENTORY_TYPE types
	public static final String WORKFLOW_TYPE_FOR_ANY_INVENTORY_TYPE = "ANY_INVENTORY_TYPE";

}
