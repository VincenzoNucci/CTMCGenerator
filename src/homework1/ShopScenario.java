package homework1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
	public static int MAX_CLERKS = 5;
	public static double M_A = 1/10.0; //ARRIVAL RATE
	public static double SEL_RATE = 1.0/(10.0/60); //10s to select a customer to be served
	public static double M_S = 1/10.0; //SERVICE TIME
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
		List<Double> vect = new ArrayList<Double>();
		for(int i = 0; i < qT.getColumnDimension(); i++) {
			vect.add(1.0);
		}
		List<Double> se = new ArrayList<Double>();
		for(int i = 0; i < qT.getColumnDimension(); i++) {
			if(i == qT.getColumnDimension()-1)
				se.add(1.0);
			else
				se.add(0.0);
		}
		System.out.println(se);
		RealVector v = new ArrayRealVector(vect.toArray(new Double[0]));
		RealVector eN = new ArrayRealVector(se.toArray(new Double[0]));
		qT.setRowVector(qT.getRowDimension()-1, v);
		DecompositionSolver solver = new LUDecomposition(qT).getSolver();
		RealVector pi = solver.solve(eN);
		System.out.println("vector pi" + pi);
		double sum = 0;
		for(int i = 0; i < pi.getDimension(); i++) {
			sum += pi.getEntry(i);
		}
		//System.out.println("CumProb: " + sum);
		
		//Utilisation for clerks (fraction of time they are working)
		//Dobbiamo soltanto sommare le prob degli stati in cui almeno un clerk lavora
		//o anche moltiplicarle per le rispettive quantità di clerk che lavorano?
		double u = 0.0;
		for(State s : ctmc.getStates().keySet()) {
			if(s.getState()[C] < MAX_CLERKS) { //There is at least a working clerk in this state
				u += pi.getEntry(ctmc.getStates().get(s));
			}
		}
		System.out.println("Clerks utilisation is: " + u);
		
		//Average number of waiting customer
		double avg_wait = 0.0;
		for(State s : ctmc.getStates().keySet()) {
			if(s.getState()[U] > 0) {
				avg_wait += s.getState()[U] * pi.getEntry(ctmc.getStates().get(s));
			}
		}
		System.out.println("Average number of waiting customer: " + avg_wait);
		
		//Average number of customer served per time unit (throughput)
		double served_rate = 0.0;
		for(State s : ctmc.getStates().keySet()) {
			if(s.getState()[US] > 0) {
				served_rate += M_S * pi.getEntry(ctmc.getStates().get(s));
			}
		}
		System.out.println("Average number of customer served per time unit: " + 1.0/served_rate);
		
		//Average number of customers per time unit that cannot enter the shop
		//prendo la prob degli stati in cui ho il massimo dei clienti e lo moltiplico per la rate di arrivo di un nuovo cliente
		//faccio 1 diviso e ottengo il numero medio
		double canter_rate = 0.0;
		for(State s : ctmc.getStates().keySet()) {
			if(s.getState()[U] + s.getState()[US] == MAX_USERS) {
				canter_rate += M_A * pi.getEntry(ctmc.getStates().get(s));
			}
		}
		System.out.println("Average number of customers per time unit that cannot enter the shop: " + 1.0/canter_rate);
		
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
