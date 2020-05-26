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
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import org.paukov.combinatorics3.Generator;


public class ShopScenarioMarkovChain {
	
	public static int WAITING_CUSTOMERS = 0;
	public static int SERVED_CUSTOMERS = 1;
	public static int WAITING_CLERKS = 2;
	public static int SERVING_CLERKS = 3;
	
	//UNIFORM TIME UNIT IS MINUTES
	private int shopCapacity = 25;
	private int shopClerks = 5;
	private double lambdaArrival = 1/10.0; //ARRIVAL RATE
	//public static double SEL_RATE = 1.0/(0.6); //SELECTION RATE, 10s to select a customer to be served
	private double lambdaServed = 1/10.0; //SERVICE TIME
	private ContinuousTimeMarkovChain<State> ctmc;
	
	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
		
		new ShopScenarioMarkovChain(5, 1, 1/10.0, 1/10.0).collectAnalysis();
		new ShopScenarioMarkovChain(5, 1, 1/10.0, 1/5.0).collectAnalysis();
		new ShopScenarioMarkovChain(5, 1, 1/10.0, 1/2.0).collectAnalysis();
		new ShopScenarioMarkovChain(5, 1, 1/10.0, 1/1.0).collectAnalysis();
		System.out.println("DONE");
	}
	
	public ShopScenarioMarkovChain(int shopCapacity, int shopClerks, double arrivalRate, double servedRate) {
		this.shopCapacity = shopCapacity;
		this.shopClerks = shopClerks;
		this.lambdaArrival = arrivalRate;
		this.lambdaServed = servedRate;
		this.ctmc = MarkovChain.generateMarkovChain(ContinuousTimeMarkovChain::new, 
				new State(0,0,this.shopClerks,0), this.next());
	}
	
	public Function<State,Map<State,Double>> next() {
		return s -> {
			HashMap<State,Double> toReturn = new HashMap<State, Double>();
			int[] values = s.getState();
			
			//A new customer arrives at the shop
			if(values[WAITING_CUSTOMERS] + values[SERVED_CUSTOMERS] < this.shopCapacity) {
				int[] newState = Arrays.copyOf(values, values.length);
				//If there is a free clerk, the customer is quickly served
				if(values[WAITING_CLERKS] > 0) {
					newState[SERVED_CUSTOMERS] = newState[SERVED_CUSTOMERS] + 1;
					newState[WAITING_CLERKS] = newState[WAITING_CLERKS] - 1;
					newState[SERVING_CLERKS] = newState[SERVING_CLERKS] + 1;
					toReturn.put(new State(newState), this.lambdaArrival);
				} else { //There are no free clerks, so the customer must wait his/her turn
					newState[WAITING_CUSTOMERS] = newState[WAITING_CUSTOMERS] + 1;
					toReturn.put(new State(newState), this.lambdaArrival);
				}
				
			} else { //Shop is full but a clerk is free
				int[] newState = Arrays.copyOf(values, values.length);
				if(values[WAITING_CLERKS] > 0) {
					newState[WAITING_CUSTOMERS] = newState[WAITING_CUSTOMERS] - 1;
					newState[SERVED_CUSTOMERS] = newState[SERVED_CUSTOMERS] + 1;
					newState[WAITING_CLERKS] = newState[WAITING_CLERKS] - 1;
					newState[SERVING_CLERKS] = newState[SERVING_CLERKS] + 1;
					toReturn.put(new State(newState), this.lambdaArrival);
				}
			}
			
			//A clerk served a customer (and the customer exits)
			if(values[SERVING_CLERKS] > 0 && values[SERVED_CUSTOMERS] > 0) {
				int[] newState = Arrays.copyOf(values, values.length);
				newState[SERVED_CUSTOMERS] = newState[SERVED_CUSTOMERS] - 1;
				newState[SERVING_CLERKS] = newState[SERVING_CLERKS] - 1;
				newState[WAITING_CLERKS] = newState[WAITING_CLERKS] + 1;
				toReturn.put(new State(newState), this.lambdaServed);
			}
			return toReturn;
		};
	}
	
	public String collectAnalysis() throws FileNotFoundException, InterruptedException {
		
		String path = "data/collect-"+shopCapacity+'-'+shopClerks+'-'+lambdaArrival+'-'+lambdaServed+".csv";
		PrintWriter writer = new PrintWriter(path);
		//writer.write("sample;utilisation;waiting;served;enter");
		TransientProbabilityContinuousSolver<State> solver = new TransientProbabilityContinuousSolver<State>(this.ctmc, 1.0E-6, new State(0,0,this.shopClerks,0));
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
			double served_rate = 0.0;
			for(State s : prob.keySet()) {
				if(s.retrieve(SERVED_CUSTOMERS) > 0) {
					served_rate += this.lambdaServed * prob.get(s);
				}
			}
			
			//Average number of customers per time unit that cannot enter the shop
			double canter_rate2 = 0.0;
			for(State s : prob.keySet()) {
				if(s.retrieve(WAITING_CUSTOMERS) + s.retrieve(SERVED_CUSTOMERS) >= this.shopCapacity) {
					canter_rate2 += this.lambdaArrival * prob.get(s);
				}
			}
			writer.write(""+t + ';' + u + ';' + avg_wait + ';' + served_rate + ';'
					+ canter_rate2+'\n');
			//Thread.sleep(10);
			writer.flush();
			
		}
		writer.close();
		return path;
	}
	
	public void plot() {
		
	}
	
}
