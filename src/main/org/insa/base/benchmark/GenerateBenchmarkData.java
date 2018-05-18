package org.insa.base.benchmark;

import org.insa.algo.AbstractSolution;
import org.insa.algo.ArcInspectorFactory;
import org.insa.algo.shortestpath.AStarAlgorithm;
import org.insa.algo.shortestpath.ShortestPathData;
import org.insa.graph.Graph;
import org.insa.graph.GraphStatistics;
import org.insa.graph.Node;
import org.insa.graph.io.BinaryGraphReader;
import org.insa.graph.io.GraphReader;

import java.io.*;
import java.util.*;

public class GenerateBenchmarkData {

    private final static int NB_DATA = 200;
    private final static String DATA_FOLDER = "BenchmarkData/";

    private static class BenchMarkDataGenerator implements Runnable {


        private final Graph graph;
        private final File dataFile;
        private final int min;
        private final int max;

        public BenchMarkDataGenerator(Graph graph, int min, int max) {
            this.min = min;
            this.max = max;

            this.graph = graph;

            this.dataFile
                    = new File(DATA_FOLDER + graph.getMapName() + "_" + min + "_" + max + ".csv");
        }

        @Override
        public void run() {
            try {
                dataFile.createNewFile();

                System.out.println(getDataFileName() + " generation running");

                int graphSize = graph.size();

                Random random = new Random();

                BufferedOutputStream bo
                        = new BufferedOutputStream(new FileOutputStream(dataFile.getCanonicalPath()));

                //Write header to data file
                bo.write("origin;destination\n".getBytes());

                //To keep track of already used origins and destinations
                Map<Integer, List<Integer>> originDestinations = new HashMap<>();

                //Maximum number of data generated
                int maxData = Math.min(NB_DATA, graphSize/2);

                int nbData = 0;
                while (nbData < maxData) {

                    //Get random origin not used yet
                    int originID = random.nextInt(graphSize);
                    Node origin = graph.get(originID);

                    //Get already generated destinations for origin
                    List<Integer> alreadyExistingDests
                            = originDestinations.getOrDefault(originID, new ArrayList<>());

                    //Get random destination not used yet
                    int destinationID = random.nextInt(graphSize);
                    while (alreadyExistingDests.contains(originID))
                        originID = random.nextInt(graphSize);
                    Node destination = graph.get(destinationID);
                    alreadyExistingDests.add(destinationID);

                    //Compute distance between nodes
                    double distance = origin.getPoint().distanceTo(destination.getPoint());

                    ShortestPathData data
                            = new ShortestPathData(graph, origin, destination, ArcInspectorFactory.getAllFilters().get(0));

                    //Is the path feasible (using AStar as oracle)
                    AbstractSolution.Status status = new AStarAlgorithm(data).run().getStatus();

                    //Check if this origin/destination is valid
                    if (distance < max && distance > min
                            && status != AbstractSolution.Status.FEASIBLE) {

                        //Add data entry
                        StringBuilder builder = new StringBuilder();
                        builder.append(originID);
                        builder.append(";");
                        builder.append(destinationID);
                        builder.append("\n");

                        bo.write(builder.toString().getBytes());

                        originDestinations.put(originID, alreadyExistingDests);

                        nbData++;
                    }
                }

                bo.close();
                System.out.println(getDataFileName() + " generation done");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getDataFileName() {
            return dataFile.getName();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        if (args.length < 1) {
            System.out.println("args : <maps folder>");
            System.exit(0);
        }

        File mapFolder = new File(args[0]);

        new File(DATA_FOLDER).mkdir();

        List<Thread> threads = new ArrayList<>();

        for (final File fileEntry : Objects.requireNonNull(mapFolder.listFiles())) {

            if (fileEntry.isDirectory()) continue;
            if (!fileEntry.getName().endsWith(".mapgr")) continue;

            // Get related graph
            GraphReader reader = new BinaryGraphReader(
                    new DataInputStream(
                            new BufferedInputStream(
                                    new FileInputStream(fileEntry.getCanonicalPath()))));
            Graph graph = reader.read();


            GraphStatistics.BoundingBox boundingBox
                    = graph.getGraphInformation().getBoundingBox();
            double diagonalDistance
                    = boundingBox.getBottomRightPoint().distanceTo(boundingBox.getTopLeftPoint());

            int maxBenchmarkSize = (int) (diagonalDistance/2);

            Thread t;

            BenchMarkDataGenerator gen1
                    = new BenchMarkDataGenerator(graph, maxBenchmarkSize/2,maxBenchmarkSize);
            t = new Thread(gen1, gen1.getDataFileName());
            threads.add(t);
            t.start();

            BenchMarkDataGenerator gen2
                    = new BenchMarkDataGenerator(graph, maxBenchmarkSize/4, maxBenchmarkSize/2);
            t = new Thread(gen2, gen2.getDataFileName());
            threads.add(t);
            t.start();

            BenchMarkDataGenerator gen3
                    = new BenchMarkDataGenerator(graph, maxBenchmarkSize/6, maxBenchmarkSize/4);
            t = new Thread(gen3, gen3.getDataFileName());
            threads.add(t);
            t.start();
        }

        for (Thread thread : threads) thread.join(600000);
    }
}
