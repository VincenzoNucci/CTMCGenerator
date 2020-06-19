//package homework2;
//
//import quasylab.sibilla.core.simulator.*;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.PrintStream;
//import java.net.UnknownHostException;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.Map;
//
//import homework1.ShopScenarioMarkovChain;
//import quasylab.sibilla.core.simulator.pm.PopulationModel;
//import quasylab.sibilla.core.simulator.pm.PopulationRule;
//import quasylab.sibilla.core.simulator.pm.PopulationState;
//import quasylab.sibilla.core.simulator.pm.ReactionRule;
//import quasylab.sibilla.core.simulator.pm.ReactionRule.Specie;
//import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
//import quasylab.sibilla.core.simulator.sampling.SamplingFunction;
//import quasylab.sibilla.core.simulator.sampling.StatisticSampling; 
//
//public class ShopScenarioPM {
//	
//	private HashMap<String, ReactionRule> rules = new HashMap<>();
//	private final DefaultRandomGenerator rng = new DefaultRandomGenerator();
//	
//	//Population ENUM
//	public final static int WAITING_CUSTOMERS = 0;
//	public final static int WAITING_CUSTOMERS_OUTSIDE = 1;
//	public final static int SERVED_CUSTOMERS = 2;
//	public final static int WAITING_CLERKS = 3;
//	public final static int SERVING_CLERKS = 4;
//	
//	//SAMPLINGS MAP ENUM
//	public final static int CLERK_UTILISATION = 0;
//	public final static int CUSTOMER_SERVED = 1;
//	public final static int CUSTOMER_WAITING = 2;
//	public final static int CUSTOMER_ENTER = 3;
//	
//	//Population INIT
//	//public final static int INIT_WDCU = 100; //Total population of customers
//	private int shop_capacity;
//	//public final static int INIT_SCU = 0;
//	private int shop_clerks;
//	//public final static int INIT_SCL = 0;
//	private int N;
//	
//	private PopulationModel f;
//	private SamplingFunction<PopulationState> sf;
//	private Map<Integer,StatisticSampling<PopulationState>> samplings;
//	
//	//Simulation RATES - MINUTES
//	private double lambda_arrival;
//	private double lambda_served;
//	
//	//SIMULATION PARAMETERS
//	public final static int SAMPLINGS = 1440; //quante righe ho nel file .data della simulazione
//	public final static double DEADLINE = 1440; //il tempo massimo che mi fermo nella simulazione. 720 significa che simulo per 12h
//	//e avendo 720 SAMPLINGS significa che simulo la situazione dello shop ogni due minuti
//	private static final int REPLICA = 10000;
//	private final static int TASKS = 8;
//	
//	public ShopScenarioPM(int users, int clerks, double arrival_rate, double exit_rate) {
//		this.shop_capacity = users;
//		this.shop_clerks = clerks;
//		this.lambda_arrival = arrival_rate;
//		this.lambda_served = exit_rate;
//		samplings = new HashMap<Integer,StatisticSampling<PopulationState>>();
//		this.N = shop_capacity + shop_clerks;
//		
//		f = this.setRules();
//		
//		sf = this.setSamplings();
//		
//		
//		
//		//SimulationEnvironment sim = new SimulationEnvironment( ThreadSimulationManager.getFixedThreadSimulationManagerFactory(TASKS) );
//		
//		
//		
//	}
//	
//	public static void main(String[] args) throws FileNotFoundException, InterruptedException, UnknownHostException {
//		
//		//POCHI POSTI - POCHI COMMESSI
//		new ShopScenarioPM(5,1,1/2.0,1/10.0).collectAnalysis(); //pochi posti e un solo commesso, sono entrambi lenti
//		new ShopScenarioPM(5,1,1/2.0,1/5.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti cominciano a velocizzare
//		new ShopScenarioPM(5,1,1/2.0,1/2.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti velocizzano di piu
//		new ShopScenarioPM(5,1,1/2.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti vanno al massimo
//		
////		new ShopScenarioPM(5,1,1/10.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso è lento
////		new ShopScenarioPM(5,1,1/5.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso comincia a velocizzare
////		new ShopScenarioPM(5,1,1/2.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso velocizza di piu
////		new ShopScenarioPM(5,1,1/1.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso va al massimo
//		
////		new ShopScenarioPM(5,1,1/1.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, sono entrambi veloci
////		new ShopScenarioPM(5,1,1/1.0,1/2.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti rallentano
////		new ShopScenarioPM(5,1,1/1.0,1/5.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti rallentano di piu
////		new ShopScenarioPM(5,1,1/1.0,1/10.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti vanno al minimo
//		
//		//POSTI MEDI - COMMESSI MEDI
////		new ShopScenarioPM(10,2,1/10.0,1/10.0).collectAnalysis(); //pochi posti e un solo commesso, sono entrambi lenti
////		new ShopScenarioPM(10,2,1/10.0,1/5.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti cominciano a velocizzare
////		new ShopScenarioPM(10,2,1/10.0,1/2.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti velocizzano di piu
////		new ShopScenarioPM(10,2,1/10.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti vanno al massimo
//		
////		new ShopScenarioPM(10,2,1/10.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso è lento
////		new ShopScenarioPM(10,2,1/5.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso comincia a velocizzare
////		new ShopScenarioPM(10,2,1/2.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso velocizza di piu
////		new ShopScenarioPM(10,2,1/1.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso va al massimo
////		
////		new ShopScenarioPM(10,2,1/1.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, sono entrambi veloci
////		new ShopScenarioPM(10,2,1/1.0,1/2.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti rallentano
////		new ShopScenarioPM(10,2,1/1.0,1/5.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti rallentano di piu
////		new ShopScenarioPM(10,2,1/1.0,1/10.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti vanno al minimo
////		
////		//POSTI TANTI - COMMESSI TANTI
////		new ShopScenarioPM(25,5,1/10.0,1/10.0).collectAnalysis(); //pochi posti e un solo commesso, sono entrambi lenti
////		new ShopScenarioPM(25,5,1/10.0,1/5.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti cominciano a velocizzare
////		new ShopScenarioPM(25,5,1/10.0,1/2.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti velocizzano di piu
////		new ShopScenarioPM(25,5,1/10.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti vanno al massimo
////		
////		new ShopScenarioPM(25,5,1/10.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso è lento
////		new ShopScenarioPM(25,5,1/5.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso comincia a velocizzare
////		new ShopScenarioPM(25,5,1/2.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso velocizza di piu
////		new ShopScenarioPM(25,5,1/1.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso va al massimo
////		
////		new ShopScenarioPM(25,5,1/1.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, sono entrambi veloci
////		new ShopScenarioPM(25,5,1/1.0,1/2.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti rallentano
////		new ShopScenarioPM(25,5,1/1.0,1/5.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti rallentano di piu
////		new ShopScenarioPM(25,5,1/1.0,1/10.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti vanno al minimo
////		
////		//POSTI TANTI - POCHI COMMESSI
////		new ShopScenarioPM(25,1,1/10.0,1/10.0).collectAnalysis(); //pochi posti e un solo commesso, sono entrambi lenti
////		new ShopScenarioPM(25,1,1/10.0,1/5.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti cominciano a velocizzare
////		new ShopScenarioPM(25,1,1/10.0,1/2.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti velocizzano di piu
////		new ShopScenarioPM(25,1,1/10.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti vanno al massimo
////		
////		new ShopScenarioPM(25,1,1/10.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso è lento
////		new ShopScenarioPM(25,1,1/5.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso comincia a velocizzare
////		new ShopScenarioPM(25,1,1/2.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso velocizza di piu
////		new ShopScenarioPM(25,1,1/1.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso va al massimo
////		
////		new ShopScenarioPM(5,1,1/1.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, sono entrambi veloci
////		new ShopScenarioPM(5,1,1/1.0,1/2.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti rallentano
////		new ShopScenarioPM(5,1,1/1.0,1/5.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti rallentano di piu
////		new ShopScenarioPM(5,1,1/1.0,1/10.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti vanno al minimo
////		
////		//POCHI POSTI - COMMESSI MEDI
////		new ShopScenarioPM(5,2,1/10.0,1/10.0).collectAnalysis(); //pochi posti e un solo commesso, sono entrambi lenti
////		new ShopScenarioPM(5,2,1/10.0,1/5.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti cominciano a velocizzare
////		new ShopScenarioPM(5,2,1/10.0,1/2.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti velocizzano di piu
////		new ShopScenarioPM(5,2,1/10.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti vanno al massimo
////		
////		new ShopScenarioPM(5,2,1/10.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso è lento
////		new ShopScenarioPM(5,2,1/5.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso comincia a velocizzare
////		new ShopScenarioPM(5,2,1/2.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso velocizza di piu
////		new ShopScenarioPM(5,2,1/1.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, il commesso va al massimo
////		
////		new ShopScenarioPM(5,2,1/1.0,1/1.0).collectAnalysis(); //pochi posti e un solo commesso, sono entrambi veloci
////		new ShopScenarioPM(5,2,1/1.0,1/2.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti rallentano
////		new ShopScenarioPM(5,2,1/1.0,1/5.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti rallentano di piu
////		new ShopScenarioPM(5,2,1/1.0,1/10.0).collectAnalysis(); //pochi posti e un solo commesso, i clienti vanno al minimo
////		
//		
//		
//	}
//	
//	public PopulationState initialState() {
//		return new PopulationState(new int[] { 0, 0, 0, this.shop_clerks, 0 });
//	}
//	
//	public PopulationModel setRules() {
//		
//		
//		PopulationRule rule_MOVE = new ReactionRule(
//				"Queue moves",
//				new Specie[] { new Specie(WAITING_CLERKS), new Specie(WAITING_CUSTOMERS) },
//				new Specie[] { new Specie(SERVING_CLERKS), new Specie(SERVED_CUSTOMERS) },
//				//s -> s.getOccupancy(WAITING_CUSTOMERS)*LAMBDA_ARRIVAL*(s.getOccupancy())
//				s -> {
//					if(s.getOccupancy(WAITING_CLERKS) <= 0 && s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS) < shop_capacity)
//						return (s.getOccupancy(WAITING_CLERKS))*s.getOccupancy(WAITING_CUSTOMERS)*lambda_arrival;
//					else
//						return 0.0;
//				}
//			);
//		
//		
//		PopulationRule rule_ARR = new ReactionRule(
//				"Customer arrives at the shop and he puts himself into queue",
//				new Specie[] {  },
//				new Specie[] { new Specie(WAITING_CUSTOMERS) },
//				//s -> s.getOccupancy(WAITING_CUSTOMERS)*LAMBDA_ARRIVAL*(s.getOccupancy())
//				s -> {
//					if(s.getOccupancy(WAITING_CLERKS) <= 0 && s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS) < shop_capacity)
//						return s.getOccupancy(WAITING_CUSTOMERS)*lambda_arrival;
//					else
//						return 0.0;
//				}
//			);
//		
//		PopulationRule rule_WAIT_OUT = new ReactionRule(
//				"Customer arrives at the shop and waits outside",
//				new Specie[] {  },
//				new Specie[] { new Specie(WAITING_CUSTOMERS_OUTSIDE) },
//				//s -> s.getOccupancy(WAITING_CUSTOMERS)*LAMBDA_ARRIVAL*(s.getOccupancy())
//				s -> {
//					if(s.getOccupancy(WAITING_CUSTOMERS) + s.getOccupancy(SERVED_CUSTOMERS) >= shop_capacity)
//						return s.getOccupancy(WAITING_CUSTOMERS_OUTSIDE)*lambda_arrival;
//					else
//						return 0.0;
//				}
//			);
//	
//		PopulationRule rule_SERV = new ReactionRule(
//				"Clerk served a customer and the customer exits",
//				new Specie[] { new Specie(SERVED_CUSTOMERS), new Specie(SERVING_CLERKS) },
//				new Specie[] { new Specie(WAITING_CLERKS) },
//				//s -> s.getOccupancy()*LAMBDA_SERVED*(s.getOccupancy())
//				s -> {//Everytime a customer enters the shop, if there is a waiting clerk, the customer is served
//					
//					return s.getOccupancy(SERVED_CUSTOMERS)*s.getOccupancy(SERVING_CLERKS)*lambda_served;
//				}
//			);
//		
//		PopulationModel f = new PopulationModel();
//		f.addState("init", this.initialState());
//		f.addRule(rule_ARR);
//		f.addRule(rule_WAIT_OUT);
//		f.addRule(rule_MOVE);
//		f.addRule(rule_SERV);
//		
//		return f;
//	}
//	
//	public SamplingFunction<PopulationState> setSamplings() {
//		
//		StatisticSampling<PopulationState> clerkUtilisationSamp = StatisticSampling.measure("Clerks utilisation",
//				SAMPLINGS, DEADLINE, s -> s.getOccupancy(SERVING_CLERKS)/this.shop_clerks);
//		StatisticSampling<PopulationState> customerServedSamp = StatisticSampling.measure("#CustomerServed",
//				SAMPLINGS, DEADLINE, s -> s.getOccupancy(SERVED_CUSTOMERS));
//		StatisticSampling<PopulationState> customerWaitingSamp = StatisticSampling.measure("#CustomerWaiting",
//				SAMPLINGS, DEADLINE, s -> s.getOccupancy(WAITING_CUSTOMERS));
//		StatisticSampling<PopulationState> customerCantEnterSamp = StatisticSampling.measure("#CustomerCantEnter",
//				SAMPLINGS, DEADLINE, s -> s.getOccupancy(WAITING_CUSTOMERS_OUTSIDE));
//		
//		SamplingFunction<PopulationState> sf = new SamplingCollection<>(
//				clerkUtilisationSamp,
//				customerServedSamp,
//				customerWaitingSamp,
//				customerCantEnterSamp
//			);
//		
//		samplings.put(CLERK_UTILISATION, clerkUtilisationSamp);
//		samplings.put(CUSTOMER_SERVED, customerServedSamp);
//		samplings.put(CUSTOMER_WAITING, customerWaitingSamp);
//		samplings.put(CUSTOMER_ENTER, customerCantEnterSamp);
//		
//		return sf;
//	}
//	
//	public void collectAnalysis() throws InterruptedException, FileNotFoundException {
//		SimulationEnvironment sim = new SimulationEnvironment( ThreadSimulationManager.getFixedThreadSimulationManagerFactory(TASKS) );
//		//Start the simulation
//		sim.simulate(new DefaultRandomGenerator(),f,initialState(),sf,REPLICA,DEADLINE);
//		
//		String path = "data/Data_"+this.shop_capacity+"_"+this.shop_clerks+"_"+this.lambda_arrival+"_"+this.lambda_served;
//		//System.out.println(clerkUtilisationSamp.getSimulationTimeSeries(REPLICA).getFirst().getSize());
//		new File(path).mkdirs();
//		samplings.get(CLERK_UTILISATION).printTimeSeries(new PrintStream(path+"/ShopScenarionPM_"+REPLICA+"_"+N+"_ClerkUtilisation_.data"),';');
//		samplings.get(CUSTOMER_SERVED).printTimeSeries(new PrintStream(path+"/ShopScenarionPM_"+REPLICA+"_"+N+"_CustomerServed_.data"),';');
//		samplings.get(CUSTOMER_WAITING).printTimeSeries(new PrintStream(path+"/ShopScenarionPM_"+REPLICA+"_"+N+"_CustomerWaiting_.data"),';');
//		samplings.get(CUSTOMER_ENTER).printTimeSeries(new PrintStream(path+"/ShopScenarionPM_"+REPLICA+"_"+N+"_CustomerWandering_.data"),';');
//	}
//	
//	
//}
