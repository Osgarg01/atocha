package simulator.view;
import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

public class InfoTable extends JPanel {

	private String title;
	private TableModel tableModel;

	InfoTable(String title, TableModel tableModel) {
		this.title = title;
		this.tableModel = tableModel;
		initGUI();
	}

	private void initGUI() {
		// Cambiar el layout del panel a BorderLayout()
		this.setLayout(new BorderLayout());

		// Añadir un borde con título al JPanel con el texto this.title
		this.setBorder(BorderFactory.createTitledBorder(this.title));

		// Añadir un JTable que use this.tableModel
		JTable table = new JTable(this.tableModel);

		// Envolver la tabla en un JScrollPane para la barra de desplazamiento vertical
		JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// Añadir el JScrollPane  al panel
		this.add(scrollPane, BorderLayout.CENTER);
	}
}