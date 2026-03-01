package simulator.model;

public interface MapInfo extends JSONable {
	public int getCols();  
	public int getRows();  
	public int getWidth();  
	public int getHeight();  
	public int getRegionWidth();  
	public int getRegionHeight(); 
}
