package simulator.factories;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;
import simulator.model.Wolf;

public class WolfBuilder extends Builder<Wolf>{
    private Factory<SelectionStrategy> sFactory;
    
    
    public WolfBuilder(Factory<SelectionStrategy> sFactory) {
        super("wolf", "A wolf");
        this.sFactory = sFactory;
}

    @Override
    protected Wolf createInstance(JSONObject data) {
         SelectionStrategy mateStrategy;
        if(data.has("mate_strategy")){
            mateStrategy = sFactory.createInstance(data.getJSONObject("mate_strategy"));
        } else {
            mateStrategy = new SelectFirst();
        }
        
        // Parse danger strategy
        SelectionStrategy dangerStrategy;
        if(data.has("hunt_strategy")) {
            dangerStrategy = sFactory.createInstance(data.getJSONObject("hunt_strategy"));
        } else {
            dangerStrategy = new SelectFirst();
        }
        //FALTA ALGUNAS REVISIONES DE POSICIONES Y EXCEPCIONES Y TAL
        
        // Parse position
        Vector2D pos = null;
        if(data.has("pos")) {
            JSONObject posObj = data.getJSONObject("pos");
            JSONArray xRange = posObj.getJSONArray("x_range");
            JSONArray yRange = posObj.getJSONArray("y_range");
            
            double xMin = xRange.getDouble(0);
            double xMax = xRange.getDouble(1);
            double yMin = yRange.getDouble(0);
            double yMax = yRange.getDouble(1);
            
            // genero numero aleatorio entre xMin y xMax, y entre yMin y yMax
            double x = xMin + Utils.RAND.nextDouble() * (xMax - xMin);
            double y = yMin + Utils.RAND.nextDouble() * (yMax - yMin);
            
            pos = new Vector2D(x, y);
        }
        
        return new Wolf(mateStrategy, dangerStrategy, pos);
    }
    }
