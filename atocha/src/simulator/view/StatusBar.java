package simulator.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class StatusBar extends JPanel implements EcoSysObserver {

	private Controller ctrl;
	
	// Atributos visuales (JLabels)
	private JLabel timeLabel;
	private JLabel animalsLabel;
	private JLabel dimensionLabel;

	StatusBar(Controller ctrl) {
		this.ctrl = ctrl;
		initGUI();
		// Registrar this como observador en el controlador
		this.ctrl.addObserver(this);
	}

	private void initGUI() {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.setBorder(BorderFactory.createBevelBorder(1)); // 1 = BevelBorder.LOWERED

		// 1. Inicializar los JLabels con un texto por defecto
		timeLabel = new JLabel("Time: 0.000");
		animalsLabel = new JLabel("Total Animals: 0");
		dimensionLabel = new JLabel("Dimension: ");

		// 2. Añadir los componentes al panel con separadores
		this.add(timeLabel);
		this.add(createSeparator());

		this.add(animalsLabel);
		this.add(createSeparator());

		this.add(dimensionLabel);
	}

	// Método auxiliar para no duplicar el código del separador
	private JSeparator createSeparator() {
		JSeparator s = new JSeparator(JSeparator.VERTICAL);
		s.setPreferredSize(new Dimension(10, 20));
		return s;
	}

	// Método auxiliar para actualizar la interfaz de forma segura
	private void updateInfo(double time, MapInfo map, List<AnimalInfo> animals) {
		// Usamos SwingUtilities.invokeLater para asegurarnos de que la interfaz 
		// gráfica se actualiza en el hilo correcto (Event Dispatch Thread)
		SwingUtilities.invokeLater(() -> {
			timeLabel.setText(String.format("Time: %.3f", time));
			animalsLabel.setText("Total Animals: " + animals.size());
			if (map != null) {
				dimensionLabel.setText("Dimension: " + map.getWidth() + "x" + map.getHeight() + " (" + map.getCols() + "x" + map.getRows() + ")");
			}
		});
	}

	// --- MÉTODOS DE LA INTERFAZ EcoSysObserver ---

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		updateInfo(time, map, animals);
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		updateInfo(time, map, animals);
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		updateInfo(time, map, animals);
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		// Cambiar una región no altera el número de animales ni el tiempo general
		// pero si quisieras podrías llamar a updateInfo() de todos modos.
	}

	@Override
	public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		updateInfo(time, map, animals);
	}
}