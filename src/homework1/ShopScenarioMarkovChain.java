package homework1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RRQRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import app.CTMCGenerator.State;
import quasylab.sibilla.core.markov.MarkovChain;
import quasylab.sibilla.core.markov.TransientProbabilityContinuousSolver;
import quasylab.sibilla.core.util.Pair;
import quasylab.sibilla.core.markov.BoundedReachabilityContinuousSolver;
import quasylab.sibilla.core.markov.ContinuousTimeMarkovChain;

import java.awt.Dimension;


public class ShopScenarioMarkovChain {
	
	public final static int WAITING_CUSTOMERS = 0;
	public final static int SERVED_CUSTOMERS = 1;
	public final static int WAITING_CLERKS = 2;
	public final static int SERVING_CLERKS = 3;
	public final static int CUSTOMERS_OUTSIDE = 4;
	
	public final static int UTILISATION = 0;
	public final static int AVG_WAITING = 1;
	public final static int AVG_SERVED = 2;
	public final static int OUTSIDE = 3;
	
	//UNIFORM TIME UNIT IS MINUTES
	private int N = 25;
	private int K = 5;
	private double LAMBDA_A = 1/10.0; //ARRIVAL RATE
	//public static double SEL_RATE = 1.0/(0.6); //SELECTION RATE, 10s to select a customer to be served
	private double LAMBDA_S = 1/10.0; //SERVICE TIME
	private ContinuousTimeMarkovChain<State> ctmc;
	private State init;
	
	public static void main(String[] args) throws InterruptedException, IOException {
		
		//new ShopScenarioMarkovChain(5, 3, 1/10.0, 1/1.0).collectAnalysis(1, 720);
		//new ShopScenarioMarkovChain(5, 1, 1/2.0, 1/10.0).collectAnalysis();
		//new ShopScenarioMarkovChain(5, 1, 1/5.0, 1/10.0).collectAnalysis();
		//new ShopScenarioMarkovChain(5, 1, 1/10.0, 1/10.0).collectAnalysis();
		int K = 1; int N = 1; int hours = 24 * 60;
		//new ShopScenarioMarkovChain(N, K, 1/1.0, 1/1.0).reachPredicate(s -> s.retrieve(SERVED_CUSTOMERS) > 1, 720);
		new ShopScenarioMarkovChain(5,1,1/1.0, 1/10.0).collectAnalysis(1, hours);
		new ShopScenarioMarkovChain(5,1,1/1.0, 1/5.0).collectAnalysis(1, hours);
		new ShopScenarioMarkovChain(5,1,1/1.0, 1/2.0).collectAnalysis(1, hours);
		new ShopScenarioMarkovChain(5,2,1/1.0, 1/1.0).collectAnalysis(1, hours);
		//new ShopScenarioMarkovChain(5,1,1/2.0,1/5.0).collectAnalysis(1, hours);
		System.out.println("DONE");
	}
	
	public ShopScenarioMarkovChain(int shopCapacity, int shopClerks, double arrivalRate, double servedRate) {
		this.N = shopCapacity;
		this.K = shopClerks;
		this.LAMBDA_A = arrivalRate;
		this.LAMBDA_S = servedRate;
		this.init = new State(0,0,this.K,0,0);
		this.ctmc = MarkovChain.generateMarkovChain(ContinuousTimeMarkovChain::new, this.init, this.next());
		System.out.println(this.ctmc.getStates().size());
	}
	
	public Function<State,Map<State,Double>> next() {
		return s -> {
			HashMap<State,Double> toReturn = new HashMap<State, Double>();
			int[] values = s.getState();
			
			//A new customer arrives at the shop
			if((values[WAITING_CUSTOMERS] + values[SERVED_CUSTOMERS]) < this.N) {
				//Customer can enter after waiting outside
				if(values[CUSTOMERS_OUTSIDE] > 0) { //coda FIFO
					int[] newState = Arrays.copyOf(values, values.length);
					newState[CUSTOMERS_OUTSIDE] = newState[CUSTOMERS_OUTSIDE] - 1;
					newState[WAITING_CUSTOMERS] = newState[WAITING_CUSTOMERS] + 1;
					toReturn.put(new State(newState), values[CUSTOMERS_OUTSIDE]*LAMBDA_S); //values[CUSTOMERS_OUTSIDE]*LAMBDA_S
				}
				// la prima rate attiva la transizione
				int[] newState = Arrays.copyOf(values, values.length);
				newState[WAITING_CUSTOMERS] = newState[WAITING_CUSTOMERS]+1;
				toReturn.put(new State(newState), LAMBDA_A);
				
			} else { //Shop is full
				if(values[CUSTOMERS_OUTSIDE] < 100) {
					int[] newState = Arrays.copyOf(values, values.length);
					newState[CUSTOMERS_OUTSIDE] = newState[CUSTOMERS_OUTSIDE] + 1;
					toReturn.put(new State(newState), LAMBDA_A);
				}
			}
			
			//
			
			//A clerk starts serving a customer
			if(values[WAITING_CLERKS] > 0 && values[WAITING_CUSTOMERS] > 0) {
				int[] newState = Arrays.copyOf(values, values.length);
				newState[WAITING_CLERKS] = newState[WAITING_CLERKS] - 1;
				newState[WAITING_CUSTOMERS] = newState[WAITING_CUSTOMERS] - 1;
				
				newState[SERVING_CLERKS] = newState[SERVING_CLERKS] + 1;
				newState[SERVED_CUSTOMERS] = newState[SERVED_CUSTOMERS] + 1;
				toReturn.put(new State(newState), values[WAITING_CLERKS]*values[WAITING_CUSTOMERS] * LAMBDA_S); //values[WAITING_CLERKS]*LAMBDA_S
			}
			
			//A clerk served a customer (and the customer exits)
			if(values[SERVING_CLERKS] > 0 && values[SERVED_CUSTOMERS] > 0) {
				//double d = (double)values[SERVED_CUSTOMERS]/this.N;
				int[] newState = Arrays.copyOf(values, values.length);
				newState[SERVED_CUSTOMERS] = newState[SERVED_CUSTOMERS] - 1;
				newState[SERVING_CLERKS] = newState[SERVING_CLERKS] - 1;
				
				newState[WAITING_CLERKS] = newState[WAITING_CLERKS] + 1;
				toReturn.put(new State(newState), values[SERVING_CLERKS]*values[SERVED_CUSTOMERS]*LAMBDA_S);  //values[SERVING_CLERKS]*values[SERVED_CUSTOMERS]*LAMBDA_S
			}
			return toReturn;
		};
	}
	
	public void reachPredicate(Predicate<State> predicate, int bound) {
		BoundedReachabilityContinuousSolver<State> solver = new BoundedReachabilityContinuousSolver<State>(this.ctmc, 1.0E-6, predicate);
		for(int t = 0; t < bound; t++) {
			Map<State, Double> prob = solver.compute(t);
			System.out.println(prob.get(this.init));
		}
	}
	
	public void collectAnalysis(int replica, int time) throws InterruptedException, IOException {
		String folder = "data/Data-"+N+"-"+K+"-"+1/LAMBDA_A+"-"+1/LAMBDA_S;
		new File(folder).mkdirs();
		String path = folder + "/" + "collect-"+N+'-'+K+'-'+1/LAMBDA_A+'-'+1/LAMBDA_S+"_";
		PrintWriter writer1 = new PrintWriter(path.concat("UTILISATION_.csv"));
		PrintWriter writer2 = new PrintWriter(path.concat("WAITING_.csv"));
		PrintWriter writer3 = new PrintWriter(path.concat("SERVED_.csv"));
		PrintWriter writer4 = new PrintWriter(path.concat("OUTSIDE_.csv"));
		PrintWriter writer5 = new PrintWriter(path.concat("THROUGHPUT_OUTSIDE_.csv"));
		//writer.write("sample;utilisation;waiting;served;enter");
		
		
			
			for(int i = 0; i < replica; i++) {
				TransientProbabilityContinuousSolver<State> solver = new TransientProbabilityContinuousSolver<State>(this.ctmc, 1.0E-6, this.init);
				for(int t = 0; t < time; t++) {
					DescriptiveStatistics dsUtilisation = new DescriptiveStatistics();
					DescriptiveStatistics dsAvgWaiting = new DescriptiveStatistics();
					DescriptiveStatistics dsAvgServed = new DescriptiveStatistics();
					DescriptiveStatistics dsAvgOutside = new DescriptiveStatistics();
					DescriptiveStatistics dsThrOutside = new DescriptiveStatistics();
					
					Map<State, Double> prob = solver.compute(t);
					//System.out.println(prob);
					//Clerks utilisation
					double u = 0.0;
					//Average number of waiting customers
					double avg_wait = 0.0;
					//Average number of customer served per time unit
					double avg_served = 0.0;
					//Average number of customers per time unit that cannot enter the shop
					double avg_outside = 0.0;
					//How many customers per time units arrives and stay outside?
					double throughput_outside = 0.0;
					
					for(State s : prob.keySet()) {
						//if(s.retrieve(SERVING_CLERKS) > 0) {
							if(s.retrieve(SERVING_CLERKS) > 0) {
								u += prob.get(s);
								//System.out.println(s+"" + ' ' + prob.get(s));
							}
						//}
						
						if(s.retrieve(WAITING_CUSTOMERS) > 0) {
							 avg_wait += s.retrieve(WAITING_CUSTOMERS) * prob.get(s);
						}
						
						if(s.retrieve(SERVED_CUSTOMERS) > 0) {
							avg_served += s.retrieve(SERVED_CUSTOMERS) * prob.get(s);
						}
						
						if(s.retrieve(CUSTOMERS_OUTSIDE) > 0) {
							avg_outside += s.retrieve(CUSTOMERS_OUTSIDE) * prob.get(s);
						}
						
						if(s.retrieve(CUSTOMERS_OUTSIDE) > 0) {
							throughput_outside += LAMBDA_A * prob.get(s);
						}
					}
					
					dsUtilisation.addValue(u);
					dsAvgWaiting.addValue(avg_wait);
					dsAvgServed.addValue(avg_served);
					dsAvgOutside.addValue(avg_outside);
					dsThrOutside.addValue(throughput_outside);
					
			
					writer1.write(""+t*1.0 + ';' + dsUtilisation.getMean() + ';' + dsUtilisation.getStandardDeviation() +'\n');
					writer2.write(""+t*1.0 + ';' + dsAvgWaiting.getMean() + ';' + dsAvgWaiting.getStandardDeviation() + '\n');
					writer3.write(""+t*1.0 + ';' + dsAvgServed.getMean() + ';' + dsAvgServed.getStandardDeviation() + '\n');
					writer4.write(""+t*1.0 + ';' + dsAvgOutside.getMean() + ';' + dsAvgOutside.getStandardDeviation() + '\n');
					writer5.write(""+t*1.0 + ';' + dsThrOutside.getMean() + ';' + dsThrOutside.getStandardDeviation() + '\n');
					//Thread.sleep(10);
					writer1.flush();
					writer2.flush();
					writer3.flush();
					writer4.flush();
					writer5.flush();
			}
		}
		writer1.close();
		writer2.close();
		writer3.close();
		writer4.close();
		writer5.close();
		
	}
	
	public RealVector steadyState() {
		Map<State,Map<State,Double>> Q = new HashMap<State, Map<State,Double>>();
		Map<State,Integer> states = new HashMap<State, Integer>();
		int idx = 0;
		
		for (State s : this.ctmc.getStates()) {
			//System.out.println(s);
			states.put(s, idx++);
			//Map<State,Double> row = new HashMap<State, Double>();
			Map<State, Double> row = this.ctmc.rateMatrixRow(s).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
			//System.out.println(row);
			Q.put(s, row);
		}
		
		//System.out.println(states);
		//System.out.println(Q);
		
		
		double[][] Qexport = new double[states.size()][states.size()];
		Map<State,Map<State,Double>> Qcopy = new HashMap<State, Map<State,Double>>();
		Qcopy.putAll(Q);
		
		Q.entrySet().forEach(pair -> {
			State k = pair.getKey();
			Map<State,Double> v = pair.getValue();
			if(states.containsKey(k)) {
				for(State s: v.keySet()) {
					Qexport[(int)states.get(k)][(int)states.get(s)] = (double)v.get(s);
				}
			}
		});
		
		for(int i = 0; i < Qexport.length; i++) {
	    	double sum = 0;
	    	for(int j = 0; j < Qexport.length; j++) {
	    		if(i!=j)
	    			sum += Qexport[i][j];
	    	}
	    	Qexport[i][i] = -sum;
	    }
		for(int i = 0; i < Qexport.length; i++) {
			//System.out.print("[ ");
			for(int j=0;j<Qexport.length;j++) {
				//System.out.print(Qexport[i][j] + " ");
			}
			//System.out.println("]");
		}
		
		RealMatrix qT = MatrixUtils.createRealMatrix(Qexport).transpose();
		//...and substitute last row with all 1's
		double[] normConditionVector = ArrayUtils.toPrimitive(Collections.nCopies(qT.getRowDimension(), 1.0).toArray(new Double[0]));
		// Now qT is qT_N
		qT.setRow(qT.getRowDimension()-1, normConditionVector);
		// create solution vector eN
		//System.out.println(qT);
		
		double[] solutionVector = ArrayUtils.toPrimitive(Collections.nCopies(qT.getRowDimension(), 0.0).toArray(new Double[0]));
		solutionVector[qT.getColumnDimension()-1] = 1.0;
		RealVector eN = new ArrayRealVector(solutionVector);
		//System.out.println(eN);
		
		
		//SymmLQ lqSolver = new SymmLQ(1000000000,1e-11,false);
		//System.out.println(lqSolver.solve((RealLinearOperator) qT, eN));
		DecompositionSolver solver = new LUDecomposition(qT).getSolver();
		RealVector pi = solver.solve(eN);
		
		System.out.println("vector pi" + pi);
		
		return pi;
	}
	
}
