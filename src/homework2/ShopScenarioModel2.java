package homework2;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;

import homework1.ShopScenarioMarkovChain;
import quasylab.sibilla.core.models.pm.PopulationState;
import quasylab.sibilla.core.simulator.SimulationEnvironment;
import quasylab.sibilla.core.simulator.sampling.SamplingCollection;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

public class ShopScenarioModel2 {
	
	public final static int SAMPLINGS = 1440;
	public final static double DEADLINE = 1440;
	private static final int REPLICA = 10000;
	
	public static void main(String[] argv) throws FileNotFoundException, InterruptedException, UnknownHostException {
		// ShopScenarioModelDefinition2 def = new ShopScenarioModelDefinition2();
		SimulationEnvironment simulator = new SimulationEnvironment();
		SamplingCollection<PopulationState> collection = new SamplingCollection<>();
		collection.add(StatisticSampling.measure("Utilisation",SAMPLINGS,DEADLINE,ShopScenarioModelDefinition2::utilisationOfK));
		collection.add(StatisticSampling.measure("Waiting",SAMPLINGS,DEADLINE,ShopScenarioModelDefinition2::waitingOfC));
		collection.add(StatisticSampling.measure("Served",SAMPLINGS,DEADLINE,ShopScenarioModelDefinition2::servedOfC));
		collection.add(StatisticSampling.measure("Outside",SAMPLINGS,DEADLINE,ShopScenarioModelDefinition2::outsideOfC));
		for(int n: new int[] {5,10,25}) {
			for(int k: new int[] {1, 2, 5}) {
				for(double la: new double[] {10.0, 5.0, 2.0, 1.0}) {
					for(double ls: new double[] {10.0, 5.0, 2.0, 1.0}) {
						ShopScenarioModelDefinition2.N = n;
						ShopScenarioModelDefinition2.K = k;
						ShopScenarioModelDefinition2.LAMBDA_A = 1/la;
						ShopScenarioModelDefinition2.LAMBDA_S = 1/ls;
						ShopScenarioModelDefinition2 def = new ShopScenarioModelDefinition2();
						simulator.simulate(def.createModel(),def.state(),collection,REPLICA,DEADLINE);
						String folder = "data/Data-"+n+"-"+k+"-"+la+"-"+ls;
						if( new File(folder).exists() ) continue;
						new File(folder).mkdirs();
						// String path = folder + "/" + "ShopScenario_";
						collection.printTimeSeries(folder,"ShopScenario_",".data");
					}
				}
			}
		}
		//simulator.simulate(def.createModel(),def.state(),collection,REPLICA,DEADLINE);
		//collection.printTimeSeries("data","ShopScenario_",".data");
	}

}
