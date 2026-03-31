package simulator.model;

import java.util.List;

public interface RegionInfo extends JSONable {  
	// for now it is empty, later we will make it implement the interface  
	// Iterable<AnimalInfo>  
 
  public List<AnimalInfo> getAnimalsInfo();  
		
	
}