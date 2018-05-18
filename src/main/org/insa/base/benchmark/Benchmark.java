package org.insa.base.benchmark;

import com.sun.org.apache.xml.internal.security.algorithms.Algorithm;
import org.insa.algo.*;
import org.insa.algo.shortestpath.ShortestPathAlgorithm;
import org.insa.algo.shortestpath.ShortestPathData;
import org.insa.algo.shortestpath.ShortestPathSolution;
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

    private static String resultsFilename = "BenchmarkResults.csv";

    private static ArcInspector inspectorLength = new ArcInspector() {
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
    };

    private static ArcInspector inspectorTime = new ArcInspector() {
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
    };

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
                = new BufferedOutputStream(new FileOutputStream(resultsFilename));

        // Write results file header
        bo.write("file;nb path".getBytes());
        for (String algorithmName : algorithmNames) {
            String algorithmLengthHeader = ";" + algorithmName + " LENGTH";
            bo.write(algorithmLengthHeader.getBytes());
        }
        for (String algorithmName : algorithmNames) {
            String algorithmTimeHeader = ";" + algorithmName + " TIME";
            bo.write(algorithmTimeHeader.getBytes());
        }
        bo.write("\n".getBytes());

        for (final File fileEntry : Objects.requireNonNull(csvFolder.listFiles())) {

            if (fileEntry.isDirectory()) continue;

            String filename = fileEntry.getName();
            if (filename.endsWith(".csv")) {

                String mapName = filename.split("_")[0];

                try {
                    // Get related graph
                    GraphReader reader = new BinaryGraphReader(
                            new DataInputStream(
                                    new BufferedInputStream(
                                            new FileInputStream("Maps/" + mapName.toLowerCase() + ".mapgr"))));
                    Graph graph = reader.read();

                    int nbEntries = 0;
                    Map<String, Duration> totalDurations = new HashMap<>();

                    //Read the csv file
                    BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                    String line;
                    br.readLine();
                    while ((line = br.readLine()) != null) {

                        //Parse entry
                        String[] splitted = line.split(";");

                        int originID = Integer.parseInt(splitted[0]);
                        int destinationID = Integer.parseInt(splitted[1]);
                        Node origin = graph.get(originID);
                        Node destination = graph.get(destinationID);

                        ShortestPathData dataLength =
                                new ShortestPathData(graph, origin, destination, inspectorLength);

                        ShortestPathData dataTime =
                                new ShortestPathData(graph, origin, destination, inspectorTime);

                        Duration totalDuration;
                        Duration duration;
                        for (String algorithmName : algorithmNames) {

                            //Algorithm for length
                            duration = doBenchmark(dataLength, algorithmName);
                            totalDuration =
                                    totalDurations.getOrDefault(algorithmName + " LENGTH", Duration.ZERO);
                            totalDuration = totalDuration.plus(duration);
                            totalDurations.put(algorithmName + "LENGTH", totalDuration);
                        }
                        for (String algorithmName : algorithmNames) {
                            //Algorithm for time
                            duration = doBenchmark(dataTime, algorithmName);
                            totalDuration =
                                    totalDurations.getOrDefault(algorithmName + " TIME", Duration.ZERO);
                            totalDuration = totalDuration.plus(duration);
                            totalDurations.put(algorithmName + "TIME", totalDuration);
                        }

                        nbEntries++;
                    }

                    StringBuilder resultEntry = new StringBuilder();
                    resultEntry.append(filename);
                    resultEntry.append(";");
                    resultEntry.append(nbEntries);
                    totalDurations.forEach((k, totalDuration) -> {
                        resultEntry.append(";");
                        resultEntry.append((float) totalDuration.getNano() / (float) 10e9);
                    });
                    resultEntry.append("\n");

                    bo.write(resultEntry.toString().getBytes());
                    bo.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        bo.close();
    }

    private static Duration doBenchmark(ShortestPathData data, String algorithmName)
            throws Exception {


        Class<? extends AbstractAlgorithm<?>> algorithmClass =
                AlgorithmFactory.getAlgorithmClass(ShortestPathAlgorithm.class, algorithmName);

        AbstractAlgorithm<?> algorithm =
                AlgorithmFactory.createAlgorithm(algorithmClass, data);

        AbstractSolution solution = algorithm.run();

        return solution.getSolvingTime();
    }
}
