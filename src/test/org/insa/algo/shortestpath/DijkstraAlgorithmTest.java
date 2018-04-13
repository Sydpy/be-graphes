package org.insa.algo.shortestpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.insa.algo.ArcInspector;
import org.insa.algo.AbstractInputData.Mode;
import org.insa.graph.*;
import org.insa.graph.RoadInformation.RoadType;
import org.insa.graph.io.BinaryGraphReader;
import org.insa.graph.io.GraphReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.io.*;
import java.util.*;


@RunWith(Parameterized.class)
public class DijkstraAlgorithmTest {

    public static class AllowAllInspector implements ArcInspector {
        @Override
        public boolean isAllowed(Arc arc) { return true; }
        @Override
        public double getCost(Arc arc) { return arc.getLength(); }
        @Override
        public int getMaximumSpeed() { return GraphStatistics.NO_MAXIMUM_SPEED; }
        @Override
        public Mode getMode() { return Mode.LENGTH; }
    }

    @Parameters
    public static Collection<Object> data() throws IOException {
        Collection<Object> data = new ArrayList<>();

        //Construction des ShortestPathData d'un graphe simple
        Node[] nodes = new Node[6];
        for (int i = 0; i < 6; i++) {
            nodes[i] = new Node(i, null);
        }

        Node.linkNodes(
                nodes[0], nodes[1], 7,
                new RoadInformation(RoadType.UNCLASSIFIED, null, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[0], nodes[2], 8,
                new RoadInformation(RoadType.UNCLASSIFIED, null, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[2], nodes[0], 7,
                new RoadInformation(RoadType.UNCLASSIFIED, null, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[1], nodes[3], 4,
                new RoadInformation(RoadType.UNCLASSIFIED, null, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[1], nodes[4], 1,
                new RoadInformation(RoadType.UNCLASSIFIED, null, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[4], nodes[3], 2,
                new RoadInformation(RoadType.UNCLASSIFIED, null, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[2], nodes[1], 2,
                new RoadInformation(RoadType.UNCLASSIFIED, null, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[1], nodes[5], 5,
                new RoadInformation(RoadType.UNCLASSIFIED, null, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[4], nodes[2], 2,
                new RoadInformation(RoadType.UNCLASSIFIED, null, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[4], nodes[5], 3,
                new RoadInformation(RoadType.UNCLASSIFIED, null, false, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[2], nodes[5], 2,
                new RoadInformation(RoadType.UNCLASSIFIED, null, false, 1, null),
                new ArrayList<>());

        Graph graph1 = new Graph("ID", "", Arrays.asList(nodes), null);

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                data.add(new ShortestPathData( graph1, nodes[i], nodes[j], new AllowAllInspector()));
            }
        }

        //Construction de 50 ShortestPathData relatifs à une map
        GraphReader reader = new BinaryGraphReader(
                new DataInputStream(new BufferedInputStream(new FileInputStream("res/Maps/bordeaux.mapgr"))));

        Graph graph2 = reader.read();
        int graph2Size = graph2.size();

        Random rand = new Random();
        for (int i = 0; i < 50; i++) {
            Node origin = graph2.get(rand.nextInt(graph2Size));
            Node dest = graph2.get(rand.nextInt(graph2Size));
            data.add(new ShortestPathData( graph2, origin, dest, new AllowAllInspector()));
        }

        return data;
    }

    @Parameter
    public ShortestPathData data;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testRun() {

        ShortestPathAlgorithm oracle = new BellmanFordAlgorithm(data);
        ShortestPathSolution oracleSoluce = oracle.run();

        ShortestPathAlgorithm algorithm = new DijkstraAlgorithm(data);
        ShortestPathSolution algorithmSoluce = algorithm.run();

        assertEquals(oracleSoluce.getStatus(), algorithmSoluce.getStatus());

        if (oracleSoluce.isFeasible() && algorithmSoluce.isFeasible()) {

            Path oraclePath = oracleSoluce.getPath();
            Path algoritmPath = algorithmSoluce.getPath();

            assertEquals(oraclePath.getArcs().size(), algoritmPath.getArcs().size());
            int pathSize = oraclePath.getArcs().size();

            for (int k = 0; k < pathSize; k++) {
                assertEquals(oraclePath.getArcs().get(k), algoritmPath.getArcs().get(k));
            }
        }
    }

    @After
    public void tearDown() throws Exception {
    }
}