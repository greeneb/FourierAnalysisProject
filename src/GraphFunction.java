import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;

import org.mariuszgromada.math.mxparser.Function;
import org.mariuszgromada.math.mxparser.FunctionExtension;
import org.mariuszgromada.math.mxparser.FunctionExtensionVariadic;
import org.mariuszgromada.math.mxparser.PrimitiveElement;

public class GraphFunction extends Function {
	private double time = 0.;
	private ArrayList<Double> fourierPoly = new ArrayList<Double>();
	private ArrayList<Point> drawingPoly = new ArrayList<Point>();
	private Polygon poly = new Polygon();
	private double[][] fourierSeries;
	private double periodMin = -10;
	private double periodMax = 10;

	// CONSTRUCTORS
	public GraphFunction(String arg0, PrimitiveElement... arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}
	public GraphFunction(String arg0, FunctionExtension arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}
	public GraphFunction(String arg0, FunctionExtensionVariadic arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}
	public GraphFunction(String arg0, String arg1, PrimitiveElement... arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}
	public GraphFunction(String arg0, String arg1, String... arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	// GETTERS
	public Color chooseColor() {
		Color[] colors = { Color.RED, Color.BLUE, Color.ORANGE, Color.MAGENTA, Color.RED };
		return colors[getFunctionName().concat(getFunctionExpressionString()).length() % colors.length];
	}
	public ArrayList<Double> getFourierPoly() {
		return fourierPoly;
	}
	public ArrayList<Point> getDrawingPoly() {
		return drawingPoly;
	}
	public Polygon getPoly() {
		return poly;
	}
	public double getTime() {
		return time;
	}
	public double getPeriodMin() {
		return periodMin;
	}
	public double getPeriodMax() {
		return periodMax;
	}
	public double getPeriod() {
		return Math.abs(periodMax - periodMin);
	}
	public double[][] getFourierSeries() {
		updateFourierSeries();
		return fourierSeries;
	}

	// SETTERS
	public GraphFunction changeFunction(String functionDefinitionString) {
		GraphFunction temp = new GraphFunction(functionDefinitionString);
		temp.setTime(this.time);
		temp.setFourierPoly(this.fourierPoly);
		temp.setPoly(this.poly);
		return temp;
	}
	public void setFourierPoly(ArrayList<Double> fourierPoly) {
		this.fourierPoly = fourierPoly;
	}
	public void setDrawingPoly(ArrayList<Point> drawingPoly) {
		this.drawingPoly = drawingPoly;
	}
	public void setPoly(Polygon poly) {
		this.poly = poly;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public void increaseTime(double increase) {
		this.time += increase;
	}
	public void setPeriodMin(double periodMin) {
		this.periodMin = periodMin;
	}
	public void setPeriodMax(double periodMax) {
		this.periodMax = periodMax;
	}
	
	// FOURIER
	public void updateFourierSeries() {
		fourierSeries = functionSeries();
	}
	public static double[][] transformToSeries(Complex[] Transform) {
		double[][] X = new double[Transform.length][5];
		for (int k = 0; k < X.length; k++) {
			// 0,  1,  2,    3,   4
			// re, im, freq, amp, phase
			double re = Transform[k].re;
			double im = Transform[k].im;
			X[k] = new double[] { re, im, k, Math.sqrt(re * re + im * im), Math.atan2(im, re) };
		}
		return X;
	}	
	private double[] getTransformInput() {
		double[] x = new double[10*(int)getPeriod()];
		//double[] x = new double[100];
		for (int i = 0; i < x.length; i++) {
			x[i] = calculate(i+10*getPeriodMin());
		}
		//System.out.println(Arrays.toString(x));
		return x;
	}
	
	// drawing
	public double[][] getDrawingSeries(int originX, int originY) {
		ArrayList<Point> points = new ArrayList<Point>();
		for (int i = 0; i < this.getPoly().npoints; i+=4)
			points.add(new Point(this.getPoly().xpoints[i]-originX, this.getPoly().ypoints[i]-originY));
		double[][] input = new double[points.size()][2];
		for (int i = 0; i < input.length; i++) {
			input[i][0] = points.get(i).x;
			input[i][1] = points.get(i).y;
		}
		return functionSeries(input);
	}
	
	// will chose FFT or DFT depending on the size of the array
	public double[][] functionSeries() {
		return transformToSeries(transform(getTransformInput()));
	}
	public static double[][] functionSeries(double[] x) {
		return transformToSeries(transform(x));
	}
	public static double[][] functionSeries(double[][] x) {
		return transformToSeries(transform(x));
	}
	public static Complex[] transform(double[] x) {
		Complex[] complex = new Complex[x.length];
		for (int i = 0; i < complex.length; i++)
			complex[i] = new Complex(x[i], 0);
		return transform(complex);
	}
	public static Complex[] transform(double[][] x) {
		Complex[] complex = new Complex[x.length];
		for (int i = 0; i < complex.length; i++)
			complex[i] = new Complex(x[i][0], x[i][1]);
		return transform(complex);
	}
	public static Complex[] transform(Complex[] x) {
		if (Math.log(x.length)/Math.log(2)-Math.floor(Math.log(x.length)/Math.log(2)) == 0) // if x has a power of 2 length
			x = FFT(x);
		else
			x = DFT(x);
		
		for (int i = 0; i < x.length; i++)
			x[i].round(3);
		return x;
	}
	
	// DFT
	public static double[][] DFTfunctionSeries(Complex[] x) {
		return transformToSeries(DFT(x));
	}
	public static Complex[] DFT(Complex[] x) {
		Complex[] X = new Complex[x.length];
		int N = X.length;
		for (int k = 0; k < N; k++) {
			Complex sum = new Complex(0, 0);
			for (int n = 0; n < N; n++) {
				double theta = (2 * Math.PI * k * n) / N;
				Complex multiplier = new Complex(Math.cos(theta), -Math.sin(theta));
				sum.add(x[n].times(multiplier));
			}
			X[k] = new Complex(sum.re/N, sum.im/N);
			X[k].round(3);
		}
		return X;
	}

	// FFT
	public static double[][] FFTfunctionSeries(Complex[] x) {
		return transformToSeries(FFT(x));
	}
	public static double[][] FFTfunctionSeries(Double[][] x) {
		Complex[] complex = new Complex[x.length];
		for (int i = 0; i < complex.length; i++)
			complex[i] = new Complex(x[i][0], x[i][1]);
		return FFTfunctionSeries(complex);
	}
	/** look at me, fancy shmancy :)
	 * 
	 * @param x.length must be a power of 2
	 * @return the fast fourier transform (array of complex numbers) of the input array
	 */
	public static Complex[] FFT(Complex[] x) { // x[i][0] = re, x[i][1] = im
		int N = x.length;
		if (N == 1)
			return new Complex[] { x[0] };
			
		Complex[] even = new Complex[N/2];
		Complex[] odd = new Complex[N/2];
		
		for (int i = 0; i < N/2; i++) {
			try {
				even[i] = x[2*i];
				odd[i] = x[2*i+1];
			} catch (ArrayIndexOutOfBoundsException e) {
				even[i] = new Complex(0, 0);
				odd[i] = new Complex(0, 0);
			}
		}
		
		Complex[] evenTransformed = FFT(even);
		Complex[] oddTransformed = FFT(odd);
		
		Complex[] X = new Complex[N];
		for (int i = 0; i < N/2; i++) {
			double arg = -2 * i * Math.PI / N;
			Complex multiplier = new Complex(Math.cos(arg), Math.sin(arg));
			X[i] = evenTransformed[i].plus(multiplier.times(oddTransformed[i]));
			X[i + N/2] = evenTransformed[i].minus(multiplier.times(oddTransformed[i]));
		}
		return X;
	}
	
}

class Complex {
	double re; double im;
	
	public Complex(double re, double im) {
		this.re = re;
		this.im = im;
	}
	
	public void add(Complex other) {
		this.re += other.re;
		this.im += other.im;
	}
	
	public Complex plus(Complex other) {
		return new Complex(this.re + other.re,
		this.im + other.im);
	}
	
	public Complex minus(Complex other) {
		return new Complex(this.re - other.re,
		this.im - other.im);
	}
	
	public Complex times(Complex other) {
		double re = this.re * other.re - this.im * other.im;
		double im = this.re * other.im + this.im * other.re;
		return new Complex(re, im);
	}
	
	public void round(int places) {
		this.re = Math.round(this.re*Math.pow(10, places))/Math.pow(10, places);
		this.im = Math.round(this.im*Math.pow(10, places))/Math.pow(10, places);
	}
	
	public String toString() {
		return re + " + " + im + "i";
	}
}
