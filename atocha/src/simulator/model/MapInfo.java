package simulator.model;

public interface MapInfo extends JSONable, Iterable<MapInfo.RegionData> {
	 public record RegionData(int row, int col, RegionInfo r) {  
	  } 
	 
	 
	public int getCols();  
	public int getRows();  
	public int getWidth();  
	public int getHeight();  
	public int getRegionWidth();  
	public int getRegionHeight(); 
}
