package homework2;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.models.pm.Population;
import quasylab.sibilla.core.models.pm.PopulationModel;
import quasylab.sibilla.core.models.pm.PopulationRule;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.models.pm.ReactionRule;

public class ShopScenarioModelDefinition2 implements ModelDefinition<PopulationState> {

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
	public final static int N = 10;
	public final static int K = 2;
	
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
		
		PopulationRule rule_CUST_ARRIVES_AND_WAIT_QUEUE = new ReactionRule(
				"A customer arrives and puts himself into queue",
				new Population[] {  },
				new Population[] { new Population(WAITING_CUSTOMERS) },
				(t,s) -> {
					return (s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS)) < N ? 
							LAMBDA_A : 0;
				}
				
			);
		
		PopulationRule rule_CUST_ARRIVES_AND_WAIT_OUTSIDE = new ReactionRule(
				"A customer arrives and waits outside since he cannot enter",
				new Population[] {  },
				new Population[] { new Population(CUSTOMERS_OUTSIDE) },
				(t,s) -> {
					return (s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS)) >= N ? 
							LAMBDA_A : 0;
				}
				
			);
		
		//ora c'è posto e può entrare dopo aver aspettato fuori
		PopulationRule rule_WAITING_OUTSIDE_ENTERS = new ReactionRule(
				"A customer can enter after waiting outside",
				new Population[] { new Population(CUSTOMERS_OUTSIDE) },
				new Population[] { new Population(WAITING_CUSTOMERS) },
				(t,s) -> {
					return (s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS)) < N ? 
							s.getOccupancy(CUSTOMERS_OUTSIDE)*LAMBDA_S : 0;
				}
				
			);
		
		//passa dalla coda all'essere servito
		PopulationRule rule_CLERKS_START_SERVING_CUSTOMER = new ReactionRule(
				"A customer is selected from queue and start being served",
				new Population[] { new Population(WAITING_CLERKS), new Population(WAITING_CUSTOMERS) },
				new Population[] { new Population(SERVED_CUSTOMERS), new Population(WORKING_CLERKS) },
				(t,s) -> {
					//return s.getOccupancy(WAITING_CLERKS)*s.getOccupancy(WAITING_CUSTOMERS)*LAMBDA_S;
					return s.getOccupancy(WAITING_CLERKS)*LAMBDA_S;
				}
				
			);
		
		//viene servito e se ne va
		PopulationRule rule_CLERKS_SERVES_CUSTOMER = new ReactionRule(
				"A clerk serves a customer",
				new Population[] { new Population(WORKING_CLERKS), new Population(SERVED_CUSTOMERS) },
				new Population[] { new Population(WAITING_CLERKS) },
				(t,s) -> {
					//return s.getOccupancy(WORKING_CLERKS)*s.getOccupancy(SERVED_CUSTOMERS)*LAMBDA_S;
					return s.getOccupancy(WORKING_CLERKS)*s.getFraction(SERVED_CUSTOMERS)*LAMBDA_S;
				}
				
			);
		
		
		
		PopulationModel f = new PopulationModel();
		f.addRule(rule_CUST_ARRIVES_AND_WAIT_QUEUE);
		f.addRule(rule_CUST_ARRIVES_AND_WAIT_OUTSIDE);
		f.addRule(rule_CLERKS_SERVES_CUSTOMER);
		f.addRule(rule_CLERKS_START_SERVING_CUSTOMER);
		f.addRule(rule_WAITING_OUTSIDE_ENTERS);
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
