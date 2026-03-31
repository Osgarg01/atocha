package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import simulator.factories.Factory;


public class Simulator implements JSONable, Observable<EcoSysObserver> {
	
	private Factory<Animal> animalsFactory;
	private Factory<Region> regionsFactory;
	private RegionManager regionManager;
	private List<Animal> animals;
	private double time;
	
	private List<EcoSysObserver> observers;
	
	
	public Simulator (int cols, int rows, int width, int length, Factory<Animal> animalsF, Factory<Region> regionsF) {
		
		if (cols <= 0) throw new IllegalArgumentException("El número de columnas debe ser positivo");
		if (rows <= 0) throw new IllegalArgumentException("El número de filas debe ser positivo");
		if (width <= 0) throw new IllegalArgumentException("La anchura debe ser positiva");
		if (length <= 0) throw new IllegalArgumentException("La altura debe ser positiva");
		if (animalsF == null) throw new IllegalArgumentException("La factoría de animales no puede ser nula");
		if (regionsF == null) throw new IllegalArgumentException("La factoría de regiones no puede ser nula");
		
		
		this.animalsFactory = animalsF;
		this.regionsFactory = regionsF;
		this.time = 0.0;
		this.animals = new ArrayList<>();
		
		this.regionManager = new RegionManager(cols,rows,width,length);
		
	}
	
	public void reset(int cols, int rows, int width, int height) { 
		this.animals.clear();
		
		this.regionManager = new RegionManager (cols,rows,width,height);
		
		this.time = 0.0;
		
		List<AnimalInfo> animalList = new ArrayList<>(animals);
		for (EcoSysObserver o : observers) {
			o.onReset(time, regionManager, animalList);
		}
	}
	
	private void setRegion(int row, int col, Region r) {
		regionManager.setRegion(row, col, r);
		
		for (EcoSysObserver o : observers) {
			o.onRegionSet(row, col, regionManager, r);
		}
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
		
		
		List<AnimalInfo> animalList = new ArrayList<>(animals);
		for (EcoSysObserver o : observers) {
			o.onAnimalAdded(time, regionManager, animalList, a);
		}
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
		
		List<AnimalInfo> animalList = new ArrayList<>(animals);
		for (EcoSysObserver o : observers) {
			o.onAdvance(time, regionManager, animalList, dt);
		}
	}

	@Override
	public JSONObject asJSON() {
		JSONObject info = new JSONObject();
		info.put("time", time);
		info.put("state", regionManager.asJSON());
		return info;
	}

	@Override
	public void addObserver(EcoSysObserver o) {
		if (!observers.contains(o)) {
			observers.add(o);
			
			List<AnimalInfo> animalList = new ArrayList<>(animals);
			o.onRegister(time, regionManager, animalList);
		}
		
	}

	@Override
	public void removeObserver(EcoSysObserver o) {
		observers.remove(o);
		
	}
	
	
	
}
