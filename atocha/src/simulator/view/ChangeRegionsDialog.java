package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.control.Controller;
import simulator.launcher.Main;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

public class ChangeRegionsDialog extends JDialog implements EcoSysObserver{
	
	 private DefaultComboBoxModel<String> regionsModel;  
	  private DefaultComboBoxModel<String> fromRowModel;  
	  private DefaultComboBoxModel<String> toRowModel;  
	  private DefaultComboBoxModel<String> fromColModel;  
	  private DefaultComboBoxModel<String> toColModel;

	  private DefaultTableModel dataTableModel;  
	  private Controller ctrl;  
	  private List<JSONObject> regionsInfo;

	  private String[] headers = { "Key", "Value", "Description" };
	  private int status = 0;
	  
	  ChangeRegionsDialog(Controller ctrl) {  
		    super((Frame)null, true);  
		    this.ctrl = ctrl;  
		    initGUI();  
		    //registrar this como observer; 
		    this.ctrl.addObserver(this);
		  }
	  
	  private void initGUI() {  
		    setTitle("Change Regions");  
		    JPanel mainPanel = new JPanel();  
		    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));  
		    setContentPane(mainPanel);

		    //  crea varios paneles para organizar los componentes visuales en el  
		    //      dialogo, y a�adelos al mainpanel. P.ej., uno para el texto de ayuda,
		    //      uno para la tabla, uno para los combobox, y uno para los botones.
		    JPanel helpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			helpPanel.add(new JLabel("<html><p>Select a region type, the rows/cols range, and provide values for the "
					+ "parameters in the <b>Value</b> column (default values are used for empty fields).</p></html>"));
			mainPanel.add(helpPanel);

		    //  crear el texto de ayuda que aparece en la parte superior del di�logo y  
		    //      a�adirlo al panel correspondiente di�logo (Ver el apartado Figuras)

		    // this.regionsInfo se usar� para establecer la informaci�n en la tabla  
		    this.regionsInfo = Main.regionsFactory.getInfo();

		    // this.dataTableModel es un modelo de tabla que incluye todos los par�metros de  
		    // la region
		    this.dataTableModel = new DefaultTableModel() {  
		      @Override  
		      public boolean isCellEditable(int row, int column) {  
		    	  return column == 1;
		      }  
		    };  
		    this.dataTableModel.setColumnIdentifiers(this.headers);

			JTable table = new JTable(this.dataTableModel);
			JScrollPane tableScroll = new JScrollPane(table);
			tableScroll.setPreferredSize(new Dimension(600, 150));
			mainPanel.add(tableScroll);

			JPanel combosPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			
			 this.regionsModel = new DefaultComboBoxModel<>();
			
			for (JSONObject info : regionsInfo) {
				regionsModel.addElement(info.getString("desc"));
			}

			JComboBox<String> regionsCombo = new JComboBox<>(regionsModel);
			// Listener para actualizar la tabla cuando se selecciona una regi�n
			regionsCombo.addActionListener((e) -> updateTableModel(regionsCombo.getSelectedIndex()));
			
			combosPanel.add(new JLabel("Region type: "));
			combosPanel.add(regionsCombo);
			
			this.fromRowModel = new DefaultComboBoxModel<>();
			this.toRowModel = new DefaultComboBoxModel<>();
			this.fromColModel = new DefaultComboBoxModel<>();
			this.toColModel = new DefaultComboBoxModel<>();

			combosPanel.add(new JLabel("Row from/to: "));
			combosPanel.add(new JComboBox<>(fromRowModel));
			combosPanel.add(new JComboBox<>(toRowModel));
			
			combosPanel.add(new JLabel("Col from/to: "));
			combosPanel.add(new JComboBox<>(fromColModel));
			combosPanel.add(new JComboBox<>(toColModel));

			mainPanel.add(combosPanel);

			// Inicializar la tabla con la primera regi�n por defecto
			if (regionsModel.getSize() > 0) {
				updateTableModel(0);
			}

			
			JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			
			JButton okButton = new JButton("OK");
			okButton.addActionListener((e) -> handleOKButton(regionsCombo.getSelectedIndex()));
			
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener((e) -> {
				status = 0;
				setVisible(false);
			});

			buttonsPanel.add(okButton);
			buttonsPanel.add(cancelButton);
			mainPanel.add(buttonsPanel);

			setPreferredSize(new Dimension(700, 400));
			pack();
			setResizable(false);
			setVisible(false); 
		  }
	  
	  
	  public void open(Frame parent) {
		    setLocation(
		      parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2,
		      parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
		    pack();  
		    setVisible(true);  
		  }

	private void handleOKButton(int selectedIndex) {
		try {
			//  Construir el JSON region_data din�micamente desde la tabla
			JSONObject regionData = new JSONObject();
			for (int i = 0; i < dataTableModel.getRowCount(); i++) {
				String key = dataTableModel.getValueAt(i, 0).toString();
				Object rawValue = dataTableModel.getValueAt(i, 1);
				String valueStr = rawValue == null ? "" : rawValue.toString();
				
				// Solo a�adimos al JSON si el usuario ha escrito algo
				if (!valueStr.trim().isEmpty()) {
					// Intentamos parsear a double, o lo guardamos como string si falla
					// (Asumimos que los par�metros din�micos suelen ser num�ricos)
					try {
						regionData.put(key, Double.parseDouble(valueStr));
					} catch (NumberFormatException ex) {
						regionData.put(key, valueStr);
					}
				}
			}

			//  Obtener region_type
			JSONObject info = regionsInfo.get(selectedIndex);
			String regionType = info.getString("type");

			//  Obtener coordenadas
			int rowFrom = Integer.parseInt((String) fromRowModel.getSelectedItem());
			int rowTo = Integer.parseInt((String) toRowModel.getSelectedItem());
			int colFrom = Integer.parseInt((String) fromColModel.getSelectedItem());
			int colTo = Integer.parseInt((String) toColModel.getSelectedItem());

			//  Construir el JSON final
			JSONObject specJSON = new JSONObject();
			specJSON.put("type", regionType);
			specJSON.put("data", regionData);

			JSONObject regionObj = new JSONObject();
			regionObj.put("row", new JSONArray().put(rowFrom).put(rowTo));
			regionObj.put("col", new JSONArray().put(colFrom).put(colTo));
			regionObj.put("spec", specJSON);

			JSONArray regionsArray = new JSONArray();
			regionsArray.put(regionObj);

			JSONObject finalJSON = new JSONObject();
			finalJSON.put("regions", regionsArray);

			//  Llamar al controlador
			ctrl.setRegions(finalJSON);
			
			// �xito
			status = 1;
			setVisible(false);

		} catch (Exception ex) {
			// Manejo de errores
			ViewUtils.showErrorMsg("Error changing regions: " + ex.getMessage());
		}
	}
	

	private void updateTableModel(int selectedIndex) {
		// Borrar filas anteriores
				dataTableModel.setRowCount(0);

				if (selectedIndex >= 0 && selectedIndex < regionsInfo.size()) {
					JSONObject info = regionsInfo.get(selectedIndex);
					JSONObject data = info.getJSONObject("data");

					// Iterar din�micamente por las claves (ej. "factor", "food"...)
					for (String key : data.keySet()) {
						String desc = data.getString(key);
						// A�adimos: Columna 0 (Clave), Columna 1 (Valor vac�o), Columna 2 (Desc)
						dataTableModel.addRow(new Object[] { key, "", desc });
					}
				}
				
	}
	
	private void updateCoordinatesModel(MapInfo map) {
		if (map == null) return;
		
		fromRowModel.removeAllElements();
		toRowModel.removeAllElements();
		fromColModel.removeAllElements();
		toColModel.removeAllElements();

		for (int i = 0; i < map.getRows(); i++) {
			fromRowModel.addElement(String.valueOf(i));
			toRowModel.addElement(String.valueOf(i));
		}
		
		for (int i = 0; i < map.getCols(); i++) {
			fromColModel.addElement(String.valueOf(i));
			toColModel.addElement(String.valueOf(i));
		}
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		updateCoordinatesModel(map);
		
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		updateCoordinatesModel(map);
		
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		
		
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		
		
	}

	@Override
	public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		
		
	}

}
