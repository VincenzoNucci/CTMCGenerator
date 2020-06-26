package homework2;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;

import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

public class ShopScenarioModel1 {
	
	public final static int SAMPLINGS = 1440;
	public final static double DEADLINE = 1440;
	private static final int REPLICA = 10000;
	
	public static void main(String[] argv) throws FileNotFoundException, InterruptedException, UnknownHostException {
		ShopScenarioModelDefinition1 def = new ShopScenarioModelDefinition1();
		SimulationEnvironment simulator = new SimulationEnvironment();
		SamplingCollection<PopulationState> collection = new SamplingCollection<>();
		collection.add(StatisticSampling.measure("Utilisation",SAMPLINGS,DEADLINE,ShopScenarioModelDefinition1::utilisationOfK));
		collection.add(StatisticSampling.measure("Waiting",SAMPLINGS,DEADLINE,ShopScenarioModelDefinition1::waitingOfC));
		collection.add(StatisticSampling.measure("Served",SAMPLINGS,DEADLINE,ShopScenarioModelDefinition1::servedOfC));
		collection.add(StatisticSampling.measure("Outside",SAMPLINGS,DEADLINE,ShopScenarioModelDefinition1::outsideOfC));
		simulator.simulate(def.createModel(),def.state(),collection,REPLICA,DEADLINE);
		collection.printTimeSeries("data","ShopScenario_",".data");
	}

}
