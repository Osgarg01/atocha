package simulator.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import simulator.model.State;

public class SpeciesTableModel extends AbstractTableModel implements EcoSysObserver {
	
private Controller ctrl;
	
	
	private List<String> species; // Guarda el orden de las filas
	private Map<String, Map<State, Integer>> counts; // Cuenta de animales 
	private String[] columnNames;
	
	
	SpeciesTableModel(Controller ctrl) {
		this.ctrl = ctrl;
		this.species = new ArrayList<>();
		this.counts = new HashMap<>();

		// Calcular din�micamente los nombres de las columnas
		// La primera columna es Species, y las siguientes son los estados
		State[] states = State.values();
		columnNames = new String[states.length + 1];
		columnNames[0] = "Species";
		for (int i = 0; i < states.length; i++) {
			columnNames[i + 1] = states[i].name();
		}

		// Registrar this como observador
		this.ctrl.addObserver(this);
	}
	
	private void updateData(List<AnimalInfo> animals) {
		SwingUtilities.invokeLater(() -> {
			// Limpiar los datos actuales
			species.clear();
			counts.clear();

			// Recorrer la lista para descubrir las especies y contar sus estados
			for (AnimalInfo a : animals) {
				String genCode = a.getGeneticCode();
				State state = a.getState();

				// Si encontramos un nuevo c�digo gen�tico, lo registramos
				if (!counts.containsKey(genCode)) {
					counts.put(genCode, new HashMap<>());
					species.add(genCode);
				}

				// Sumamos 1 al contador de ese estado para esa especie
				Map<State, Integer> speciesCounts = counts.get(genCode);
				speciesCounts.put(state, speciesCounts.getOrDefault(state, 0) + 1);
			}

			// Avisar a la tabla gr�fica de que los datos internos han cambiado
			fireTableDataChanged();
		});
	}
	
	

	@Override
	public int getRowCount() {
		
		return species.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		String genCode = species.get(rowIndex);
		
		if (columnIndex == 0) {
			return genCode; // La primera columna es el nombre de la especie
		} else {
			// Para las dem�s columnas, sacamos el estado din�micamente seg�n el �ndice
			State state = State.values()[columnIndex - 1];
			// Devolvemos la cuenta, o 0 si no hay ning�n animal en ese estado
			return counts.get(genCode).getOrDefault(state, 0);
		}
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		updateData(animals);
		
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		updateData(animals);
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		updateData(animals);
		
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		
		
	}

	@Override
	public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		updateData(animals);
		
	}

}
