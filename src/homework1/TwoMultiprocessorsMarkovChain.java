package homework1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RRQRDecomposition;
import org.apache.commons.math3.linear.RealLinearOperator;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SymmLQ;

import app.CTMCGenerator;
import app.CTMCGenerator.State;
import quasylab.sibilla.core.markov.ContinuousTimeMarkovChain;
import quasylab.sibilla.core.markov.MarkovChain;
import quasylab.sibilla.core.markov.TransientProbabilityContinuousSolver;
import quasylab.sibilla.core.util.Pair;

public class TwoMultiprocessorsMarkovChain {
	
	public static final int A = 0;
	public static final int B = 1;
	
	public static final int EXECUTE_WAITING = 0;
	public static final int EXECUTE_PRIVATE = 1;
	public static final int EXECUTE_COMMON = 2;
	
	private double LAMBDA_A; //access private
	private double MU_A; //access common
	private double LAMBDA_B;
	private double MU_B;
	private ContinuousTimeMarkovChain<State> ctmc;
	private State init;

	public static void main(String[] args) {
		//new TwoMultiprocessorsMarkovChain(1/20.0, 1/50.0, 1/10.0, 1/20.0).transientAnalysis(1000*60*60*24); //1 giorno (assumendo che le rate siano ms)
		new TwoMultiprocessorsMarkovChain(1/20.0, 1/50.0, 1/10.0, 1/20.0).steadyState();
	}
	
	public TwoMultiprocessorsMarkovChain(double privateRateA, double commonRateA, double privateRateB, double commonRateB) {
		this.LAMBDA_A = privateRateA;
		this.MU_A = commonRateA;
		this.LAMBDA_B = privateRateB;
		this.MU_B = commonRateB;
		this.init = new State(EXECUTE_PRIVATE,EXECUTE_PRIVATE);
		this.ctmc = MarkovChain.generateMarkovChain(ContinuousTimeMarkovChain::new, this.init, next());
		System.out.println(this.ctmc.getStates().size());
		System.out.println(this.ctmc.getStates());
	}
	
	public Function<State,Map<State,Double>> next() {
		return s -> {
			HashMap<State,Double> toReturn = new HashMap<State, Double>();
			int[] values = s.getState();
			
			//A is executing common and wants to go back to private while B is waiting for common
			if(values[A] == EXECUTE_COMMON && values[B] == EXECUTE_WAITING) {
				int[] newState = Arrays.copyOf(values, values.length);
				newState[A] = EXECUTE_PRIVATE;
				newState[B] = EXECUTE_COMMON;
				toReturn.put(new State(newState), LAMBDA_A);
			}
			
			//B is executing common and wants to go back to private while A is waiting for common
			if(values[B] == EXECUTE_COMMON && values[A] == EXECUTE_WAITING) {
				int[] newState = Arrays.copyOf(values, values.length);
				newState[B] = EXECUTE_PRIVATE;
				newState[A] = EXECUTE_COMMON;
				toReturn.put(new State(newState), LAMBDA_B);
			}
			
			//B wants to access common and can do it becuase A is executing private
			if(values[A] == EXECUTE_PRIVATE && values[B] != EXECUTE_COMMON) {
				int[] newState = Arrays.copyOf(values, values.length);
				newState[B] = EXECUTE_COMMON;
				toReturn.put(new State(newState), MU_B);
			}
			
			//A wants to access common and can do it because B is executing private
			if(values[B] == EXECUTE_PRIVATE && values[A] != EXECUTE_COMMON) {
				int[] newState = Arrays.copyOf(values, values.length);
				newState[A] = EXECUTE_COMMON;
				toReturn.put(new State(newState), MU_A);
			}
			
			//B wants to execute common but A is already accessing common so B puts itself waiting
			if(values[A] == EXECUTE_COMMON && values[B] != EXECUTE_WAITING) {
				int[] newState = Arrays.copyOf(values, values.length);
				newState[B] = EXECUTE_WAITING;
				toReturn.put(new State(newState), MU_B);
				newState = Arrays.copyOf(values, values.length);
				newState[A] = EXECUTE_PRIVATE;
				toReturn.put(new State(newState), LAMBDA_A);
			}
			
			//A wants to execute common but B is already accessing common so A puts itself waiting
			if(values[B] == EXECUTE_COMMON && values[A] != EXECUTE_WAITING) {
				int[] newState = Arrays.copyOf(values, values.length);
				newState[A] = EXECUTE_WAITING;
				toReturn.put(new State(newState), MU_A);
				newState = Arrays.copyOf(values, values.length);
				newState[B] = EXECUTE_PRIVATE;
				toReturn.put(new State(newState), LAMBDA_B);
			}
			
			return toReturn;
		};
	}
	
	public void transientAnalysis(int time) {
		TransientProbabilityContinuousSolver<State> solver = new TransientProbabilityContinuousSolver<State>(this.ctmc, 1.0E-6, this.init);
		for(int t = 0; t < time; t++) {
			double u = 0.0;
			Map<State, Double> prob = solver.compute(t);
			for(State s : prob.keySet()) {
				if(s.retrieve(A) == EXECUTE_COMMON || s.retrieve(B) == EXECUTE_COMMON) {
					u += prob.get(s);
					
				}
					
			}
			System.out.println(u);
			//System.out.println(prob.get(new State(EXECUTE_COMMON,EXECUTE_PRIVATE)));
		}
	}
	
	public void steadyState() {
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
		
		System.out.println(states);
		System.out.println(Q);
		
		
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
			System.out.print("[ ");
			for(int j=0;j<Qexport.length;j++) {
				System.out.print(Qexport[i][j] + " ");
			}
			System.out.println("]");
		}
		
		RealMatrix qT = MatrixUtils.createRealMatrix(Qexport).transpose();
		//...and substitute last row with all 1's
		double[] normConditionVector = ArrayUtils.toPrimitive(Collections.nCopies(qT.getRowDimension(), 1.0).toArray(new Double[0]));
		// Now qT is qT_N
		qT.setRow(qT.getRowDimension()-1, normConditionVector);
		// create solution vector eN
		System.out.println(qT);
		
		double[] solutionVector = ArrayUtils.toPrimitive(Collections.nCopies(qT.getRowDimension(), 0.0).toArray(new Double[0]));
		solutionVector[qT.getColumnDimension()-1] = 1.0;
		RealVector eN = new ArrayRealVector(solutionVector);
		System.out.println(eN);
		
		DecompositionSolver solver3 = new RRQRDecomposition(qT).getSolver();
		RealVector pi3 = solver3.solve(eN);
		System.out.println(pi3);
		SymmLQ lqSolver = new SymmLQ(1000000000,1e-11,false);
		System.out.println(lqSolver.solve((RealLinearOperator) qT, eN));
		DecompositionSolver solver = new LUDecomposition(qT).getSolver();
		RealVector pi = solver.solve(eN);
		
		System.out.println("vector pi" + pi);
	}

}
