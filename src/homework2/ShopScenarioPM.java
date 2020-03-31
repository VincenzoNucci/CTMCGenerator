package homework2;

import quasylab.sibilla.core.simulator.*;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.HashMap;

import quasylab.sibilla.core.simulator.pm.PopulationModel;
import quasylab.sibilla.core.simulator.pm.PopulationRule;
import quasylab.sibilla.core.simulator.pm.PopulationState;
import quasylab.sibilla.core.simulator.pm.ReactionRule;
import quasylab.sibilla.core.simulator.pm.ReactionRule.Specie;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling; 

public class ShopScenarioPM {
	
	private HashMap<String, ReactionRule> rules = new HashMap<>();
	private final DefaultRandomGenerator rng = new DefaultRandomGenerator();
	
	//Population ENUM
	public final static int WAITING_CUSTOMERS = 0;
	public final static int SERVED_CUSTOMERS = 1;
	public final static int WAITING_CLERKS = 2;
	public final static int SERVING_CLERKS = 3;
	
	//Population INIT
	//public final static int INIT_WDCU = 100; //Total population of customers
	public final static int SHOP_CAPACITY = 5;
	//public final static int INIT_SCU = 0;
	public final static int SHOP_CLERKS = 1;
	//public final static int INIT_SCL = 0;
	public final static double N = SHOP_CAPACITY + SHOP_CLERKS;
	
	//Simulation RATES - MINUTES
	public final static double LAMBDA_ARRIVAL = 1/5.0;
	public final static double LAMBDA_SERVED = 1/10.0;
	
	//SIMULATION PARAMETERS
	public final static int SAMPLINGS = 1440; //quante righe ho nel file .data della simulazione
	public final static double DEADLINE = 1440; //il tempo massimo che mi fermo nella simulazione. 720 significa che simulo per 12h
	//e avendo 720 SAMPLINGS significa che simulo la situazione dello shop ogni due minuti
	private static final int REPLICA = 10000;
	private final static int TASKS = 8;
	
	
	public static void main(String[] args) throws FileNotFoundException, InterruptedException, UnknownHostException {
		
		PopulationRule rule_ARR = new ReactionRule(
				"Customer arrives at the shop",
				new Specie[] {  },
				new Specie[] { new Specie(WAITING_CUSTOMERS) },
				//s -> s.getOccupancy(WAITING_CUSTOMERS)*LAMBDA_ARRIVAL*(s.getOccupancy())
				s -> {
					if(s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS) < SHOP_CAPACITY) {
						//return LAMBDA_ARRIVAL;
						return LAMBDA_ARRIVAL;
					} else
						return 0.0;
				}
			);
		
		PopulationRule rule_ARR_SERV = new ReactionRule(
				"Customer arrives at the shop",
				new Specie[] { new Specie(WAITING_CUSTOMERS), new Specie(WAITING_CLERKS) },
				new Specie[] { new Specie(SERVED_CUSTOMERS), new Specie(SERVING_CLERKS) },
				//s -> s.getOccupancy(WAITING_CUSTOMERS)*LAMBDA_ARRIVAL*(s.getOccupancy())
				s -> {
					if(s.getOccupancy(WAITING_CUSTOMERS) > 0 && s.getOccupancy(WAITING_CLERKS) > 0) {
						//return LAMBDA_ARRIVAL;
						return LAMBDA_ARRIVAL;
					} else
						return 0.0;
				}
			);
	
		PopulationRule rule_SERV = new ReactionRule(
				"Clerk served a customer",
				new Specie[] { new Specie(SERVED_CUSTOMERS), new Specie(SERVING_CLERKS) },
				new Specie[] { new Specie(WAITING_CLERKS) },
				//s -> s.getOccupancy()*LAMBDA_SERVED*(s.getOccupancy())
				s -> {//Everytime a customer enters the shop, if there is a waiting clerk, the customer is served
					if(s.getOccupancy(SERVED_CUSTOMERS) > 0 && s.getOccupancy(SERVING_CLERKS)> 0) {
						return LAMBDA_SERVED;
					}
					else
						return 0.0;
				}
			);
		
		PopulationModel f = new PopulationModel();
		f.addState("init", initialState());
		f.addRule(rule_ARR);
		f.addRule(rule_ARR_SERV);
		f.addRule(rule_SERV);
		
		StatisticSampling<PopulationState> clerkUtilisationSamp = StatisticSampling.measure("Clerks utilisation",
				SAMPLINGS, DEADLINE, s -> s.getOccupancy(SERVING_CLERKS)/SHOP_CLERKS);
		StatisticSampling<PopulationState> customerServedSamp = StatisticSampling.measure("#CustomerServed",
				SAMPLINGS, DEADLINE, s -> s.getOccupancy(SERVED_CUSTOMERS));
		StatisticSampling<PopulationState> customerWaitingSamp = StatisticSampling.measure("#CustomerWaiting",
				SAMPLINGS, DEADLINE, s -> s.getOccupancy(WAITING_CUSTOMERS));
		StatisticSampling<PopulationState> customerCantEnterSamp = StatisticSampling.measure("#CustomerCantEnter",
				SAMPLINGS, DEADLINE, s -> {
						if((s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS) == SHOP_CAPACITY)) {
							return LAMBDA_ARRIVAL;
						} else
							return 0.0;
					});
		
		SimulationEnvironment sim = new SimulationEnvironment( ThreadSimulationManager.getFixedThreadSimulationManagerFactory(TASKS) );
		
		SamplingFunction<PopulationState> sf = new SamplingCollection<>(
				clerkUtilisationSamp,
				customerServedSamp,
				customerWaitingSamp,
				customerCantEnterSamp
			);
		
		//Start the simulation
		sim.simulate(new DefaultRandomGenerator(),f,initialState(),sf,REPLICA,DEADLINE);
		
		System.out.println(clerkUtilisationSamp.getSimulationTimeSeries(REPLICA).getFirst().getSize());
		
		clerkUtilisationSamp.printTimeSeries(new PrintStream("data/ShopScenarionPM_"+REPLICA+"_"+N+"_ClerkUtilisation_.data"),';');
		customerServedSamp.printTimeSeries(new PrintStream("data/ShopScenarionPM_"+REPLICA+"_"+N+"_CustomerServed_.data"),';');
		customerWaitingSamp.printTimeSeries(new PrintStream("data/ShopScenarionPM_"+REPLICA+"_"+N+"_CustomerWaiting_.data"),';');
		customerCantEnterSamp.printTimeSeries(new PrintStream("data/ShopScenarionPM_"+REPLICA+"_"+N+"_CustomerWandering_.data"),';');
	}
	
	public static PopulationState initialState() {
		return new PopulationState(new int[] { 0,0, SHOP_CLERKS, 0 });
	}
	
	
}
