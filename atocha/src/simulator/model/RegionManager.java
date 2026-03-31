package simulator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Vector2D;

public class RegionManager implements AnimalMapView {
	private int cols;
	private int rows;
	private int width;
	private int height;
	//Dimensiones de región
	private int regionWidth;
	private int regionHeight;
	
	private Region[][] regions;
	private Map<Animal, Region> animalRegion;
	
	public RegionManager(int cols, int rows, int width, int height) {
		// VALIDACIONES DE PARÁMETROS
		if (cols <= 0) {
			throw new IllegalArgumentException("El número de columnas debe ser positivo");
		}
		if (rows <= 0) {
			throw new IllegalArgumentException("El número de filas debe ser positivo");
		}
		if (width <= 0) {
			throw new IllegalArgumentException("La anchura del mapa debe ser positiva");
		}
		if (height <= 0) {
			throw new IllegalArgumentException("La altura del mapa debe ser positiva");
		}
		
		this.cols = cols;
		this.rows = rows;
		this.width = width;
		this.height = height;
		this.regionWidth = width / cols;
		this.regionHeight = height / rows;
		
		this.regions = new Region[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				this.regions[i][j] = new DefaultRegion();
			}
		}
		
		this.animalRegion = new HashMap<>();
		
	}


	@Override
	public int getCols() {
		
		return cols;
	}

	@Override
	public int getRows() {
		
		return rows;
	}

	@Override
	public int getWidth() {
	
		return width;
	}

	@Override
	public int getHeight() {

		return height;
	}

	@Override
	public int getRegionWidth() {

		return regionWidth;
	}

	@Override
	public int getRegionHeight() {

		return regionHeight;
	}

	@Override
	public double getFood(AnimalInfo a, double dt) {
		Region r = animalRegion.get((Animal) a);

		if (r != null) {
			return r.getFood(a, dt);
		} else {
			return 0.0;
		}
	}

	@Override
	public List<Animal> getAnimalsInRange(Animal e, Predicate<Animal> filter) {
List<Animal> inRange = new ArrayList<>();
		
		double range = e.getSightRange();
		Vector2D pos = e.getPosition();
		
		double minX = pos.getX() - range;
		double maxX = pos.getX() + range;
		double minY = pos.getY() - range;
		double maxY = pos.getY() + range;

		int colStart = (int) (minX / regionWidth);
		int colEnd = (int) (maxX / regionWidth);
		int rowStart = (int) (minY / regionHeight);
		int rowEnd = (int) (maxY / regionHeight);

		for (int r = rowStart; r <= rowEnd; r++) {
			for (int c = colStart; c <= colEnd; c++) {
				int actualRow = (r % rows + rows) % rows;
				int actualCol = (c % cols + cols) % cols;

				Region reg = regions[actualRow][actualCol];
				
				for (Animal other : reg.getAnimals()) {
					if (e != other && filter.test(other)) {
						if (pos.distanceTo(other.getPosition()) <= range) {
							inRange.add(other);
						}
					}
				}
			}
		}
		return inRange;
	}
	
	
	public void setRegion(int row, int col, Region r) {
		
		if (r == null) throw new IllegalArgumentException("Region no puede ser nula");
		if (row < 0 || row >= rows || col < 0 || col >= cols) throw new IllegalArgumentException("Las coordenadas de la región son inválidas");

		Region oldRegion = regions[row][col];
		
		// Movemos todos los animales de la región vieja a la nueva
		List<Animal> animalsToMove = new ArrayList<>(oldRegion.getAnimals());
		
		for (Animal a : animalsToMove) {
			oldRegion.removeAnimal(a);
			r.addAnimal(a);
			animalRegion.put(a, r);
		}
		
		regions[row][col] = r;
	}
	
	public void updateAllRegions(double dt) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				regions[i][j].update(dt);
			}
		}
	}
	
	public void registerAnimal(Animal a) {
		a.init(this);
		Region region = getRegionByPos(a.getPosition());
		region.addAnimal(a);
		animalRegion.put(a, region);
	}

	public void unregisterAnimal(Animal a) {
		Region region = animalRegion.get(a);
		if (region != null) {
			region.removeAnimal(a);
			animalRegion.remove(a);
		}
	}
	
	public void updateAnimalRegion(Animal a) {
		Region currentRegion = animalRegion.get(a);
		Region correctRegion = getRegionByPos(a.getPosition());

		if (currentRegion != correctRegion) {
			if (currentRegion != null) {
				currentRegion.removeAnimal(a);
			}
			correctRegion.addAnimal(a);
			animalRegion.put(a, correctRegion);
		}
	}
	//Funcion que nos dice la region en la que estamos pasando como parametro el
	//vector2D como posicion
	private Region getRegionByPos(Vector2D pos) {
		int col = (int) (pos.getX() / regionWidth);
		int row = (int) (pos.getY() / regionHeight);
		
		if (col >= cols) col = cols - 1;
		if (row >= rows) row = rows - 1;
		if (col < 0) col = 0;
		if (row < 0) row = 0;
		
		return regions[row][col];
	}
	
	@Override
	public JSONObject asJSON() {
		JSONObject info = new JSONObject();
		JSONArray regArray = new JSONArray();

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				JSONObject rObj = new JSONObject();
				rObj.put("row", i);
				rObj.put("col", j);
				rObj.put("data", regions[i][j].asJSON());
				regArray.put(rObj);
			}
		}

		info.put("regions", regArray);
		return info;
	}


	@Override
	public Iterator<RegionData> iterator() {
		return new Iterator<RegionData>() {
			private int currentRow = 0;
			private int currentCol = 0;

			@Override
			public boolean hasNext() {
				// Quedan elementos si la fila actual es menor que el total de filas
				return currentRow < rows && currentCol < cols;
			}

			@Override
			public RegionData next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				
				// Creamos el RegionData con la región actual
				MapInfo.RegionData data = new MapInfo.RegionData(currentRow, currentCol, regions[currentRow][currentCol]);
				
				// Avanzamos de izquierda a derecha (columnas)
				currentCol++;
				
				// Si llegamos al final de las columnas, pasamos a la siguiente fila
				if (currentCol >= cols) {
					currentCol = 0;
					currentRow++;
				}
				
				return data;
	}
		
	};
}
	
}


