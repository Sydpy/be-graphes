package org.insa.base.benchmark;

import com.sun.org.apache.xml.internal.security.algorithms.Algorithm;
import org.insa.algo.*;
import org.insa.algo.shortestpath.AStarAlgorithm;
import org.insa.algo.shortestpath.DijkstraAlgorithm;
import org.insa.algo.shortestpath.ShortestPathAlgorithm;
import org.insa.algo.shortestpath.ShortestPathData;
import org.insa.graph.Arc;
import org.insa.graph.Graph;
import org.insa.graph.GraphStatistics;
import org.insa.graph.Node;
import org.insa.graph.io.BinaryGraphReader;
import org.insa.graph.io.GraphReader;

import java.io.*;
import java.time.Duration;
import java.util.*;

public class Benchmark {

    private static String RESULTS_FILENAME = "BenchmarkResults.csv";

    //Inspector for length path finding benchmark
    private static final ArcInspector inspectorLength = new ArcInspector() {
        @Override
        public boolean isAllowed(Arc arc) {
            return true;
        }

        @Override
        public double getCost(Arc arc) {
            return arc.getLength();
        }

        @Override
        public int getMaximumSpeed() {
            return GraphStatistics.NO_MAXIMUM_SPEED;
        }

        @Override
        public AbstractInputData.Mode getMode() {
            return AbstractInputData.Mode.LENGTH;
        }

        @Override
        public String toString() {
            return "LENGTH";
        }
    };

    //Inspector for time path finding benchmark
    private static final ArcInspector inspectorTime = new ArcInspector() {
        @Override
        public boolean isAllowed(Arc arc) {
            return true;
        }

        @Override
        public double getCost(Arc arc) {
            return arc.getMinimumTravelTime();
        }

        @Override
        public int getMaximumSpeed() {
            return GraphStatistics.NO_MAXIMUM_SPEED;
        }

        @Override
        public AbstractInputData.Mode getMode() {
            return AbstractInputData.Mode.TIME;
        }

        @Override
        public String toString() {
            return "TIME";
        }
    };

    //Algorithms to benchmark (must match the names given in AlgorithmFactory)
    private static final String[] ALGORITHMS = { "Dijkstra", "A*"};
    private static final ArcInspector[] INSPECTORS = { inspectorTime, inspectorLength};

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.out.println("args : <csv input folder>");
            System.exit(0);
        }

        //Folder containing the benchmark input data
        File csvFolder = new File(args[0]);

        //Create the file where results are going to be written
        BufferedOutputStream resultFile
                = new BufferedOutputStream(new FileOutputStream(RESULTS_FILENAME));

        // Write results file header
        StringBuilder header = new StringBuilder();
        header.append("file;nb path");
        for (ArcInspector inspector : INSPECTORS) {
            for (String algorithm : ALGORITHMS) {
                header.append(";");
                header.append(algorithm);
                header.append(" ");
                header.append(inspector.toString());
            }
        }
        header.append("\n");
        resultFile.write(header.toString().getBytes());

        //For each csv input data file
        for (final File fileEntry : Objects.requireNonNull(csvFolder.listFiles())) {

            if (fileEntry.isDirectory()) continue;

            String filename = fileEntry.getName();
            if (filename.endsWith(".csv")) {

                String mapName = filename.split("_")[0];

                System.out.println("Benchmarking " + filename);

                try {
                    // Get related graph
                    GraphReader reader = new BinaryGraphReader(
                            new DataInputStream(
                                    new BufferedInputStream(
                                            new FileInputStream("Maps/" + mapName.toLowerCase() + ".mapgr"))));
                    Graph graph = reader.read();

                    //Associate each algorithm with the total duration it took
                    //to process each entry
                    Map<String, Duration> totalDurations = new HashMap<>();

                    int nbEntries = 0;

                    //Read the csv file
                    BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                    String line;
                    br.readLine();
                    //For each entry in file
                    while ((line = br.readLine()) != null) {

                        //Parse entry
                        String[] splitted = line.split(";");

                        int originID = Integer.parseInt(splitted[0]);
                        int destinationID = Integer.parseInt(splitted[1]);

                        //Associate each algorithm with the duration it took
                        //to process this entry
                        Map<String, Duration> durations = doBenchmark(graph, originID, destinationID);

                        //Add the duration for this entry to the total duration
                        for (ArcInspector inspector : INSPECTORS) {
                            for (String algorithm : ALGORITHMS) {

                                String key = algorithm + " " + inspector.toString();

                                Duration totalDuration
                                        = totalDurations.getOrDefault(key, Duration.ZERO);

                                totalDuration = totalDuration.plus(durations.get(key));

                                totalDurations.put(key, totalDuration);
                            }
                        }

                        nbEntries++;
                        if (nbEntries % 5 == 0) System.out.println(nbEntries + " entries processed.");
                    }

                    //After the file as been processed, we can write the results
                    //in the results file
                    StringBuilder resultEntry = new StringBuilder();
                    resultEntry.append(filename);
                    resultEntry.append(";");
                    resultEntry.append(nbEntries);

                    for (ArcInspector inspector : INSPECTORS) {
                        for (String algorithm : ALGORITHMS) {

                            String key = algorithm + " " + inspector.toString();
                            Duration duration = totalDurations.getOrDefault(key, Duration.ZERO);

                            double total = (double) duration.getSeconds() + ((double) duration.getNano() / 10e9);

                            resultEntry.append(";");
                            resultEntry.append(total);
                        }
                    }
                    resultEntry.append("\n");

                    resultFile.write(resultEntry.toString().getBytes());
                    resultFile.flush();

                    System.out.println("Done benchmarking " + filename + " (" + nbEntries + " entries).");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        resultFile.close();
    }

    private static Map<String, Duration> doBenchmark(Graph graph, int originID, int destinationID) throws Exception {

        Map<String, Duration> results = new HashMap<>();

        Node origin = graph.get(originID);
        Node destination = graph.get(destinationID);

        for (String algorithm : ALGORITHMS) {
            for (ArcInspector inspector : INSPECTORS) {

                ShortestPathData data = new ShortestPathData(graph, origin, destination, inspector);
                Class<? extends AbstractAlgorithm<?>> algorithmClass
                        = AlgorithmFactory.getAlgorithmClass(ShortestPathAlgorithm.class, algorithm);

                AbstractSolution solution = AlgorithmFactory.createAlgorithm(algorithmClass, data).run();

                results.put(algorithm + " " + inspector.toString(), solution.getSolvingTime());
            }
        }

        return results;
    }
}
