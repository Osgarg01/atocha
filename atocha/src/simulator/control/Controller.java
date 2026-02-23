package simulator.control;

import java.io.OutputStream;
import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.Simulator;

public class Controller {

    Simulator sim;

    public Controller(Simulator sim) {
    	if (sim == null) {
    		throw new IllegalArgumentException("El simulador no existe");
    	}
        this.sim = sim;
    }   

    public void loadData(JSONObject data){
        // First, load regions (optional) - must be done before adding animals
        if(data.has("regions")){
            JSONArray regions = data.getJSONArray("regions");
            for(int i = 0; i < regions.length(); i++){
                JSONObject regionObj = regions.getJSONObject(i);
                JSONArray rowRange = regionObj.getJSONArray("row");
                JSONArray colRange = regionObj.getJSONArray("col");
                JSONObject spec = regionObj.getJSONObject("spec");
                
                int rf = rowRange.getInt(0);
                int rt = rowRange.getInt(1);
                int cf = colRange.getInt(0);
                int ct = colRange.getInt(1);
                
                // Set region for each cell in the range
                for(int R = rf; R <= rt; R++){
                    for(int C = cf; C <= ct; C++){
                        sim.setRegion(R, C, spec);
                    }
                }
            }
        }
        
        // Then, load animals
        if(data.has("animals")){
            JSONArray animals = data.getJSONArray("animals");
            for(int i = 0; i < animals.length(); i++){
                JSONObject animalObj = animals.getJSONObject(i);
                int amount = animalObj.getInt("amount");
                JSONObject spec = animalObj.getJSONObject("spec");
                
                // Add N animals of this type
                for(int j = 0; j < amount; j++){
                    sim.addAnimal(spec);
                }
            }
        }
    }
    
    public void run(double t, double dt, boolean sv, OutputStream out){
        // Get initial state
        JSONObject initState = sim.asJSON();
        
        // Run simulation until time exceeds t
        while(sim.getTime() <= t){
            sim.advance(dt);
            
            // Show simulation if sv is true
            if(sv){
                // TODO: Implement object viewer display
            }
        }
        
        // Get final state
        JSONObject finalState = sim.asJSON();
        
        // Create output JSON
        JSONObject output = new JSONObject();
        output.put("in", initState);
        output.put("out", finalState);
        
        // Write to output stream
        PrintWriter pw = new PrintWriter(out);
        pw.println(output.toString());
        pw.flush();
    }
}
