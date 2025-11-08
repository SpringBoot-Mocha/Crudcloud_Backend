package com.crudzaso.CrudCloud.domain.enums;

/**
 * Database instance possible states
 *
 * CREATING - The instance is being created
 * RUNNING - The instance is active and running
 * SUSPENDED - The instance is suspended
 * DELETED - The instance has been deleted
 */
public enum InstanceStatus {
    CREATING,
    RUNNING,
    SUSPENDED,
    DELETED
}
