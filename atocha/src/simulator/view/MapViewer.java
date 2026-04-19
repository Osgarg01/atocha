package simulator.view;

import simulator.model.AnimalInfo;
import simulator.model.MapInfo;
import simulator.model.State; // Importaci�n del estado

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public class MapViewer extends AbstractMapViewer {
	private static final int MIN_ANIMAL_SIZE = 6;
	private static final int MAX_ANIMAL_SIZE = 18;
	private static final double AGE_SIZE_FACTOR = 0.8;

	private int width;
	private int height;
	private int rows;
	private int cols;
	int rWidth;
	int rHeight;

	// Cambiado a State (asumiendo que est� en simulator.model.State)
	State currState;

	volatile private Collection<AnimalInfo> objs;
	volatile private Double time;

	private static class SpeciesInfo {
		private Integer count;
		private Color color;

		SpeciesInfo(Color color) {
			count = 0;
			this.color = color;
		}
	}

	Map<String, SpeciesInfo> kindsInfo = new HashMap<>();

	private Font textFont = new Font("Arial", Font.BOLD, 12);
	private boolean showHelp;

	public MapViewer() {
		initGUI();
	}

	private void initGUI() {
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyChar()) {
				case 'h':
					showHelp = !showHelp;
					repaint();
					break;
				case 's':
					// Ciclo circular para los estados
					State[] states = State.values();
					if (currState == null) {
						currState = states[0]; // Del null pasamos al primero
					} else {
						int nextIndex = currState.ordinal() + 1;
						if (nextIndex < states.length) {
							currState = states[nextIndex]; // Siguiente estado
						} else {
							currState = null; // Del �ltimo pasamos a null
						}
					}
					repaint();
					break;
				default:
				}
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				requestFocus(); 
			}
		});

		currState = null;
		showHelp = true;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D gr = (Graphics2D) g;
		gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g.setFont(textFont);
		gr.setBackground(Color.WHITE);
		gr.clearRect(0, 0, width, height);

		if (objs != null)
			drawObjects(gr, objs, time);

		// Mostrar el texto de ayuda si showHelp es true
		if (showHelp) {
			gr.setColor(Color.RED);
			gr.drawString("h: toggle help", 10, 20);
			gr.drawString("s: show animals of a specific state", 10, 35);
		}
	}

	private boolean visible(AnimalInfo a) {
		// Devolver true si currState es null o si coincide con el estado del animal
		return currState == null || a.getState() == currState;
	}

	private void drawObjects(Graphics2D g, Collection<AnimalInfo> animals, Double time) {

		// Dibujar el grid de regiones
		g.setColor(Color.LIGHT_GRAY);
		for (int i = 0; i <= cols; i++) { // L�neas verticales
			g.drawLine(i * rWidth, 0, i * rWidth, height);
		}
		for (int i = 0; i <= rows; i++) { // L�neas horizontales
			g.drawLine(0, i * rHeight, width, i * rHeight);
		}
		
		// Dibujar los animales
		for (AnimalInfo a : animals) {
			if (!visible(a))
				continue;

			SpeciesInfo speciesInfo = kindsInfo.get(a.getGeneticCode());

			// Si espInfo es null, lo a�adimos y generamos color
			if (speciesInfo == null) {
				// Usamos ViewUtils (aseg�rate de que est� en el paquete o importado)
				speciesInfo = new SpeciesInfo(ViewUtils.getColor(a.getGeneticCode()));
				kindsInfo.put(a.getGeneticCode(), speciesInfo);
			}
			
			// Incrementar el contador
			speciesInfo.count++;

			// Dibujar el animal con un tama\u00f1o m\u00ednimo mayor para mejorar visibilidad.
			g.setColor(speciesInfo.color);
			int size = Math.max(MIN_ANIMAL_SIZE,
					Math.min(MAX_ANIMAL_SIZE, (int) Math.round(a.getAge() * AGE_SIZE_FACTOR) + MIN_ANIMAL_SIZE));
			int x = (int) a.getPosition().getX() - size / 2;
			int y = (int) a.getPosition().getY() - size / 2;
			g.fillOval(x, y, size, size);
		}

		// Dibujar la etiqueta del estado visible
		if (currState != null) {
			g.setColor(Color.BLUE);
			drawStringWithRect(g, 10, height - 40, "State: " + currState.toString());
		}
		
		// Dibujar la etiqueta del tiempo
		g.setColor(Color.MAGENTA);
		drawStringWithRect(g, 10, height - 15, "Time: " + String.format("%.3f", time));
		
		// Dibujar la informaci�n de todas la especies
		int xOffset = 150; // Empezamos a la derecha del tiempo
		for (Entry<String, SpeciesInfo> e : kindsInfo.entrySet()) {
			g.setColor(e.getValue().color);
			drawStringWithRect(g, xOffset, height - 15, e.getKey() + ": " + e.getValue().count);
			
			// Movemos la posici�n de X para la siguiente especie
			xOffset += 100; 
			
			// Reiniciamos el contador
			e.getValue().count = 0; 
		}
	}

	void drawStringWithRect(Graphics2D g, int x, int y, String s) {
		Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
		g.drawString(s, x, y);
		g.drawRect(x - 1, y - (int) rect.getHeight(), (int) rect.getWidth() + 1, (int) rect.getHeight() + 5);
	}

	@Override
	public void update(List<AnimalInfo> objs, Double time) {
		// Almacenar y repintar
		this.objs = objs;
		this.time = time;
		repaint();
	}

	@Override
	public void reset(double time, MapInfo map, List<AnimalInfo> animals) {
		// Actualizar las dimensiones
		this.width = map.getWidth();
		this.height = map.getHeight();
		this.cols = map.getCols();
		this.rows = map.getRows();
		this.rWidth = map.getRegionWidth();
		this.rHeight = map.getRegionHeight();

		setPreferredSize(new Dimension(map.getWidth(), map.getHeight()));

		update(animals, time);
	}
}