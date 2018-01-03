package com.dynatrace.openkit.api;

/**
 * This interface provides the same functionality as Action, additionally it allows to create child Actions
 */
public interface RootAction extends Action {

    /**
     * Enters a (child) Action with a specified name on this Action.
     *
     * @param actionName name of the Action
     * @return Action instance to work with
     */
    Action enterAction(String actionName);
}
