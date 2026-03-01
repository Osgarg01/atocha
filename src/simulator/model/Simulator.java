package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import simulator.factories.Factory;


public class Simulator implements JSONable {
	
	private Factory<Animal> animalsFactory;
	private Factory<Region> regionsFactory;
	private RegionManager regionManager;
	private List<Animal> animals;
	private double time;
	
	
	public Simulator (int cols, int rows, int width, int length, Factory<Animal> animalsF, Factory<Region> regionsF) {
		
		this.animalsFactory = animalsF;
		this.regionsFactory = regionsF;
		this.time = 0.0;
		this.animals = new ArrayList<>();
		
		this.regionManager = new RegionManager(cols,rows,width,length);
		
	}
	
	private void setRegion(int row, int col, Region r) {
		regionManager.setRegion(row, col, r);
	}

	public void setRegion(int row, int col, JSONObject rJson) {
		// Crea la region usando la factoria y llama al metodo privado
		Region r = regionsFactory.createInstance(rJson);
		setRegion(row, col, r);
	}
	
	private void addAnimal(Animal a) {
		// Añade a la lista principal y registra en el gestor
		animals.add(a);
		regionManager.registerAnimal(a);
	}

	public void addAnimal(JSONObject aJson) {
		// Crea el animal con la factoria 
		Animal a = animalsFactory.createInstance(aJson);
		addAnimal(a);
	}
	
	public MapInfo getMapInfo() {
		return regionManager;
	}

	public List<? extends AnimalInfo> getAnimals() {
		// Devuelve una lista de solo lectura
		return Collections.unmodifiableList(animals);
	}

	public double getTime() {
		return time;
	}
		
	
	public void advance(double dt) {
		
		time += dt;

		
		List<Animal> deadAnimals = new ArrayList<>();
		for (Animal a : animals) {
			if (a.getState() == State.DEAD) {
				deadAnimals.add(a);
				regionManager.unregisterAnimal(a);
			}
		}
		animals.removeAll(deadAnimals);

		
		for (Animal a : animals) {
			a.update(dt);
			regionManager.updateAnimalRegion(a);
		}

	
		regionManager.updateAllRegions(dt);

	
		List<Animal> babies = new ArrayList<>();
		for (Animal a : animals) {
			if (a.isPregnant()) {
				babies.add(a.deliverBaby());
			}
		}
		

		for (Animal baby : babies) {
			addAnimal(baby);
		}
	}

	@Override
	public JSONObject asJSON() {
		JSONObject info = new JSONObject();
		info.put("time", time);
		info.put("state", regionManager.asJSON());
		return info;
	}
	
	
	
}
