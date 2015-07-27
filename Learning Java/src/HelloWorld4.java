import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class HelloWorld4 {
	public static void main(String[] args){
		JFrame frame = new JFrame("Hello world");
		frame.add(new HelloComponent4("Hello JAVA!"));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(300, 300);
		frame.setVisible(true);
	}
}

class HelloComponent4 extends JComponent 
	implements MouseMotionListener, ActionListener, Runnable {
	
}
