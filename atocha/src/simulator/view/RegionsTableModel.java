package simulator.view;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.Diet;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class RegionsTableModel extends AbstractTableModel implements EcoSysObserver {
	
	
private Controller ctrl;
	
	// Nombres de las columnas precalculados
	private String[] columnNames;
	
	// Estructura interna para guardar los datos de cada fila
	private record RegionRow(int row, int col, String desc, Map<Diet, Integer> dietCounts) {}
	private List<RegionRow> regionRows;
	
	RegionsTableModel(Controller ctrl) {
		this.ctrl = ctrl;
		this.regionRows = new ArrayList<>();

		// Inicializar las columnas
		Diet[] diets = Diet.values();
		columnNames = new String[3 + diets.length];
		columnNames[0] = "Row";
		columnNames[1] = "Col";
		columnNames[2] = "Desc.";
		
		for (int i = 0; i < diets.length; i++) {
			columnNames[3 + i] = diets[i].name();
		}

		//Registrar this como observador
		this.ctrl.addObserver(this);
	}
	
	private void updateData(MapInfo map) {
		if (map == null) return;
		
		SwingUtilities.invokeLater(() -> {
			regionRows.clear();
			
			// Recorremos el iterador de regiones que implementamos en MapInfo
			for (MapInfo.RegionData rd : map) {
				// Creamos un mapa para contar los animales por dieta en esta regi�n concreta
				Map<Diet, Integer> counts = new HashMap<>();
				
				// Recorremos todos los animales de la regi�n
				for (AnimalInfo a : rd.r().getAnimalsInfo()) {
					Diet d = a.getDiet();
					counts.put(d, counts.getOrDefault(d, 0) + 1);
				}
				
				// A�adimos la fila a nuestra lista interna 
				regionRows.add(new RegionRow(rd.row(), rd.col(), rd.r().toString(), counts));
			}
		
			fireTableDataChanged();
		});
	}

	@Override
	public int getRowCount() {
		return regionRows.size();
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
		RegionRow r = regionRows.get(rowIndex);
		
		// Las 3 primeras columnas son fijas 
		switch (columnIndex) {
			case 0:
				return r.row();
			case 1:
				return r.col();
			case 2:
				return r.desc();
			default:
				// Si es de la columna 3 en adelante, calculamos la dieta
				Diet d = Diet.values()[columnIndex - 3];
				return r.dietCounts().getOrDefault(d, 0); // Devolvemos 0 si no hay animales de esa dieta
		}
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		//Aqui si
		updateData(map);
		
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		updateData(map);
		
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		updateData(map);
		
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		updateData(map);
	}

	@Override
	public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		updateData(map);
	}

}
