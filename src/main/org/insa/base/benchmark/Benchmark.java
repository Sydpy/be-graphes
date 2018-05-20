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

    private static final String[] ALGORITHMS = { "Dijkstra", "A*"};
    private static final ArcInspector[] INSPECTORS = { inspectorTime, inspectorLength};

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.out.println("args : <csv input folder>");
            System.exit(0);
        }

        //Get all algorithms names
        Set<String> algorithmNames =
                AlgorithmFactory.getAlgorithmNames(ShortestPathAlgorithm.class);

        File csvFolder = new File(args[0]);

        BufferedOutputStream bo
                = new BufferedOutputStream(new FileOutputStream(RESULTS_FILENAME));

        // Write results file header
        StringBuilder header = new StringBuilder();
        header.append("file;nb path");
        for (String algorithm : ALGORITHMS) {
            for (ArcInspector inspector : INSPECTORS) {
                header.append(";");
                header.append(algorithm);
                header.append(" ");
                header.append(inspector.toString());
            }
        }
        header.append("\n");
        bo.write(header.toString().getBytes());

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

                    Map<String, Duration> totalDurations = new HashMap<>();

                    int nbEntries = 0;

                    //Read the csv file
                    BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                    String line;
                    br.readLine();
                    while ((line = br.readLine()) != null) {

                        //Parse entry
                        String[] splitted = line.split(";");

                        int originID = Integer.parseInt(splitted[0]);
                        int destinationID = Integer.parseInt(splitted[1]);

                        Map<String, Duration> durations = doBenchmark(graph, originID, destinationID);

                        for (String algorithm : ALGORITHMS) {
                            for (ArcInspector inspector : INSPECTORS) {

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

                    StringBuilder resultEntry = new StringBuilder();
                    resultEntry.append(filename);
                    resultEntry.append(";");
                    resultEntry.append(nbEntries);

                    for (String algorithm : ALGORITHMS) {
                        for (ArcInspector inspector : INSPECTORS) {


                            String key = algorithm + " " + inspector.toString();
                            Duration duration = totalDurations.getOrDefault(key, Duration.ZERO);

                            double total = (double) duration.getSeconds() + ((double) duration.getNano() / 10e9);

                            resultEntry.append(";");
                            resultEntry.append(total);
                        }
                    }
                    resultEntry.append("\n");

                    bo.write(resultEntry.toString().getBytes());
                    bo.flush();

                    System.out.println("Done benchmarking " + filename + " (" + nbEntries + " entries).");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        bo.close();
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
