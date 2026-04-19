package simulator.view;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;
import simulator.launcher.Main;

class ControlPanel extends JPanel {

  private Controller ctrl;  
  private ChangeRegionsDialog changeRegionsDialog;

  private JToolBar toolaBar;  
  private JFileChooser fc;  
  private boolean stopped = true; // utilizado en los botones de run/stop  
  private JButton quitButton;
  
  private JButton openButton;
  private JButton viewerButton;
  private JButton regionsButton;
  private JButton runButton;
  private JButton stopButton;
  private JSpinner stepsSpinner; 
  private JTextField dtTextField;

 

  ControlPanel(Controller ctrl) {  
    this.ctrl = ctrl;  
    initGUI();  
  }

  private void initGUI() {  
    setLayout(new BorderLayout());  
    toolaBar = new JToolBar();  
    add(toolaBar, BorderLayout.PAGE_START);

    //inicializar el fc
    fc = new JFileChooser();
	fc.setCurrentDirectory(new File(System.getProperty("user.dir") + "/resources/examples"));
	
	changeRegionsDialog = new ChangeRegionsDialog(ctrl);
	
	//1. BOTON OPEN
	openButton = new JButton();
	openButton.setToolTipText("Load an input file");
	openButton.setIcon(new ImageIcon("resources/icons/open.png"));
	openButton.addActionListener((e) -> loadFile());
	toolaBar.add(openButton);
	toolaBar.addSeparator();
	
	//2.BOTON VIEWER
	viewerButton = new JButton();
	viewerButton.setToolTipText("Open Map Viewer");
	viewerButton.setIcon(new ImageIcon("resources/icons/viewer.png"));
	viewerButton.addActionListener((e) -> openViewer());
	toolaBar.add(viewerButton);
	
	//3.BOTON REGIONS
	regionsButton = new JButton();
	regionsButton.setToolTipText("Change Regions");
	regionsButton.setIcon(new ImageIcon("resources/icons/regions.png"));
	regionsButton.addActionListener((e) -> changeRegionsDialog.open(ViewUtils.getWindow(this)));
	toolaBar.add(regionsButton);
	toolaBar.addSeparator();
	
	//4.BOTON RUN
	runButton = new JButton();
	runButton.setToolTipText("Run the simulation");
	runButton.setIcon(new ImageIcon("resources/icons/run.png"));
	runButton.addActionListener((e) -> startSimulation());
	toolaBar.add(runButton);
	
	//5.BOTON STOP
	stopButton = new JButton();
	stopButton.setToolTipText("Stop the simulation");
	stopButton.setIcon(new ImageIcon("resources/icons/stop.png"));
	stopButton.addActionListener((e) -> stopped = true);
	toolaBar.add(stopButton);
	stopButton.setEnabled(false);
	
	//6. JSPINNER DE PASOS
	toolaBar.addSeparator();
	toolaBar.add(new JLabel("Steps: "));
	
	stepsSpinner = new JSpinner(new SpinnerNumberModel(10000, 1, 100000, 100));
	stepsSpinner.setMaximumSize(new Dimension(80, 40));
	toolaBar.add(stepsSpinner);
	
	//7. JTEXTFIELD DE DELTA TIME
	toolaBar.addSeparator();
	toolaBar.add(new JLabel("Delta-Time: "));
	
	String initialDt = (Main.deltaTime != null) ? Main.deltaTime.toString() : "0.03";
	dtTextField = new JTextField(initialDt);
	dtTextField.setMaximumSize(new Dimension(80, 40));
	toolaBar.add(dtTextField);
	
	//8. BOTON QUIT
	toolaBar.add(Box.createGlue()); 
	toolaBar.addSeparator();
	quitButton = new JButton();
	quitButton.setToolTipText("Quit");
	quitButton.setIcon(new ImageIcon("resources/icons/exit.png"));
	quitButton.addActionListener((e) -> ViewUtils.quit(this));
	toolaBar.add(quitButton);

   
  }   
  // TODO el resto de m�todos van aqu� 
  private void loadFile() {
		int ret = fc.showOpenDialog(ViewUtils.getWindow(this));
		if (ret == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try (InputStream is = new FileInputStream(file)) {
				JSONObject jsonInput = new JSONObject(new JSONTokener(is));
				
				// Extraemos las dimensiones para resetear el simulador
				int width = jsonInput.getInt("width");
				int height = jsonInput.getInt("height");
				int rows = jsonInput.getInt("rows");
				int cols = jsonInput.getInt("cols");
				
				ctrl.reset(cols, rows, width, height);
				ctrl.loadData(jsonInput);
				
			} catch (Exception e) {
				ViewUtils.showErrorMsg("Error loading file: " + e.getMessage());
			}
		}
	}

	private void openViewer() {
		try {
			new MapWindow(ViewUtils.getWindow(this), ctrl);
		} catch (Exception e) {
			ViewUtils.showErrorMsg("Error opening Viewer: " + e.getMessage());
		}
	}

	private void startSimulation() {
		try {
			// Desactivar botones y encender flag
			setButtonsEnabled(false);
			stopped = false;
			
			// Leer valores
			double dt = Double.parseDouble(dtTextField.getText());
			int steps = (Integer) stepsSpinner.getValue();
			
			runSim(steps, dt);
			
		} catch (NumberFormatException e) {
			ViewUtils.showErrorMsg("Invalid format for Delta-Time.");
			setButtonsEnabled(true);
			stopped = true;
		} catch (Exception e) {
			ViewUtils.showErrorMsg("Error starting simulation: " + e.getMessage());
			setButtonsEnabled(true);
			stopped = true;
		}
	}

	private void setButtonsEnabled(boolean enabled) {
		openButton.setEnabled(enabled);
		viewerButton.setEnabled(enabled);
		regionsButton.setEnabled(enabled);
		runButton.setEnabled(enabled);
		quitButton.setEnabled(enabled);
		stepsSpinner.setEnabled(enabled);
		dtTextField.setEnabled(enabled);
		
		// El bot�n de stop hace lo contrario: si los dem�s est�n desactivados, �l se activa
		stopButton.setEnabled(!enabled); 
	}

	private void runSim(int n, double dt) {
		if (n > 0 && !this.stopped) {
			try {
				this.ctrl.advance(dt);
				SwingUtilities.invokeLater(() -> runSim(n - 1, dt));
			} catch (Exception e) {
				// Mostrar error, restaurar botones y estado
				ViewUtils.showErrorMsg("Simulation error: " + e.getMessage());
				setButtonsEnabled(true);
				this.stopped = true;
			}
		} else {
			// Finaliz� la simulaci�n o el usuario le dio a "stop"
			setButtonsEnabled(true);
			this.stopped = true;
		}
	}
}