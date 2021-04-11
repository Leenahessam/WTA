import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class WTA {

    public static ArrayList<String> types = new ArrayList<>();
    public static ArrayList<String> weapons = new ArrayList<>();
    public static int nTargets = 0;
    public static ArrayList<Integer> threatCoefficients = new ArrayList<>();
    public ArrayList<Chromosome> generation = new ArrayList<>();
    public ArrayList<Chromosome> offsprings = new ArrayList<>();
    public int populationSize = 100;
    public static float[][] targetSurvivalProb;

    public void getInput() {
        System.out.println("Enter the weapon types and the number of instances of each type: (Enter “x”  when you're done)");
        Scanner scan = new Scanner(System.in);
        String weapon = scan.next();
        while (!weapon.equalsIgnoreCase("x")) {
            int instances = scan.nextInt();
            types.add(weapon);
            for (int i = 0; i < instances; i++)
                weapons.add(weapon + " #" + (i+1));
            weapon = scan.next();
        }

        System.out.println("\nEnter the number of targets:");
        nTargets = scan.nextInt();

        System.out.println("\nEnter the threat coefficient of each target");
        for (int i = 0; i < nTargets; i++) {
            int threatCoefficient = scan.nextInt();
            threatCoefficients.add(threatCoefficient);
        }

        System.out.println("\nEnter the weapons’ success probabilities matrix:");
        targetSurvivalProb = new float[types.size()][nTargets];
        for (int i = 0; i < types.size(); i++) {
            for (int j = 0; j < nTargets; j++) {
                float successProb = scan.nextFloat();
                targetSurvivalProb[i][j] = 1 - successProb;
            }
        }

        System.out.println("\nPlease wait while running the GA...\n");
    }

    public boolean contain(Chromosome c){
        for (Chromosome chromosome : generation) {
            if(c.getGenes() == chromosome.getGenes())
                return true;
        }
        return false;
    }

    public void initPopulation() {
        for (int i = 0; i < populationSize ; i++) {
            Chromosome chromosome = new Chromosome();
            chromosome.initialize();
            if(contain(chromosome))
                i--;
            else
                generation.add(chromosome);
        }
    }

    public ArrayList<Integer> selection (int k, int nParents) {
        ArrayList<Integer>chromosomeIndex = new ArrayList<>();
        ArrayList<Chromosome>parents = new ArrayList<>();
        for (int i = 0; i < nParents ; i++) {
            ArrayList<Integer>fitIndices = new ArrayList<>();
            for (int j = 0; j < k ; j++) {
                int fitIndex = ThreadLocalRandom.current().nextInt(0,generation.size());
                if(!(fitIndices.contains(fitIndex)) && !(chromosomeIndex.contains(fitIndex))&& !(parents.contains(generation.get(fitIndex)))) {
                    /*for (int x = 0; x < fitIndices.size(); x++) {
                        if(generation.get(x).getGenes() == generation.get(fitIndex).getGenes())
                            j--;
                    }*/

                    fitIndices.add(fitIndex);
                } else
                    j--;
            }
            ArrayList<Float>values = new ArrayList<>();
            for (int j = 0; j < fitIndices.size() ; j++)
                values.add(generation.get(fitIndices.get(j)).getFitness());
            float min = Collections.min(values);
            chromosomeIndex.add(fitIndices.get(values.indexOf(min)));
            parents.add(generation.get(chromosomeIndex.get(chromosomeIndex.size()-1)));
        }
        return chromosomeIndex;
    }

    public void crossover(int parent1, int parent2){
        float Pc = (float) 0.9;
        double rc = Math.random();
        if(rc <= Pc){
            int crossoverPoint;
            do {
                crossoverPoint = ThreadLocalRandom.current().nextInt(1, generation.get(0).getGenes().size());
            }while ((crossoverPoint % nTargets) != 0);
            ArrayList<Integer> genes1 = new ArrayList<>();
            ArrayList<Integer> genes2 = new ArrayList<>();
            genes1.addAll(generation.get(parent1).getGenes().subList(0, crossoverPoint));
            genes1.addAll(generation.get(parent2).getGenes().subList(crossoverPoint, generation.get(0).getGenes().size()));
            genes2.addAll(generation.get(parent2).getGenes().subList(0, crossoverPoint));
            genes2.addAll(generation.get(parent1).getGenes().subList(crossoverPoint, generation.get(0).getGenes().size()));
            offsprings.add(new Chromosome(genes1));
            offsprings.add(new Chromosome(genes2));
        }
        else{
            offsprings.add(generation.get(parent1));
            offsprings.add(generation.get(parent2));
        }
    }

    public void mutation(){
        float Pm = (float) 0.1;
        for (int i = offsprings.size()-2; i < offsprings.size(); i++) {
            for (int j = 0; j < offsprings.get(i).getGenes().size(); j++) {
                double rm = Math.random();
                if (rm <= Pm){
                    int value = offsprings.get(i).getGenes().get(j) ^ 1;
                    offsprings.get(i).getGenes().set(j, value);
                }
            }
        }
    }

    public void elitistReplacementStrategy(){
        ArrayList<Chromosome> newGeneration = new ArrayList<>();
        newGeneration.addAll(offsprings);
        int difference = populationSize - offsprings.size();
        generation.sort(Chromosome.byFitness);
        newGeneration.addAll(generation.subList(0, difference));
        generation = newGeneration;
    }

    public void getFinalSolution(){
        generation.sort(Chromosome.byFitness);
        System.out.println("\nThe final WTA solution is:");
        for (int i = 0; i < generation.get(0).getGenes().size(); i+=nTargets) {
            String weapon = weapons.get(i/nTargets);
            int target = 0;
            for (int j = i; j < (i+nTargets); j++) {
                if(generation.get(0).getGenes().get(j) == 1) {
                    target = j % nTargets;
                    break;
                }
            }
            System.out.println(weapon + " is assigned to target #" + (target+1));
        }
        System.out.println("\nThe expected total threat of the surviving targets is " + generation.get(0).getFitness());
    }

    public void runGA() {
        initPopulation();

        for (int i = 0; i < 100; i++) {
            offsprings.clear();
            while (offsprings.size() != 90){
                ArrayList<Integer> parents = selection(30, 2);
                crossover(parents.get(0), parents.get(1));
                mutation();
            }
            for (int j = 0; j < offsprings.size(); j++) {
                offsprings.get(j).handleInfeasiblity();
                offsprings.get(j).fitnessEvaluation();
            }
            elitistReplacementStrategy();
        }
        System.out.println();
        getFinalSolution();
    }

    public static void main(String[] args) {
        WTA wta = new WTA();
        wta.getInput();
        wta.runGA();
    }
}
