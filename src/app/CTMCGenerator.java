package app;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.Function;

public class CTMCGenerator {
	
	private HashMap<State,Integer> states = new HashMap<CTMCGenerator.State, Integer>();
	private HashMap<State,HashMap<State,Double>> Q = new HashMap<CTMCGenerator.State, HashMap<State,Double>>();
	private State state;
	private Function<State,HashMap<State,Double>> stepFunction;
	
	public CTMCGenerator(State state, Function<State,HashMap<State,Double>> stepFunction){
		this.state = state;
		this.stepFunction = stepFunction;
	}
	
	public int generate() {
		HashSet<State> pending = new HashSet<CTMCGenerator.State>();
		states = new HashMap<CTMCGenerator.State, Integer>();
		Q = new HashMap<CTMCGenerator.State, HashMap<State,Double>>();
		LinkedList<State> queue = new LinkedList<State>();
		queue.add(state);
		int counter = 0;
		while(!queue.isEmpty()) {
			State next = queue.poll();
			states.put(next, counter++);
			HashMap<State,Double> row = stepFunction.apply(next);
			Q.put(next, row);
			for(State s : row.keySet()) {
				if(!states.containsKey(s)) {
					if(!pending.contains(s)) {
						queue.add(s);
						pending.add(s);
					}
				}
			}
		}
		return getSize();
	}
	
	private int getSize() {
		return states.size();
	}

	public static class State {
		
		private int[] state;
		
		public State(int ... state) {
			this.state = state;
		}
		
		public int[] getState() {
			return state;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(state);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			State other = (State) obj;
			if (!Arrays.equals(state, other.state))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return Arrays.toString(state);
		}
	}
}
