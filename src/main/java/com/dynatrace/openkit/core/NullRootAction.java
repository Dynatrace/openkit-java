package com.dynatrace.openkit.core;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.RootAction;

public class NullRootAction extends NullAction implements RootAction {

    @Override
    public Action enterAction(String actionName) {
        return new NullAction(this);
    }
}
