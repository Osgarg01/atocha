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
	
	
	public Simulator (int rows, int cols, int width, int length, Factory<Animal> animalsF, Factory<Region> regionsF) {
		
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
		// Crea la regi�n usando la factor�a y llama al m�todo privado
		Region r = regionsFactory.createInstance(rJson);
		setRegion(row, col, r);
	}
	
	private void addAnimal(Animal a) {
		// A�ade a la lista principal y registra en el gestor
		animals.add(a);
		regionManager.registerAnimal(a);
	}

	public void addAnimal(JSONObject aJson) {
		// Crea el animal usando la factor�a y llama al m�todo privado
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
		// 1. Incrementar el tiempo
		time += dt;

		// 2. Quitar animales muertos
		//Usamos lista auxiliar
		List<Animal> deadAnimals = new ArrayList<>();
		for (Animal a : animals) {
			if (a.getState() == State.DEAD) {
				deadAnimals.add(a);
				regionManager.unregisterAnimal(a);
			}
		}
		animals.removeAll(deadAnimals);

		// 3. Actualizar cada animal
		for (Animal a : animals) {
			a.update(dt);
			regionManager.updateAnimalRegion(a);
		}

		// 4. Actualizar todas las regiones
		regionManager.updateAllRegions(dt);

		// 5. Reproducci�n (Beb�s)
		List<Animal> babies = new ArrayList<>();
		for (Animal a : animals) {
			if (a.isPregnant()) {
				babies.add(a.deliverBaby());
			}
		}
		
		// A�adimos los beb�s a la simulaci�n
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
