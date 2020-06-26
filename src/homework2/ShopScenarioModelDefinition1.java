package homework2;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.models.pm.Population;
import quasylab.sibilla.core.models.pm.PopulationModel;
import quasylab.sibilla.core.models.pm.PopulationRule;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.models.pm.ReactionRule;

public class ShopScenarioModelDefinition1 implements ModelDefinition<PopulationState> {

	//Population ENUM
	public final static int WAITING_CLERKS = 0;
	public final static int SERVED_CUSTOMERS = 1;
	public final static int WAITING_CUSTOMERS = 2;
	public final static int WORKING_CLERKS = 3;
	public final static int CUSTOMERS_OUTSIDE = 4;
	
	//SAMPLINGS MAP ENUM
	public final static int CLERK_UTILISATION = 0;
	public final static int CUSTOMER_SERVED = 1;
	public final static int CUSTOMER_WAITING = 2;
	public final static int CUSTOMER_ENTER = 3;
	
	//Population INIT
	public final static int N = 5;
	public final static int K = 5;
	
	//RATES
	public final static double LAMBDA_A = 1.0;
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
		
		
		////////////////////////////////////////// ENTER RULES
		
		PopulationRule rule_ENTERS_AND_WAIT_INSIDE = new ReactionRule(
				"A new customer arrives at the shop and puts into queue bc no clerks available to serve him",
				new Population[] {  },
				new Population[] { new Population(WAITING_CUSTOMERS) },
				(t,s) -> {
					if(s.getOccupancy(WAITING_CLERKS) <= 0 && (s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS)) < N)
						return LAMBDA_A;
					return 0;
				}
			);
		
		PopulationRule rule_WAIT_OUTSIDE = new ReactionRule(
				"Customer arrives at the shop and waits outside",
				new Population[] {  },
				new Population[] { new Population(CUSTOMERS_OUTSIDE) },
				(t,s) -> {
					if(s.getOccupancy(WAITING_CLERKS) <= 0 && (s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS)) >= N)
						return LAMBDA_A;
					else
						return 0;
				}
			);
		
		PopulationRule rule_OUTSIDE_ENTERS = new ReactionRule(
				"Waiting outside customer enters the shop and puts into queue after a customer has been served",
				new Population[] { new Population(CUSTOMERS_OUTSIDE) },
				new Population[] { new Population(WAITING_CUSTOMERS) },
				(t,s) -> {
					if(s.getOccupancy(WAITING_CLERKS)<= 0 && (s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS)) < N)
						return LAMBDA_S;
					return 0;
				}
			);
		
		////////////////////////////////////////////// SERVED RULES
		
		PopulationRule rule_SERV = new ReactionRule(
				"Clerk served a customer and the customer exits",
				new Population[] { new Population(SERVED_CUSTOMERS), new Population(WORKING_CLERKS) },
				new Population[] { new Population(WAITING_CLERKS) },
				(t,s) -> {
					return s.getOccupancy(WORKING_CLERKS)*s.getOccupancy(SERVED_CUSTOMERS)*LAMBDA_S;
				}
			);
		
		PopulationRule rule_SERVED_FROM_ENTERING = new ReactionRule(
				"A new customer arrives at the shop and becomes served bc clerks available to serve him",
				new Population[] { new Population(WAITING_CLERKS) },
				new Population[] { new Population(SERVED_CUSTOMERS), new Population(WORKING_CLERKS) },
				(t,s) -> {
					if((s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS)) < N)
						return s.getOccupancy(WAITING_CLERKS)*LAMBDA_A;
					return 0;
				}
			);
		
		PopulationRule rule_SERVED_FROM_QUEUE = new ReactionRule(
				"customer waiting in queue at the shop is served",
				new Population[] { new Population(WAITING_CLERKS), new Population(WAITING_CUSTOMERS) },
				new Population[] { new Population(SERVED_CUSTOMERS), new Population(WORKING_CLERKS) },
				//s -> s.getOccupancy(WAITING_CUSTOMERS)*LAMBDA_ARRIVAL*(s.getOccupancy())
				(t,s) -> {
					//if(s.getOccupancy(WAITING_CLERKS) > 0 && s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS) < MAX_WAITING_CUSTOMERS)
						return s.getOccupancy(WAITING_CLERKS)*s.getOccupancy(WAITING_CUSTOMERS)*LAMBDA_S;
					
				}
			);
		
		PopulationRule rule_SERVED_FROM_OUTSIDE = new ReactionRule(
				"customer waiting outside the shop enters and is served",
				new Population[] { new Population(WAITING_CLERKS), new Population(CUSTOMERS_OUTSIDE) },
				new Population[] { new Population(SERVED_CUSTOMERS), new Population(WORKING_CLERKS) },
				//s -> s.getOccupancy(WAITING_CUSTOMERS)*LAMBDA_ARRIVAL*(s.getOccupancy())
				(t,s) -> {
					//if(s.getOccupancy(WAITING_CLERKS) > 0 && s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS) < MAX_WAITING_CUSTOMERS)
						return s.getOccupancy(WAITING_CLERKS)*s.getOccupancy(CUSTOMERS_OUTSIDE)*LAMBDA_S;
					
				}
			);
		
		PopulationModel f = new PopulationModel();
		f.addRule(rule_SERV);
		f.addRule(rule_WAIT_OUTSIDE);
		f.addRule(rule_OUTSIDE_ENTERS);
		f.addRule(rule_ENTERS_AND_WAIT_INSIDE);
		f.addRule(rule_WAIT_OUTSIDE);
		f.addRule(rule_SERVED_FROM_OUTSIDE);
		f.addRule(rule_SERVED_FROM_QUEUE);
		f.addRule(rule_SERVED_FROM_ENTERING);
		
		return f;
	}
	
	public static double utilisationOfK( PopulationState s ) {
		return s.getOccupancy(WORKING_CLERKS);
		
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
