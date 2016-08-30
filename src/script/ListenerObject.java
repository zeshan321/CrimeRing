package script;

import javax.script.ScriptEngine;

public class ListenerObject {

    public String scriptID;
    public ScriptEngine engine;
    public String method;

    public ListenerObject(String scriptID, ScriptEngine engine, String method) {
        this.scriptID = scriptID;
        this.engine = engine;
        this.method = method;
    }
}
