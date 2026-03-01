package simulator.control;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.AnimalInfo;
import simulator.model.MapInfo;
import simulator.model.Simulator;
import simulator.view.SimpleObjectViewer;
import simulator.view.SimpleObjectViewer.ObjInfo;

public class Controller {

    private Simulator sim;

    public Controller(Simulator sim) {
    	if (sim == null) {
    		throw new IllegalArgumentException("El simulador no existe");
    	}
        this.sim = sim;
    }   

    public void loadData(JSONObject data){
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
                
                for(int R = rf; R <= rt; R++){
                    for(int C = cf; C <= ct; C++){
                        sim.setRegion(R, C, spec);
                    }
                }
            }
        }
        
        if(data.has("animals")){
            JSONArray animals = data.getJSONArray("animals");
            for(int i = 0; i < animals.length(); i++){
                JSONObject animalObj = animals.getJSONObject(i);
                int amount = animalObj.getInt("amount");
                JSONObject spec = animalObj.getJSONObject("spec");
                
                for(int j = 0; j < amount; j++){
                    sim.addAnimal(spec);
                }
            }
        }
    }
    
    // Método privado obligatorio para el visor
    private List<ObjInfo> toAnimalsInfo(List<? extends AnimalInfo> animals) {
        List<ObjInfo> ol = new ArrayList<>(animals.size());
        for (AnimalInfo a : animals) {
            ol.add(new ObjInfo(a.getGeneticCode(), (int) a.getPosition().getX(), (int) a.getPosition().getY(), (int)Math.round(a.getAge())+2));
        }
        return ol;
    }
    
    public void run(double t, double dt, boolean sv, OutputStream out){
        // Inicializar el visor según el enunciado
        SimpleObjectViewer view = null;
        if (sv) {
            MapInfo m = sim.getMapInfo();
            view = new SimpleObjectViewer("[ECOSYSTEM]", m.getWidth(), m.getHeight(), m.getCols(), m.getRows());
            view.update(toAnimalsInfo(sim.getAnimals()), sim.getTime(), dt);
        }

        JSONObject initState = sim.asJSON();
        
        while(sim.getTime() <= t){
            sim.advance(dt);
            
            // Actualizar visor
            if(sv) {
                view.update(toAnimalsInfo(sim.getAnimals()), sim.getTime(), dt);
            }
        }
        
        JSONObject finalState = sim.asJSON();
        JSONObject output = new JSONObject();
        output.put("in", initState);
        output.put("out", finalState);
        
        // El enunciado recomienda usar PrintStream
        PrintStream p = new PrintStream(out);
        p.println(output.toString());
        
        // Cerrar visor
        if (sv) {
            view.close();
        }
    }
}