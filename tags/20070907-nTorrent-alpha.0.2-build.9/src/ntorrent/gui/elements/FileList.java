package ntorrent.gui.elements;

import java.awt.Color;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import ntorrent.model.FileTableModel;

public class FileList {
	//Simple filelist.
	JTable filetable = new JTable(new FileTableModel());
	JScrollPane fileList;
	
	FileList(){
		filetable.setOpaque(false);
		filetable.setAutoCreateRowSorter(true);
		filetable.setBackground(Color.white);
		filetable.setFillsViewportHeight(true);
		
		/*TableColumn column = null;
		for (int i = 0; i < filetable.getColumnCount(); i++) {
			column = filetable.getColumnModel().getColumn(i);
			switch (i){
				case 0:
					column.setPreferredWidth(10);
				case 2:
					column.setPreferredWidth(100);
				
			}
		}*/
		
		fileList  = new JScrollPane(filetable);
	}
	
	public JScrollPane getFileList() {
		return fileList;
	}

	public void hideInfo() {
		fileList.setVisible(false);
	}

	public void setInfo(Vector<Object>[] list) {
		FileTableModel table = ((FileTableModel)filetable.getModel());
		table.clear();
		for(int y = 0; y < list.length; y++)
			for(int x = 0; x < 3; x++)
				table.setValueAt(list[y].get(x), y, x);
	}
}