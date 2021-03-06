package homework2;

import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.models.pm.Population;
import quasylab.sibilla.core.models.pm.PopulationModel;
import quasylab.sibilla.core.models.pm.PopulationRule;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.models.pm.ReactionRule;

public class ShopScenarioModelDefinition implements ModelDefinition<PopulationState> {

	//Population ENUM
	public final static int WAITING_CUSTOMERS = 0;
	public final static int SERVED_CUSTOMERS = 1;
	public final static int WAITING_CLERKS = 2;
	public final static int SERVING_CLERKS = 3;
	
	//SAMPLINGS MAP ENUM
	public final static int CLERK_UTILISATION = 0;
	public final static int CUSTOMER_SERVED = 1;
	public final static int CUSTOMER_WAITING = 2;
	public final static int CUSTOMER_ENTER = 3;
	
	//Population INIT
	public final static int SHOP_CAPACITY = 5;
	public final static int SHOP_CLERKS = 1;
	
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
		return new PopulationState( new int[] { 0, 0, SHOP_CLERKS, 0 } );
	}

	@Override
	public Model<PopulationState> createModel(double... args) {
		PopulationRule rule_ARR_SERV = new ReactionRule(
				"Queue moves",
				new Population[] { new Population(WAITING_CLERKS) },
				new Population[] { new Population(SERVING_CLERKS), new Population(SERVED_CUSTOMERS) },
				(t,s) -> { 
					if(s.getOccupancy(WAITING_CLERKS) > 0 && s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS) < SHOP_CAPACITY)
						return s.getOccupancy(WAITING_CLERKS)*LAMBDA_A;
					return 0;
					}
			);
		
		
		PopulationRule rule_ARR = new ReactionRule(
				"Customer arrives at the shop and he puts himself into queue",
				new Population[] {  },
				new Population[] { new Population(WAITING_CUSTOMERS) },
				//s -> s.getOccupancy(WAITING_CUSTOMERS)*LAMBDA_ARRIVAL*(s.getOccupancy())
				(t,s) -> {
					if(s.getOccupancy(WAITING_CLERKS) <= 0 && s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS) < SHOP_CAPACITY)
						return LAMBDA_A;
					return 0;
					
				}
			);
		
		/*
		PopulationRule rule_WAIT_OUT = new ReactionRule(
				"Customer arrives at the shop and waits outside",
				new Population[] {  },
				new Population[] { new Population(WAITING_CUSTOMERS_OUTSIDE) },
				//s -> s.getOccupancy(WAITING_CUSTOMERS)*LAMBDA_ARRIVAL*(s.getOccupancy())
				(t,s) -> {
					if(s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS) >= shop_capacity)
						return s.getOccupancy(WAITING_CUSTOMERS_OUTSIDE)*LAMBDA_A;
					
				}
			);
		*/
	
		PopulationRule rule_SERV = new ReactionRule(
				"Clerk served a customer and the customer exits",
				new Population[] { new Population(SERVED_CUSTOMERS), new Population(SERVING_CLERKS) },
				new Population[] { new Population(WAITING_CLERKS) },
				(t,s) -> {
					return s.getOccupancy(SERVED_CUSTOMERS)*s.getOccupancy(SERVING_CLERKS)*LAMBDA_S;
				}
			);
		
		PopulationModel f = new PopulationModel();
		f.addRule(rule_ARR);
		//f.addRule(rule_WAIT_OUT);
		f.addRule(rule_ARR_SERV);
		f.addRule(rule_SERV);
		return f;
	}
	
	public static double utilisationOfK( PopulationState s ) {
		return s.getOccupancy(SERVING_CLERKS)/SHOP_CLERKS;
	}
	
	public static double waitingOfC( PopulationState s ) {
		return s.getOccupancy(WAITING_CUSTOMERS);
	}
	
	public static double servedOfC( PopulationState s ) {
		return s.getOccupancy(SERVED_CUSTOMERS);
	}
	
	public static double outsideOfC( PopulationState s ) {
		if(s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS) >= SHOP_CAPACITY)
			return LAMBDA_A;
		return 0;
	}

}
