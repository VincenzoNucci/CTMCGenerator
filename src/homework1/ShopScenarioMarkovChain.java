package homework1;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.function.Function;
import java.util.stream.Collectors;

import quasylab.sibilla.core.markov.MarkovChain;
import quasylab.sibilla.core.markov.TransientProbabilityContinuousSolver;
import quasylab.sibilla.core.markov.ContinuousTimeMarkovChain;

import java.awt.Dimension;


public class ShopScenarioMarkovChain {
	
	public final static int WAITING_CUSTOMERS = 0;
	public final static int SERVED_CUSTOMERS = 1;
	public final static int WAITING_CLERKS = 2;
	public final static int SERVING_CLERKS = 3;
	public final static int CUSTOMERS_OUTSIDE = 4;
	
	//UNIFORM TIME UNIT IS MINUTES
	private int N = 25;
	private int K = 5;
	private double LAMBDA_A = 1/10.0; //ARRIVAL RATE
	//public static double SEL_RATE = 1.0/(0.6); //SELECTION RATE, 10s to select a customer to be served
	private double LAMBDA_S = 1/10.0; //SERVICE TIME
	private ContinuousTimeMarkovChain<State> ctmc;
	
	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
		
		new ShopScenarioMarkovChain(5, 1, 1/1.0, 1/10.0).collectAnalysis();
		new ShopScenarioMarkovChain(5, 1, 1/2.0, 1/10.0).collectAnalysis();
		new ShopScenarioMarkovChain(5, 1, 1/5.0, 1/10.0).collectAnalysis();
		new ShopScenarioMarkovChain(5, 1, 1/10.0, 1/10.0).collectAnalysis();
		System.out.println("DONE");
	}
	
	public ShopScenarioMarkovChain(int shopCapacity, int shopClerks, double arrivalRate, double servedRate) {
		this.N = shopCapacity;
		this.K = shopClerks;
		this.LAMBDA_A = arrivalRate;
		this.LAMBDA_S = servedRate;
		this.ctmc = MarkovChain.generateMarkovChain(ContinuousTimeMarkovChain::new, 
				new State(0,0,this.K,0,0), this.next());
	}
	
	public Function<State,Map<State,Double>> next() {
		return s -> {
			HashMap<State,Double> toReturn = new HashMap<State, Double>();
			int[] values = s.getState();
			
			//A new customer arrives at the shop
			if((values[WAITING_CUSTOMERS] + values[SERVED_CUSTOMERS]) < this.N) {
				//Customer can enter after waiting outside
				if(values[CUSTOMERS_OUTSIDE] > 0) {
					int[] newState = Arrays.copyOf(values, values.length);
					newState[CUSTOMERS_OUTSIDE] = newState[CUSTOMERS_OUTSIDE] - 1;
					newState[WAITING_CUSTOMERS] = newState[WAITING_CUSTOMERS] + 1;
					toReturn.put(new State(newState), values[CUSTOMERS_OUTSIDE]*LAMBDA_S);
				}
				int[] newState = Arrays.copyOf(values, values.length);
				newState[WAITING_CUSTOMERS] = newState[WAITING_CUSTOMERS]+1;
				toReturn.put(new State(newState), LAMBDA_A);
				
			} else { //Shop is full
				int[] newState = Arrays.copyOf(values, values.length);
				newState[CUSTOMERS_OUTSIDE] = newState[CUSTOMERS_OUTSIDE] + 1;
				toReturn.put(new State(newState), LAMBDA_A);
			}
			
			//
			
			//A clerk starts serving a customer
			if(values[WAITING_CLERKS] > 0 && values[WAITING_CUSTOMERS] > 0) {
				int[] newState = Arrays.copyOf(values, values.length);
				newState[WAITING_CLERKS] = newState[WAITING_CLERKS] - 1;
				newState[WAITING_CUSTOMERS] = newState[WAITING_CUSTOMERS] - 1;
				
				newState[SERVING_CLERKS] = newState[SERVING_CLERKS] + 1;
				newState[SERVED_CUSTOMERS] = newState[SERVED_CUSTOMERS] + 1;
				toReturn.put(new State(newState), values[WAITING_CLERKS]*LAMBDA_S);
			}
			
			//A clerk served a customer (and the customer exits)
			if(values[SERVING_CLERKS] > 0 && values[SERVED_CUSTOMERS] > 0) {
				int[] newState = Arrays.copyOf(values, values.length);
				newState[SERVED_CUSTOMERS] = newState[SERVED_CUSTOMERS] - 1;
				newState[SERVING_CLERKS] = newState[SERVING_CLERKS] - 1;
				
				newState[WAITING_CLERKS] = newState[WAITING_CLERKS] + 1;
				toReturn.put(new State(newState), values[SERVING_CLERKS]*(values[SERVED_CUSTOMERS]/N)*LAMBDA_S);
			}
			return toReturn;
		};
	}
	
	public String collectAnalysis() throws FileNotFoundException, InterruptedException {
		
		String path = "data/collect-"+N+'-'+K+'-'+LAMBDA_A+'-'+LAMBDA_S+".csv";
		PrintWriter writer = new PrintWriter(path);
		//writer.write("sample;utilisation;waiting;served;enter");
		TransientProbabilityContinuousSolver<State> solver = new TransientProbabilityContinuousSolver<State>(this.ctmc, 1.0E-6, new State(0,0,this.K,0,0));
		for(int t = 0; t < 1440; t++) {
			Map<State, Double> prob = solver.compute(t);
			
			//Clerks utilisation
			double u = 0.0;
			for(State s : prob.keySet()) {
				if(s.retrieve(SERVING_CLERKS) > 0) {
					u += prob.get(s);
				}
			}
			
			//Average number of waiting customers
			double avg_wait = 0.0;
			for(State s : prob.keySet()) {
				if(s.retrieve(WAITING_CUSTOMERS) > 0) {
					avg_wait += s.retrieve(WAITING_CUSTOMERS) * prob.get(s);
				}
			}
			
			//Average number of customer served per time unit
			double avg_served = 0.0;
			for(State s : prob.keySet()) {
				if(s.retrieve(SERVED_CUSTOMERS) > 0) {
					avg_served += this.LAMBDA_S * prob.get(s);
				}
			}
			
			//Average number of customers per time unit that cannot enter the shop
			double avg_outside = 0.0;
			for(State s : prob.keySet()) {
				if(s.retrieve(CUSTOMERS_OUTSIDE) > 0) {
					avg_outside += prob.get(s);
				}
			}
			writer.write(""+t + ';' + u + ';' + avg_wait + ';' + avg_served + ';'
					+ avg_outside+'\n');
			//Thread.sleep(10);
			writer.flush();
			
		}
		writer.close();
		return path;
	}
	
	public void plot() {
		
	}
	
}
