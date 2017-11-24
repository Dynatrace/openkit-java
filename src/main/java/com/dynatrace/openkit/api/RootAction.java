package com.dynatrace.openkit.api;

/**
 * This interface provides the same functionality as Action and in addition allows to create child Actions
 */
public interface RootAction extends Action {
    /**
     * Enters a (child) Action with a specified name on this Action.
     *
     * @param actionName	name of the Action
     * @return				Action instance to work with
     */
    public Action enterAction(String actionName);
}
