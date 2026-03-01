package simulator.factories;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;
import simulator.model.Sheep;

public class SheepBuilder extends Builder<Animal>{
    private Factory<SelectionStrategy> strategyFactory;
    
    public SheepBuilder(Factory<SelectionStrategy> strategyFactory) {
        super("sheep", "A sheep");
        this.strategyFactory = strategyFactory;
    }

    @Override
    protected Sheep createInstance(JSONObject data) {
        // Parse mate strategy
        SelectionStrategy mateStrategy;
        if(data.has("mate_strategy")){
            mateStrategy = strategyFactory.createInstance(data.getJSONObject("mate_strategy"));
        } else {
            mateStrategy = new SelectFirst();
        }
        
        // Parse danger strategy
        SelectionStrategy dangerStrategy;
        if(data.has("danger_strategy")) {
            dangerStrategy = strategyFactory.createInstance(data.getJSONObject("danger_strategy"));
        } else {
            dangerStrategy = new SelectFirst();
        }
        
        // Parse position
        Vector2D pos = null;
        if(data.has("pos")) {
            JSONObject posObj = data.getJSONObject("pos");
            JSONArray xRange = posObj.getJSONArray("x_range");
            JSONArray yRange = posObj.getJSONArray("y_range");
            
            double xMin = ((Number) xRange.get(0)).doubleValue();
            double xMax = ((Number) xRange.get(1)).doubleValue();
            double yMin =((Number) yRange.get(0)).doubleValue();
            double yMax =((Number) yRange.get(1)).doubleValue();
            
            // Generate random position within the ranges
            double x = xMin + Utils.RAND.nextDouble() * (xMax - xMin);
            double y = yMin + Utils.RAND.nextDouble() * (yMax - yMin);
            
            pos = new Vector2D(x, y);
        }
        
        return new Sheep(mateStrategy, dangerStrategy, pos);
    }
}
