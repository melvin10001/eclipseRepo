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
	
	String theMessage;
	int messageX = 125, messageY = 95;
	
	JButton theButton;
	
	int colorIndex;
	Color[] someColors = {
			Color.black, Color.blue, Color.red, Color.green, Color.magenta
	};
	
	boolean blinkState;
	
	public HelloComponent4 (String message) {
		theMessage = message;
		theButton = new JButton("Change Color");
		setLayout(new FlowLayout());
		add(theButton);
		theButton.addActionListener(this);
		addMouseMotionListener(this);
		Thread t = new Thread(this);
		t.start();
	}
	public void paintComponent(Graphics g) {
		g.setColor(blinkState ? getBackground() : currentColor());
		g.drawString(theMessage, messageX, messageY);
		
	}
}
