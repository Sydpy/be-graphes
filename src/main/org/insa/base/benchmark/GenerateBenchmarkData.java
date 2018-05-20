package org.insa.base.benchmark;

import org.insa.algo.ArcInspectorFactory;
import org.insa.algo.shortestpath.DijkstraAlgorithm;
import org.insa.algo.shortestpath.ShortestPathData;
import org.insa.algo.utils.Label;
import org.insa.graph.Graph;
import org.insa.graph.GraphStatistics;
import org.insa.graph.Node;
import org.insa.graph.io.BinaryGraphReader;
import org.insa.graph.io.GraphReader;

import java.io.*;
import java.util.*;

public class GenerateBenchmarkData {

    private final static int NB_DATA = 400;
    private final static String DATA_FOLDER = "BenchmarkData/";

    private static class BenchMarkDataGenerator implements Runnable {


        private final File graphFile;

        public BenchMarkDataGenerator(File graphFile) {

            this.graphFile = graphFile;
        }

        @Override
        public void run() {
            try {

                Random random = new Random();

                // Get related graph
                GraphReader reader = new BinaryGraphReader(
                        new DataInputStream(
                                new BufferedInputStream(
                                        new FileInputStream(this.graphFile.getCanonicalPath()))));
                Graph graph = reader.read();
                int graphSize = graph.size();

                //Get graph name
                String graphName = graphFile.getName().substring(0, graphFile.getName().lastIndexOf("."));

                GraphStatistics.BoundingBox boundingBox
                        = graph.getGraphInformation().getBoundingBox();
                double diagonalDistance
                        = boundingBox.getBottomRightPoint().distanceTo(boundingBox.getTopLeftPoint());

                double maxBenchmarkedDistance = diagonalDistance/2;

                //Create an output for each interval
                BufferedOutputStream[] bos = new BufferedOutputStream[3];
                for (int i = 0; i < bos.length; i++) {

                    StringBuilder builder = new StringBuilder();
                    builder.append(DATA_FOLDER);
                    builder.append(graphName);
                    builder.append("_");
                    builder.append((i + 1) * (int) maxBenchmarkedDistance / 4);
                    builder.append("_");
                    builder.append((i + 2) * (int) maxBenchmarkedDistance / 4);
                    builder.append(".csv");

                    bos[i] = new BufferedOutputStream(new FileOutputStream(builder.toString()));
                    bos[i].write("origin;destination\n".getBytes());
                }

                List<Integer> alreadyPickedOrigins = new ArrayList<>();

                //Maximum number of data generated
                int maxData = Math.min(NB_DATA, graphSize / 2);

                int nbData = 0;
                while (nbData < maxData) {

                    //Get random origin not used yet
                    int originID = random.nextInt(graphSize);
                    while (alreadyPickedOrigins.contains(originID))
                        originID = random.nextInt(graphSize);
                    alreadyPickedOrigins.add(originID);

                    Node origin = graph.get(originID);

                    //Construct a dijkstra algorithm with no destination (run potentially through all nodes)
                    ShortestPathData data
                            = new ShortestPathData(graph, origin, null, ArcInspectorFactory.getAllFilters().get(0));
                    DijkstraAlgorithm algorithm = new DijkstraAlgorithm(data);

                    //While we don't have reached all nodes below maxBenchmarkedDistance, we step
                    Node step;
                    do {
                        step = algorithm.step();
                    } while (step != null && algorithm.getLabels()[step.getId()].getCost() <= maxBenchmarkedDistance);

                    //For each intervals of length
                    for (int i = 0; i < bos.length; i++) {
                        double minCost = (i+1)*maxBenchmarkedDistance/4;
                        double maxCost = (i+2)*maxBenchmarkedDistance/4;

                        //Keep only labels inside this interval
                        List<Label> labels = new ArrayList<>();
                        labels.addAll(Arrays.asList(algorithm.getLabels()));
                        labels.removeIf(l -> l == null || l.getCost() < minCost || l.getCost() > maxCost);

                        if (labels.isEmpty()) continue;

                        //Get a random label inside this interval
                        Label randomLabel = labels.get(random.nextInt(labels.size()));
                        labels.remove(randomLabel);

                        int destinationID = randomLabel.getNode().getId();

                        //Add data entry to corresponding output
                        StringBuilder builder = new StringBuilder();
                        builder.append(originID);
                        builder.append(";");
                        builder.append(destinationID);
                        builder.append("\n");

                        bos[i].write(builder.toString().getBytes());
                    }
                    nbData++;
                }

                for (BufferedOutputStream bo : bos) {
                    bo.close();
                }

                System.out.println("Benchmark data generation from " + graphFile.getName() + " done ");
            } catch (IOException e) {
                e.printStackTrace();
            }
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

            System.out.println("Reading " + fileEntry.getName());

            Thread t = new Thread(new BenchMarkDataGenerator(fileEntry));
            threads.add(t);
            t.start();
        }

        for (Thread thread : threads) thread.join();

        System.out.println("Generation done.");
    }
}
