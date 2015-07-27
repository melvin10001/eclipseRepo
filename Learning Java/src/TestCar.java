
public class TestCar {

	public static void main(String[] args) {
		
		Car efficientCar = new EfficientCar();
		Car acceptableCar = new AcceptableCar();
		Car gasGuzzler = new GasGuzzler();
		
		Car[] cars = {efficientCar, acceptableCar, gasGuzzler};
		
		for(int i = 0 ; i < 3; i ++ ) {
			System.out.println("Car name: " + cars[i].getName());
			System.out.println("Min usage for 100 miles is : " + cars[i].minUsage(100));
			System.out.println("Max usage for 100 miles is : " + cars[i].maxUsage(100) + "\n");
		}

	}

}
