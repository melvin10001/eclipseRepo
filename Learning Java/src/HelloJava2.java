import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class HelloJava2 {
	public static void main(String[] args ){
		
		JFrame frame = new JFrame("Hello Java!");
		frame.add(new HelloComponent2(args[0]));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(300, 300);
		frame.setVisible(true);
		
	}
}
