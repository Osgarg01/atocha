package simulator.view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class MapWindow extends JFrame implements EcoSysObserver {

	private Controller ctrl;
	private AbstractMapViewer viewer;
	private Frame parent;

	MapWindow(Frame parent, Controller ctrl) {
		super("[MAP VIEWER]");
		this.ctrl = ctrl;
		this.parent = parent;
		intiGUI();
		// Registrar this como observador
		this.ctrl.addObserver(this);
	}

	private void intiGUI() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		// 1. Poner contentPane como mainPanel
		setContentPane(mainPanel);

		// 2. Crear el viewer y a�adirlo a mainPanel (en el centro)
		this.viewer = new MapViewer();
		mainPanel.add(this.viewer, BorderLayout.CENTER);

		// 3. En el m�todo windowClosing, eliminar �MapWindow.this� de los observadores
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				ctrl.removeObserver(MapWindow.this);
			}
		});

		pack();
		if (this.parent != null) {
			setLocation(
				this.parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2,
				this.parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
		}
		
		setResizable(false);
		setVisible(true);
	}

	// --- M�TODOS DE EcoSysObserver ---

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		SwingUtilities.invokeLater(() -> {
			this.viewer.reset(time, map, animals);
			pack();
		});
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		SwingUtilities.invokeLater(() -> {
			this.viewer.reset(time, map, animals);
			pack();
		});
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		// No es estrictamente necesario repintar aqu� porque onAdvance lo har� continuamente
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
	}

	@Override
	public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		SwingUtilities.invokeLater(() -> {
			this.viewer.update(animals, time);
		});
	}
}