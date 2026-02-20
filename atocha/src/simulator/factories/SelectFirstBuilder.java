package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectFirst;

public class SelectFirstBuilder extends Builder<SelectFirst>{

    public SelectFirstBuilder() {
        super("first", "Select the first animal in the list");
    }

    @Override
    protected SelectFirst createInstance(JSONObject data) {
        return new SelectFirst();
    }

}
