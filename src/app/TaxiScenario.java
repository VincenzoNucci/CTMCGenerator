package app;

import java.util.Arrays;
import java.util.HashMap;

import app.CTMCGenerator.State;

public class TaxiScenario {
	
	public static int T = 0;
	public static int U = 1;
	public static int TS = 2;
	public static int TL = 3;
	
	public static int MAX_USERS = 10;
	public static int NUMBER_OF_TAXIS = 5;
	public static double LAMBDA = 1.0/5.0;
	public static double BETA = 1.0;
	public static double MU_SHORT = 1.0/10.0;
	public static double MU_LONG = 1.0/30.0;
	public static double P_SHORT = 0.5;
	
	public static void main(String[] args) {
		CTMCGenerator ctmc = new CTMCGenerator(new State(NUMBER_OF_TAXIS,0,0,0), TaxiScenario::next);
		System.out.println("SIZE: " + ctmc.generate());
	}
	
	public static HashMap<State,Double> next(State s) {
		HashMap<State,Double> toReturn = new HashMap<CTMCGenerator.State, Double>();
		int[] values = s.getState();
		
		//A new user arrives at the station
		if(values[U] < MAX_USERS) {
			int[] newState = Arrays.copyOf(values, values.length);
			newState[U] = newState[U] + 1;
			toReturn.put(new State(newState), LAMBDA);
		}
		
		//A user enters in a taxi...
		if((values[U] > 0) && (values[T] > 0)) {
			//...and selects the short trip
			int[] newState = Arrays.copyOf(values, values.length);
			newState[U] = newState[U] - 1;
			newState[T] = newState[T] - 1;
			newState[TS] = newState[TS] + 1;
			toReturn.put(new State(newState), P_SHORT*BETA);
			
			//...and selects the long trip
			newState = Arrays.copyOf(values, values.length);
			newState[U] = newState[U] - 1;
			newState[T] = newState[T] - 1;
			newState[TL] = newState[TL] + 1;
			toReturn.put(new State(newState), (1-P_SHORT)*BETA);
		}
		
		//A taxi returns from a short trip
		if(values[TS] > 0) {
			int[] newState = Arrays.copyOf(values, values.length);
			newState[T] = newState[T] + 1;
			newState[TS] = newState[TS] - 1;
			toReturn.put(new State(newState), MU_SHORT);
		}
		
		//A taxi returns from a long trip
		if(values[TL] > 0) {
			int[] newState = Arrays.copyOf(values, values.length);
			newState[T] = newState[T] + 1;
			newState[TL] = newState[TL] - 1;
			toReturn.put(new State(newState), MU_LONG);
		}

		return toReturn;
	}
	
}
