import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

public class Chromosome {
    private ArrayList<Integer> genes = new ArrayList<>();
    private float fitness = 0;


    public Chromosome() {
    }

    public Chromosome(ArrayList<Integer> genes) {
        this.genes = genes;
        fitnessEvaluation();
    }

    public void initialize(){
        int size = WTA.weapons.size() * WTA.nTargets;
        for (int i = 0; i < size ; i++) {
            int randValue = ThreadLocalRandom.current().nextInt(0, 2);
            genes.add(randValue);
        }
        handleInfeasiblity();
        fitnessEvaluation();
    }

    public void handleInfeasiblity() {
        for (int i = 0; i < genes.size() ; i += WTA.nTargets) {
            int sum = 0;
            for (int j = i; j < (i + WTA.nTargets); j++)
                sum += genes.get(j);
            if (sum != 1)
            {
                int randIndex = ThreadLocalRandom.current().nextInt(i, i+ WTA.nTargets);
                for (int j = i; j < (i + WTA.nTargets); j++)
                    if(j == randIndex)
                        genes.set(j,1);
                    else
                        genes.set(j,0);
            }
        }
    }

    public void fitnessEvaluation(){
        float totalThreat = 0;
        for (int j = 0; j < WTA.nTargets; j++) {
            float survivalProb = 1;
            for (int k = j; k < genes.size(); k += WTA.nTargets) {
                if (genes.get(k) == 1) {
                    String weapon = WTA.weapons.get(k / WTA.nTargets);
                    int typeIndex = WTA.types.indexOf(weapon.substring(0, weapon.indexOf(" ")));
                    survivalProb *= WTA.targetSurvivalProb[typeIndex][j];
                }
            }
            float expectedThreat = survivalProb * WTA.threatCoefficients.get(j);
            totalThreat += expectedThreat;
        }
        fitness = totalThreat;

    }

    public static Comparator<Chromosome> byFitness = new Comparator<Chromosome>() {
        @Override
        public int compare(Chromosome o1, Chromosome o2) {
            if (o1.fitness > o2.fitness) {
                return 1;
            }
            if (o1.fitness < o2.fitness) {
                return -1;
            }
            return 0;
        }
    };

    public ArrayList<Integer> getGenes() {
        return genes;
    }

    public float getFitness() {
        return fitness;
    }

}
