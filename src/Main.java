/*
 * Mouradov APCS Period 4 Final Project 2020-2021
 * Copyright Benjamin Greene 04/16/2021
 * 
 * Libraries Used:
 * mXparser - https://mathparser.org/mxparser-api/
 * RangeSlider - https://github.com/ernieyu/Swing-range-slider
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class Main extends JPanel {
	private static final long serialVersionUID = 1L;
	static Main titleScreen;
	static Image titleImage = null;
	static Image functionImage = null;
	JButton functionButton;
	static Image graphImage = null;
	JButton graphButton = new JButton("");
	static Image drawingImage = null;
	JButton drawingButton = new JButton("");
	static JFrame frame;
	static JPanel graph;
	ArrayList<Double> poly = new ArrayList<Double>();
	Timer timer;
	double time = 0;

	public Main() {
		titleScreen = this;
		this.setLayout(null);
		this.setBackground(Color.BLACK);
		try {
			titleImage = ImageIO.read(ClassLoader.getSystemResource("title image.png"));
			functionImage = ImageIO.read(ClassLoader.getSystemResource("function button image.png"));
			functionButton = new JButton(new ImageIcon(functionImage));
			functionButton.setBounds(100, 725, 500, 250);
			functionButton.setBorder(BorderFactory.createEmptyBorder());
			functionButton.setFocusable(false);
			functionButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					// TODO Auto-generated method stub
					titleScreen.setVisible(false);
					graph = new GraphPanel(GraphPanel.FUNCTION_MODE, new GraphFunction("f(x) = 2*x"));
					frame.add(graph);
				}

			});
			this.add(functionButton);

			graphImage = ImageIO.read(ClassLoader.getSystemResource("graph button image.png"));
			graphButton = new JButton(new ImageIcon(graphImage));
			graphButton.setBounds(710, 725, 500, 250);
			graphButton.setBorder(BorderFactory.createEmptyBorder());
			graphButton.setFocusable(false);
			graphButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					// TODO Auto-generated method stub
					titleScreen.setVisible(false);
					graph = new GraphPanel(GraphPanel.GRAPHING_MODE, new GraphFunction("f(x) = 2*x"));
					frame.add(graph);
				}

			});
			this.add(graphButton);

			drawingImage = ImageIO.read(ClassLoader.getSystemResource("drawing button image.png"));
			drawingButton = new JButton(new ImageIcon(drawingImage));
			drawingButton.setBounds(1320, 725, 500, 250);
			drawingButton.setBorder(BorderFactory.createEmptyBorder());
			drawingButton.setFocusable(false);
			drawingButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					// TODO Auto-generated method stub
					titleScreen.setVisible(false);
					graph = new GraphPanel(GraphPanel.DRAWING_MODE, new GraphFunction("f(x) = 2*x"));
					frame.add(graph);
				}

			});
			this.add(drawingButton);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		timer = new Timer(20, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if (titleScreen.isVisible()) {
					time += .05;
					repaint();
				}
			}
			
		});
		timer.start();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(titleImage, (getWidth() - titleImage.getWidth(null)) / 2, 80, titleImage.getWidth(null),
				titleImage.getHeight(null), null);
		
		Graphics2D g2 = (Graphics2D)g;
		
		int numCircles = 20;
		double[] radius = new double[numCircles];
		double[] frequency = new double[numCircles];
		double[] phase = new double[numCircles];
		
		int timeperiod = 200;
		if (time%timeperiod <= 50) {
			for (int i = 0; i < numCircles; i++) {
				radius[i] = 100 * 4/(Math.PI*(2*i+1));
				frequency[i] = 2*i + 1;
				phase[i] = 0;
			}
		} else if (time%timeperiod <= 100) {
			for (int i = 0; i < numCircles; i++) {
				radius[i] =  100* 2 * Math.pow(-1, i+2)/(Math.PI*(i+1));
				frequency[i] = i+1;
				phase[i] = 0;
			}
		} else if (time%timeperiod <= 150) {
			for (int i = 0; i < numCircles; i++) {
				radius[i] = 100 * 8/(Math.PI*Math.PI*(2*i+1)*(2*i+1));
				frequency[i] = 2*i + 1;
				phase[i] = -1 * Math.PI/2;
			}
		} else if (time%timeperiod <= 200) {
			radius[0] = 1; frequency[0] = 0; phase[0] = 0.88068923542; // weird thingy because this one's fourier series is wacky
			for (int i = 1; i < numCircles; i++) {
				radius[i] = 100 * 4/(Math.PI*(4*i*i-1));
				frequency[i] = 2*i;
				phase[i] = -1 * Math.PI/2;
			}
		}

		int x = 220; int y = this.getHeight()/2-75;

		
		for (int i = 0; i < numCircles; i++) { // max circles need to be limited for lag sake
			
			int oldX = x;
			int oldY = y;

			x += radius[i] * Math.cos(frequency[i] * time + phase[i]);
			y += radius[i] * Math.sin(frequency[i] * time + phase[i]);
			g.setColor(Color.GRAY);
			g.drawOval((int) (oldX - radius[i]), (int) (oldY - radius[i]), (int) (2 * radius[i]), (int) (2 * radius[i]));
			g.setColor(Color.WHITE);
			g2.drawLine(oldX, oldY, x, y);
		}
		g.fillOval(x - 5, y - 5, 10, 10);
		g.drawLine(x, y, 440, y);
		poly.add(0, (Double) (double) (y - this.getHeight() / 2));
		
		for (int i = 1; i < poly.size(); i++) {
			if (Math.abs(poly.get(i) - poly.get(i - 1)) < Math.min((this.getHeight() + this.getWidth()) / 4,
					this.getHeight() / 2))
				g.drawLine(i - 1 + 440, (int) (double) poly.get(i - 1) + this.getHeight() / 2, i + 440,
						(int) (double) poly.get(i) + this.getHeight() / 2);
			if (i > this.getMaximumSize().getWidth())
				poly.remove(i);
		}
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		/**
		* IMPORTANT: IF THE PROGRAM IS CRASHING BUT THERE ARE NO ERRORS, IT IS BECUASE I DISABLED THE ERROR STREAM.
		* Reasoning: sometimes when you set the period to 0 for the function display, it throws an indexoutofbounds exception
		* but doesn't actually mess anything up, so I wanted to make it look neat :)
		*/
		
		System.err.close(); // uhh no errors here
		try {
			UIManager.installLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel",
					"com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel"); // I like the look of linux
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
		}
		frame = new JFrame("Fourier Things");
		frame.add(new Main());
		frame.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == 27) {
					graph.setVisible(false);
					titleScreen.setVisible(true);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
			}

		});
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setResizable(false);
		frame.setVisible(true);

	}
}

/*
 * POSSIBLE IMPLEMENTATION FOR GRAPHING IMPLICIT FUNCTIONS: given String input =
 * "x^2+y^2=12" // works for any implicit function define GraphFunction f = new
 * GraphFunction("f(x,y)=" + input.substring(0, input.indexOf("=")) +
 * "+ 0*x + 0*y"); define GraphFunction g = new GraphFunction("g(x,y)=" +
 * input.substring(input.indexOf("=")+1) + "+ 0*x + 0*y"); scan through each
 * pixel on screen -> check if f(xcoord, ycoord) = g(xcoord, ycoord) if yes:
 * paint that pixel if no: dont paint that pixel
 */