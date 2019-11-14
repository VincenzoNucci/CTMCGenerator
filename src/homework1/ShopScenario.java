package homework1;

import java.util.Arrays;
import java.util.HashMap;

import app.CTMCGenerator;
import app.CTMCGenerator.State;

public class ShopScenario {
	
	public static int C = 0;
	public static int U = 1;
	public static int US = 2;
	//public static int TS = 2;
	//public static int TL = 3;
	
	public static int MAX_USERS = 5;
	public static int MAX_CLERKS = 1;
	public static double M_A = 1.0; //ARRIVAL RATE
	public static double SEL_RATE = 1.0/0.5; //30s to select a customer to be served
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
		//Compute steady state probability
		
		
		//System.err.println(ctmc.statesToString());
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
