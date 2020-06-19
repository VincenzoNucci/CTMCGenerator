package homework2;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.models.pm.Population;
import quasylab.sibilla.core.models.pm.PopulationModel;
import quasylab.sibilla.core.models.pm.PopulationRule;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.models.pm.ReactionRule;

public class ShopScenarioModelDefinition3 implements ModelDefinition<PopulationState> {

	//Population ENUM
	public final static int WAITING_CLERKS = 0;
	public final static int SERVED_CUSTOMERS = 3;
	public final static int WAITING_CUSTOMERS = 2;
	public final static int WORKING_CLERKS = 1;
	public final static int CUSTOMERS_OUTSIDE = 4;
	
	//SAMPLINGS MAP ENUM
	public final static int CLERK_UTILISATION = 0;
	public final static int CUSTOMER_SERVED = 1;
	public final static int CUSTOMER_WAITING = 2;
	public final static int CUSTOMER_ENTER = 3;
	
	//Population INIT
	public final static int N = 5;
	public final static int K = 1;
	
	//RATES
	public final static double LAMBDA_A = 1/1.0;
	public final static double LAMBDA_S = 1/1.0;
	
	
	
	@Override
	public int stateArity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int modelArity() {
		// TODO Auto-generated method stub
		return 0;
	}

	//INITIAL STATE
	@Override
	public PopulationState state(double... parameters) {
		return new PopulationState( new int[] { K, 0, 0, 0, 0 } );
	}

	@Override
	public Model<PopulationState> createModel(double... args) {
		
		PopulationRule rule_ARR_WAIT_INSIDE = new ReactionRule(
				"Clienti arrivano, aspettano dentro",
				new Population[] {  },
				new Population[] { new Population(WAITING_CUSTOMERS) },
				//s -> s.getOccupancy(WAITING_CUSTOMERS)*LAMBDA_ARRIVAL*(s.getOccupancy())
				(t,s) -> {
					if((N - s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS)) >= LAMBDA_A)
						return LAMBDA_A;
					return 0;
				}
			);
		
		
		PopulationRule rule_WAITING_OUT_ENTERS = new ReactionRule(
				"Clienti che aspettavano fuori ora possono entrare",
				new Population[] { new Population(CUSTOMERS_OUTSIDE)  },
				new Population[] { new Population(WAITING_CUSTOMERS) },
				//s -> s.getOccupancy(WAITING_CUSTOMERS)*LAMBDA_ARRIVAL*(s.getOccupancy())
				(t,s) -> {
					if((N - (s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS))) >= LAMBDA_A)
							return s.getOccupancy(CUSTOMERS_OUTSIDE) * LAMBDA_A;
					return 0;
				}
			);
		
	
		PopulationRule rule_ARR_WAIT_OUTSIDE = new ReactionRule(
				"Clienti che arrivano aspettano fuori",
				new Population[] {  },
				new Population[] { new Population(CUSTOMERS_OUTSIDE) },
				(t,s) -> {
					if((N - (s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS))) <= 0)
						return LAMBDA_A;
					return 0;
				}
			);
		
		PopulationRule rule_SERVED_FROM_QUEUE = new ReactionRule(
				"Clerks servono dalla coda",
				new Population[] { new Population(WAITING_CLERKS), new Population(WAITING_CUSTOMERS) },
				new Population[] { new Population(WORKING_CLERKS), new Population(SERVED_CUSTOMERS) },
				(t,s) -> {
					return s.getOccupancy(WAITING_CLERKS) * s.getOccupancy(WAITING_CUSTOMERS);
				}
			);
		
		PopulationRule rule_FINISH_SERVING = new ReactionRule(
				"Clerks finiscono di servire",
				new Population[] { new Population(SERVED_CUSTOMERS), new Population(WORKING_CLERKS) },
				new Population[] {  new Population(WAITING_CLERKS) },
				(t,s) -> {
					return s.getOccupancy(WORKING_CLERKS)*s.getOccupancy(SERVED_CUSTOMERS)*LAMBDA_S;
				}
			);
		
		
		
		PopulationModel f = new PopulationModel();
		f.addRule(rule_ARR_WAIT_INSIDE);
		f.addRule(rule_WAITING_OUT_ENTERS);
		f.addRule(rule_ARR_WAIT_OUTSIDE);
		f.addRule(rule_SERVED_FROM_QUEUE);
		f.addRule(rule_FINISH_SERVING);
		return f;
	}
	
	public static double utilisationOfK( PopulationState s ) {
		return s.getOccupancy(WORKING_CLERKS)/K;
	}
	
	public static double waitingOfC( PopulationState s ) {
		return s.getOccupancy(WAITING_CUSTOMERS);
	}
	
	public static double servedOfC( PopulationState s ) {
		return s.getOccupancy(SERVED_CUSTOMERS);
	}
	
	public static double outsideOfC( PopulationState s ) {
		return s.getOccupancy(CUSTOMERS_OUTSIDE);
	}

}
