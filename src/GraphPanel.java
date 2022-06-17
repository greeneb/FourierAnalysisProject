import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import slider.RangeSlider;

public class GraphPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private GraphPanel panel = this;
	private GraphFunction function;
	private Timer timer;
	private double speed = 0.5;
	private int mode;
	private double yScale = 100;
	private double xScale = 100;
	private int originY = 0;
	private int originX = 0;
	private Point mousePoint = new Point(0, 0);
	private JPanel cp;
	private boolean fourierMode = false;
	private MouseMotionWatcher mouseMotionWatcher = new MouseMotionWatcher();
	private ScrollListener scrollListener = new ScrollListener();
	private MouseWatcher mouseWatcher = new MouseWatcher();
	public static final int FUNCTION_MODE = 0;
	public static final int DRAWING_MODE = 1;
	public static final int GRAPHING_MODE = 2;
	
	// CONSTRUCTORS
	public GraphPanel(int mode, GraphFunction f) {
		super();
		this.function = f;
		timer = new Timer(20, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (e.getSource() == timer && function != null) {
					if (mode == FUNCTION_MODE)
						function.increaseTime(2 * speed * Math.PI / 20);
					if (mode == DRAWING_MODE) {
						function.increaseTime(2 * Math.PI / function.getDrawingSeries(panel.getWidth()/2, panel.getHeight()/2).length);
					}
				}
				repaint();
			}
		});
		timer.start();
		cp = new ControlPanel(100, mode);
		this.mode = mode;
		this.setLayout(new BorderLayout());
		this.setBackground(Color.BLACK);
		this.addMouseWheelListener(scrollListener);
		this.addMouseMotionListener(mouseMotionWatcher);
		this.addMouseListener(mouseWatcher);
		this.add(cp, BorderLayout.SOUTH);
		this.setFocusable(true);
//		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // be evil
	}
	public GraphPanel(int mode, String functionDefinition) {
		this(mode, new GraphFunction(functionDefinition));
	}

	// GETTERS
	public GraphFunction getFunction() {
		return function;
	}
	public int getMode() {
		return this.mode;
	}

	private double getGraphX(double x) {
		return (x - getWidth() / 2 - originX) / xScale;
	}
	private double getGraphX() {
		return getGraphX(mousePoint.x);
	}
	private double getGraphY(double y) {
		return (getHeight() / 2 - y + originY) / yScale;
	}
	private double getGraphY() {
		return getGraphY(mousePoint.y);
	}

	// SETTERS
	public void setFunction(GraphFunction f) {
		function = f;
	}
	public void setFunction(String functionDefinition) {
		function = new GraphFunction(functionDefinition);
	}

	// PAINTCOMPONENT
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g.setColor(Color.WHITE);
		g2.setStroke(new BasicStroke(2));
		if (mode == DRAWING_MODE)
			paintDRAWING_MODE(g2);
		else if (mode == FUNCTION_MODE)
			paintFUNCTION_MODE(g2);
		else if (mode == GRAPHING_MODE)
			paintGRAPHING_MODE(g2);
	}
	private void paintFUNCTION_MODE(Graphics2D g) {
		cp.setVisible(true);
		if (function != null) {
			drawFourier(g, 220, this.getHeight() / 2, Math.PI/2); // gotta make it pi/2 so it works horizontally
		}
	}
	private void paintDRAWING_MODE(Graphics2D g) {
		cp.setVisible(false);
		if (fourierMode) {
			double[][] fourier = function.getDrawingSeries(this.getWidth()/2, this.getHeight()/2);
			Arrays.sort(fourier, new Comparator<double[]>() {

				@Override
				public int compare(double[] first, double[] second) {
					// TODO Auto-generated method stub
					if (first[3] < second[3])
						return 1;
					return -1;
				}
				
			});
			drawFourierDrawing(g, this.getWidth()/2, this.getHeight()/2, 0, fourier);
			drawFourierDrawing(g);
		} else {
			g.drawPolyline(function.getPoly().xpoints, function.getPoly().ypoints, function.getPoly().npoints);
		}
	}
	private void paintGRAPHING_MODE(Graphics2D g) {
		cp.setVisible(true);
		g.setColor(Color.GRAY);
		g.drawLine(0, this.getHeight() / 2 + originY, this.getWidth(), this.getHeight() / 2 + originY); // x-axis
		g.drawLine(this.getWidth() / 2 + originX, 0, this.getWidth() / 2 + originX, this.getHeight()); // y-axis
		g.setFont(new Font("Courier New", Font.PLAIN, 17));
		g.drawString("(" + Math.round(100 * getGraphX()) / 100. + ", " + Math.round(100 * getGraphY()) / 100. + ")",
				mousePoint.x, mousePoint.y);
		for (int i = 0; i < this.getWidth(); i++)
			if (getGraphX(i) - (int) getGraphX(i) == 0) {
				if (getGraphX(i) % 5 == 0) {
					g.drawLine(i, 0, i, getHeight());
					g.drawString("" + getGraphX(i), i + 3, getHeight() / 2 + originY + 20);
				} else
					g.drawLine(i, getHeight() / 2 + originY - 10, i, getHeight() / 2 + originY + 10);
			}
		for (int i = 0; i < this.getHeight(); i++)
			if (getGraphY(i) - (int) getGraphY(i) == 0 && getGraphY(i) != 0) { // second condition avoids labeling the
																				// origin twice
				if (getGraphY(i) % 5 == 0) {
					g.drawLine(0, i, getWidth(), i);
					g.drawString("" + getGraphY(i), getWidth() / 2 + originX + 3, i + 20);
				} else
					g.drawLine(getWidth() / 2 + originX - 10, i, getWidth() / 2 + originX + 10, i);
			}
		if (function != null)
			drawFunction(g, function);
	}

	// GRAPH FUNCTION
	private void drawFunction(Graphics2D g, GraphFunction f) {
		Polygon p = new Polygon();
		for (int i = -1 * this.getWidth() / 2; i < this.getWidth() / 2; i++)
			p.addPoint(this.getWidth() / 2 + i,
					(int) Math.ceil(this.getHeight() / 2 - yScale * f.calculate((i - originX) / xScale) + originY));
		g.setColor(f.chooseColor());
		drawFunction(g, p);
	}
	private void drawFunction(Graphics2D g, Polygon p) {
		g.setStroke(new BasicStroke(3));
		for (int i = 1; i < p.xpoints.length; i++)
			if (Point.distance(p.xpoints[i - 1], p.ypoints[i - 1], p.xpoints[i], p.ypoints[i]) < Math
					.min((this.getHeight() + this.getWidth()) / 4, this.getHeight() / 2) && p.ypoints[i] != 0)
				g.drawLine(p.xpoints[i - 1], p.ypoints[i - 1], p.xpoints[i], p.ypoints[i]);
	}

	// DRAW FOURIER
	private void drawFourier(Graphics2D g, int x, int y, double rotation) {
//		g.drawLine(440, (this.getHeight()-100) / 10, 440,
//				9 * (this.getHeight()-100) / 10); // y-axis
//		g.drawLine(440, this.getHeight() / 2, 2 * this.getWidth() / 2,
//				this.getHeight() / 2); // x-axis
		double[][] fourier = function.getFourierSeries();
		g.setStroke(new BasicStroke(1));
		for (int i = 0; i < 20; i++) { // max circles need to be limited for lag sake
			fourier = function.getFourierSeries();
			Arrays.sort(fourier, new Comparator<double[]>() {

				@Override
				public int compare(double[] first, double[] second) {
					// TODO Auto-generated method stub
					if (first[3] < second[3])
						return 1;
					return -1;
				}
				
			});
			int oldX = x;
			int oldY = y;
			double radius = xScale/100 * fourier[i][3];
			double frequency = fourier[i][2];
			double phase = fourier[i][4];
			x += radius * Math.cos(frequency * function.getTime() + phase + rotation);
			y += radius * Math.sin(frequency * function.getTime() + phase + rotation);
			g.setColor(Color.GRAY);
			drawCircle(g, oldX, oldY, radius);
			g.setColor(Color.WHITE);
			g.drawLine(oldX, oldY, x, y);
		}
		g.fillOval(x - 5, y - 5, 10, 10);
		g.drawLine(x, y, 440, y);
		function.getFourierPoly().add(0, (Double) (double) (y - this.getHeight() / 2));
		drawFourier(g, function.getFourierPoly(), 440);
	}
	private void drawFourier(Graphics2D g, ArrayList<Double> yPoints, int xOffset) {
		for (int i = 1; i < yPoints.size(); i++) {
			if (Math.abs(yPoints.get(i) - yPoints.get(i - 1)) < Math.min((this.getHeight() + this.getWidth()) / 4,
					this.getHeight() / 2))
				g.drawLine(i - 1 + xOffset, (int) (double) yPoints.get(i - 1) + this.getHeight() / 2, i + xOffset,
						(int) (double) yPoints.get(i) + this.getHeight() / 2);
			if (i > this.getMaximumSize().getWidth())
				yPoints.remove(i);
		}
	}
	private void drawFourierDrawing(Graphics2D g, int x, int y, double rotation, double[][] fourier) {
		g.setStroke(new BasicStroke(1));
		for (int i = 0; i < fourier.length; i++) { // max circles need to be limited for lag sake
			int oldX = x;
			int oldY = y;
			double radius = fourier[i][3];
			double frequency = fourier[i][2];
			double phase = fourier[i][4];
			//System.out.println("radius: " + radius + ", frequency: " + frequency + ", phase: " + phase);
			x += radius * Math.cos(frequency * function.getTime() + phase + rotation);
			y += radius * Math.sin(frequency * function.getTime() + phase + rotation);
			g.setStroke(new BasicStroke(0.5f));
			g.setColor(Color.GRAY);
			drawCircle(g, oldX, oldY, radius);
			g.setColor(Color.WHITE);
			g.drawLine(oldX, oldY, x, y);
		}
		g.fillOval(x - 5, y - 5, 10, 10);
		//g.drawLine(x, y, 440, y);
		function.getDrawingPoly().add(new Point(x, y));
		if (function.getDrawingPoly().size() > 50) 
			function.getDrawingPoly().remove(0);
	}
	
	private void drawFourierDrawing(Graphics2D g) {
		for (int i = 1; i < function.getDrawingPoly().size(); i++) {
			g.setColor(new Color(1f, 1f, 1f, 1f));//(float)(.3 + i*.7/function.getDrawingPoly().size())));
			g.drawLine(function.getDrawingPoly().get(i).x, function.getDrawingPoly().get(i).y, function.getDrawingPoly().get(i-1).x, function.getDrawingPoly().get(i-1).y);
		}
	}
	
	// DRAW CIRCLE
	private void drawCircle(Graphics2D g, int x, int y, double r) { // Draws a circle with radius r centered at (x,y)
		g.drawOval((int) (x - r), (int) (y - r), (int) (2 * r), (int) (2 * r));
	}

	// CONTROL PANEL
	public class ControlPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private int mode;
		private JSlider speedSlider = new JSlider();
		private RangeSlider periodSlider = new RangeSlider();
		private JButton resetZoom; private JTextArea functionBox;
		
		public ControlPanel(int height, int mode) {
			super();
			this.mode = mode;
			this.setPreferredSize(new Dimension(getWidth(), height));
			//this.setBackground();
			this.setLayout(null);
		}

		public void drawCP() {
			if (mode == FUNCTION_MODE) {
				if (this.getComponentCount() == 0) {
					functionBox = new JTextArea();
					functionBox.setLineWrap(false);
					functionBox.setForeground(Color.BLACK);
					functionBox.setText(function.getFunctionName() + "(" + function.getParameterName(0) + ") = " + function.getFunctionExpressionString());
					functionBox.setFont(new Font("Courier New", Font.PLAIN, 17));
					functionBox.setBounds(85, 10, 200, 25);
					functionBox.setPreferredSize(new Dimension(200, 25));
					functionBox.setBackground(Color.GRAY);
					functionBox.setCaretColor(Color.WHITE);
					functionBox.setBorder(null);
					functionBox.addKeyListener(new KeyListener() {

						@Override
						public void keyPressed(KeyEvent e) {
							// TODO Auto-generated method stub
							if (e.getKeyCode() == 27) {
								((JFrame)SwingUtilities.getRoot(panel)).requestFocusInWindow();
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
					add(functionBox);
					
					speedSlider.setOpaque(false);
					speedSlider.setMinimum(1);
					speedSlider.setMaximum(100);
					speedSlider.addChangeListener(new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							// TODO Auto-generated method stub
							speed = speedSlider.getValue()/100.;
						}	
					});
					speedSlider.setBounds(2 * getWidth() / 3 + 15, 55, getWidth()/3-100, 25);
					speedSlider.setBackground(Color.BLACK);
					speedSlider.setPaintTrack(true);
					speedSlider.setFocusable(false);
					add(speedSlider);
					
					periodSlider.setMinimum(-10);
					periodSlider.setMaximum(10);
					periodSlider.setValue((int)function.getPeriodMin());
					periodSlider.setUpperValue((int)function.getPeriodMax());
					periodSlider.setPaintLabels(true);
					periodSlider.setBounds(85, 55, 200, 25);
					periodSlider.setOpaque(false);
					periodSlider.addChangeListener(new ChangeListener() {

						@Override
						public void stateChanged(ChangeEvent arg0) {
							// TODO Auto-generated method stub
							//System.out.println(Arrays.deepToString(function.getFourierSeries()));
							function.setTime(0);
						}
						
					});
					periodSlider.setFocusable(false);
					add(periodSlider);
					
					resetZoom = new JButton("Reset Drawing");
					resetZoom.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							function.setTime(0);
							function.getFourierPoly().clear();
							speedSlider.setValue(50);
							speed = 0.5;
							xScale = 100;
							yScale = 100;
						}
					});
					resetZoom.setBackground(Color.BLACK);
					resetZoom.setForeground(Color.RED);
					resetZoom.setPreferredSize(new Dimension(150, 25));
					resetZoom.setBounds(2 * getWidth() / 3 + 20, 25, 150, 25);
					resetZoom.setFocusable(false);
					add(resetZoom);
					
				}
				if (functionBox.getText() != function.getFunctionName() + "(" + function.getParameterName(0) + ") = " + function.getFunctionExpressionString() && new GraphFunction(functionBox.getText()).checkSyntax()) {
					function = function.changeFunction(functionBox.getText());
				}
				function.setPeriodMin(periodSlider.getValue());
				function.setPeriodMax(periodSlider.getUpperValue());
				speedSlider.setBounds(2 * getWidth() / 3 + 15, 55, getWidth()/3-100, 40);
				resetZoom.setBounds(2 * getWidth() / 3 + 20, 25, 150, 25);
			} else if (mode == GRAPHING_MODE) {
				if (this.getComponentCount() == 0) {
					resetZoom = new JButton("Reset Zoom");
					resetZoom.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							xScale = 100;
							yScale = 100;
							originX = 0;
							originY = 0;
						}
					});
					resetZoom.setBackground(Color.BLACK);
					resetZoom.setForeground(Color.RED);
					resetZoom.setPreferredSize(new Dimension(150, 25));
					resetZoom.setBounds(2 * getWidth() / 3, 5, 150, 25);
					resetZoom.setFocusable(false);
					add(resetZoom);
					
					functionBox = new JTextArea();
					functionBox.setLineWrap(false);
					functionBox.setForeground(Color.BLACK);
					functionBox.setText(function.getFunctionName() + "(" + function.getParameterName(0) + ") = " + function.getFunctionExpressionString());
					functionBox.setFont(new Font("Courier New", Font.PLAIN, 17));
					functionBox.setBounds(85, 10, 200, 25);
					functionBox.setPreferredSize(new Dimension(200, 25));
					functionBox.setBackground(Color.GRAY);
					functionBox.setCaretColor(Color.WHITE);
					functionBox.setBorder(null);
					functionBox.addKeyListener(new KeyListener() {

						@Override
						public void keyPressed(KeyEvent e) {
							// TODO Auto-generated method stub
							if (e.getKeyCode() == 27) {
								((JFrame)SwingUtilities.getRoot(panel)).requestFocusInWindow();
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
						
					});					add(functionBox);
				}
				resetZoom.setBounds(2 * getWidth() / 3, 5, 150, 25);
				if (functionBox.getText() != function.getFunctionName() + "(" + function.getParameterName(0) + ") = " + function.getFunctionExpressionString() && new GraphFunction(functionBox.getText()).checkSyntax())
					function = new GraphFunction(functionBox.getText());
			}
		}

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			drawCP();
			
			if (mode == GraphPanel.DRAWING_MODE) {
				
			} else if (mode == GraphPanel.FUNCTION_MODE) {
				g.drawString("Function: ", 10, 30);
				g.drawString("Speed: ", getWidth() / 2, 82);
				Color temp = g.getColor();
				g.setColor(Color.GRAY);
				Rectangle bounds = speedSlider.getBounds();
				g.drawLine(bounds.x, bounds.y+2*bounds.height/3, bounds.x+bounds.width, bounds.y+2*bounds.height/3);
				g.setColor(temp);
				g.drawString("Period: ", 10, 70);
				g.drawString(periodSlider.getValue() + "", periodSlider.getX() + (periodSlider.getValue()-periodSlider.getMinimum())*periodSlider.getWidth()/(periodSlider.getMaximum()-periodSlider.getMinimum()), periodSlider.getY()-2);
				g.drawString(periodSlider.getUpperValue() + "", periodSlider.getX() + (periodSlider.getUpperValue()-periodSlider.getMinimum())*periodSlider.getWidth()/(periodSlider.getMaximum()-periodSlider.getMinimum()), periodSlider.getY()+38);

			} else if (mode == GraphPanel.GRAPHING_MODE) {
				g.drawString(
						"Center Offset: (" + Math.round(originX * 100. / xScale) / 100. + ", "
								+ Math.round(originY * 100. / yScale) / 100. + ")",
						2 * getWidth() / 3, this.getHeight() / 2);
				g.drawString(
						"Viewable Domain: [" + Math.round(100 * (-1 * this.getWidth() / 2 - originX) / xScale) / 100.
								+ ", " + Math.round(100 * (this.getWidth() / 2 - originX) / xScale) / 100. + "]",
						2 * getWidth() / 3, this.getHeight() / 2 + 20);
				g.drawString(
						"Viewable Range: ["
								+ Math.round(100 * getGraphY(this.getParent().getHeight() - this.getHeight())) / 100.
								+ ", " + Math.round(100 * getGraphY(0)) / 100. + "]",
						2 * getWidth() / 3, this.getHeight() / 2 + 40);
				g.drawString("function: ", 10, 30);
			}
		}
	}

	// LISTENERS
	class ScrollListener implements MouseWheelListener {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			// TODO Auto-generated method stub
			xScale -= e.getWheelRotation(); // negative means scrolling up/zooming out
			yScale -= e.getWheelRotation(); // positive means scrolling down/zooming in
			xScale = Math.max(1, xScale);
			yScale = Math.max(1, yScale);
			originY += e.getWheelRotation() * (e.getY() - (getHeight() / 2 + originY)) / 100;
			originX += e.getWheelRotation() * (e.getX() - (getWidth() / 2 + originX)) / 100;
			// repaint();
		}
	}
	class MouseMotionWatcher extends MouseMotionAdapter {

		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			originX += e.getX() - mousePoint.x;
			originY += e.getY() - mousePoint.y;
			mousePoint = e.getPoint();
			if (mode == GraphPanel.DRAWING_MODE) {
				function.getPoly().addPoint(mousePoint.x, mousePoint.y);
				function.setTime(0);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			mousePoint = e.getPoint();
		}
	}
	class MouseWatcher extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			if (mode == GraphPanel.DRAWING_MODE) {
				function.setPoly(new Polygon());
				function.setDrawingPoly(new ArrayList<Point>());
				fourierMode = false;
			}
			if (mode == GraphPanel.GRAPHING_MODE)
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			mousePoint = e.getPoint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (mode == GraphPanel.DRAWING_MODE) {
				function.setFourierPoly(new ArrayList<Double>());
				fourierMode = true;
			}
			setCursor(Cursor.getDefaultCursor());
		}
	}
}
