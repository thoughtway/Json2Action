
package com.shntec.json2action.demo;


public abstract class ActionHandler {


    public abstract ResponseBase doAction();

    public abstract String getJsonSchema();

    public abstract ActionBase getAction();

}
