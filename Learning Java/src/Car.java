


public abstract class Car {
	String name;
	FuelEfficiency fuelEfficiency;
	
	public int minUsage(int mileDist){
		
		return mileDist * fuelEfficiency.getMin();
	}
	
	public int maxUsage(int mileDist){
		return mileDist * fuelEfficiency.getMax();
	}
	
	public String getName(){
		return this.name;
	}

}
