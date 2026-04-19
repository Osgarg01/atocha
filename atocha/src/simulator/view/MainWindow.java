package simulator.view;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import simulator.control.Controller;

public class MainWindow extends JFrame {
	private Controller ctrl;

	  public MainWindow(Controller ctrl) {  
	    super("[ECOSYSTEM SIMULATOR]");  
	    this.ctrl = ctrl;  
	    initGUI();  
	  }

	  private void initGUI() {  
	    JPanel mainPanel = new JPanel(new BorderLayout());  
	    setContentPane(mainPanel);

	    //crear ControlPanel y a�adirlo en PAGE_START de mainPanel
	    
	    ControlPanel ctrlPanel = new ControlPanel(ctrl);
		mainPanel.add(ctrlPanel, BorderLayout.PAGE_START);

	    // crear StatusBar y a�adirlo en PAGE_END de mainPanel  
		
		StatusBar statusBar = new StatusBar(ctrl);
		mainPanel.add(statusBar, BorderLayout.PAGE_END);

	    // Definici�n del panel de tablas (usa un BoxLayout vertical)  
	    JPanel contentPanel = new JPanel();  
	    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));  
	    mainPanel.add(contentPanel, BorderLayout.CENTER);

	    // crear la tabla de especies y a�adirla a contentPanel.  
	    
	    InfoTable speciesTable = new InfoTable("Species", new SpeciesTableModel(ctrl));
		speciesTable.setPreferredSize(new Dimension(500, 250));
		contentPanel.add(speciesTable);

	    //  crear la tabla de regiones.  
	   
		
		InfoTable regionsTable = new InfoTable("Regions", new RegionsTableModel(ctrl));
		regionsTable.setPreferredSize(new Dimension(500, 250));
		contentPanel.add(regionsTable);

	    // llama a ViewUtils.quit(MainWindow.this) en el m�todo windowClosing
	   
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				ViewUtils.quit(MainWindow.this);
			}
		});

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		pack();
		setVisible(true);
	   }  
}
