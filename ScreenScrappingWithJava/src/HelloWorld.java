import javax.swing.JFrame;
import javax.swing.JLabel;

public class HelloWorld {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame frame = new JFrame("Hello Java");
		JLabel label = new JLabel("Hello JAVA!", JLabel.CENTER);
		frame.add(new HelloComponent());
		frame.setSize(300,300);
		frame.setVisible(true);
		
	}

}
