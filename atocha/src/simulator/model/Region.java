package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Region implements Entity, FoodSupplier, AnimalInfo {

	protected List<Animal> animals;
	
	public Region() {
		animals = new ArrayList<>();
	}
	
	public final void addAnimal(Animal a) {
		if (a != null && !animals.contains(a)) {
			animals.add(a);
		}
	}
			
	public final void removeAnimal(Animal a) {
		animals.remove(a);
	}
	
	public final List<Animal> getAnimals(){
		return Collections.unmodifiableList(animals);
	}
	
	@Override
	public JSONObject asJSON() {
		JSONObject info = new JSONObject();
		JSONArray ja = new JSONArray();
		for (Animal a : animals) {
			ja.put(a.asJSON());
		}
		info.put("animals", ja);
		return info;
	}
	
}
