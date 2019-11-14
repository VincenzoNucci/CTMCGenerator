package homework1;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.math3.*;
import org.apache.commons.math3.geometry.euclidean.oned.Vector1D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealLinearOperator;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SymmLQ;
import org.apache.commons.math3.linear.ArrayRealVector;

import app.CTMCGenerator;
import app.CTMCGenerator.State;

public class ShopScenario {
	
	public static int C = 0;
	public static int U = 1;
	public static int US = 2;
	//public static int TS = 2;
	//public static int TL = 3;
	
	//TIME UNIT IS MINUTES
	public static int MAX_USERS = 5;
	public static int MAX_CLERKS = 1;
	public static double M_A = 1.0; //ARRIVAL RATE
	public static double SEL_RATE = 1.0/(30.0/60); //30s to select a customer to be served
	public static double M_S = 1.0; //SERVICE TIME
	//public static double P_SHORT = 0.5;
	
	public static void main(String[] args) {
		CTMCGenerator ctmc = new CTMCGenerator(new State(MAX_CLERKS,0,0), ShopScenario::next);
		System.out.println("SIZE: " + ctmc.generate());
		/*
		for(State s : ctmc.getStates().keySet()) {
			System.out.println(s);
		}*/
		double[][] q = ctmc.exportMatrixQ();
		/*for(int i = 0; i < q.length; i++) {
			System.out.println("");
			for(int j = 0; j < q.length; j++) {
				System.out.print(q[i][j] + " ");
			}
		}*/
		//System.err.println(ctmc.statesToString());
		
		//Compute steady state probability...
		//...transpose Q
		RealMatrix qT = MatrixUtils.createRealMatrix(q).transpose();
		//...and substitute last row with all 1's
		double[] vect = {1,1,1,1,1,1,1,1,1,1,1};
		double[] se = {0,0,0,0,0,0,0,0,0,0,1};
		RealVector v = new ArrayRealVector(vect);
		RealVector eN = new ArrayRealVector(se);
		qT.setRowVector(qT.getRowDimension()-1, v);
		DecompositionSolver solver = new LUDecomposition(qT).getSolver();
		RealVector pi = solver.solve(eN);
		System.out.println(pi);
		double sum = 0;
		for(int i = 0; i < pi.getDimension(); i++) {
			sum += pi.getEntry(i);
		}
		System.out.println("CumProb: " + sum);
	}
	
	public static HashMap<State,Double> next(State s) {
		HashMap<State,Double> toReturn = new HashMap<CTMCGenerator.State, Double>();
		int[] values = s.getState();
		
		//A new customer arrives at the shop
		if(values[U] + values[US] < MAX_USERS) {
			int[] newState = Arrays.copyOf(values, values.length);
			newState[U] = newState[U] + 1;
			toReturn.put(new State(newState), M_A);
		}
		
		//A clerk selects a customer (BETA RATE)
		if((values[U] > 0) && (values[C] > 0)) {
			int[] newState = Arrays.copyOf(values, values.length);
			newState[U] = newState[U] - 1; //customer could be served twice so it's removed now
											//and put in the "client being served counter"
			newState[US] = newState[US] + 1;
			newState[C] = newState[C] - 1; //the clerk is now busy
			toReturn.put(new State(newState), SEL_RATE);
		}
		
		//A clerk served a customer (and the customer exits)
		if(values[US] > 0) {
			int[] newState = Arrays.copyOf(values, values.length);
			newState[US] = newState[US] - 1;
			newState[C] = newState[C] + 1;
			toReturn.put(new State(newState), M_S);
		}

		return toReturn;
	}
	
}
