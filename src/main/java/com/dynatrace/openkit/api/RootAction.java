package com.dynatrace.openkit.api;

/**
 * This interface provides functionality to create (child) Actions, report events/values/errors and tag web requests.
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
