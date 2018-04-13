package org.insa.algo.shortestpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.insa.algo.ArcInspector;
import org.insa.algo.AbstractInputData.Mode;
import org.insa.graph.*;
import org.insa.graph.RoadInformation.RoadType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class ShortestPathAlgorithmTest {

    public abstract ShortestPathAlgorithm createAlgorithm(ShortestPathData data);

    private class AllowAllInspector implements ArcInspector {
        @Override
        public boolean isAllowed(Arc arc) { return true; }
        @Override
        public double getCost(Arc arc) { return arc.getLength(); }
        @Override
        public int getMaximumSpeed() { return GraphStatistics.NO_MAXIMUM_SPEED; }
        @Override
        public Mode getMode() { return Mode.LENGTH; }
    }

    private ShortestPathData[][] datas;

    @Before
    public void setUp() throws Exception {

        //Construction du graphe de test
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

        Graph graph = new Graph("ID", "", Arrays.asList(nodes), null);

        datas = new ShortestPathData[6][6];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                datas[i][j] = new ShortestPathData( graph, nodes[i], nodes[j], new AllowAllInspector());
            }
        }
    }

    @Test
    public void testRun() {

        ShortestPathSolution[][] oracleSoluces = new ShortestPathSolution[6][6];
        ShortestPathSolution[][] algorithmSoluces = new ShortestPathSolution[6][6];

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                ShortestPathAlgorithm oracle = new BellmanFordAlgorithm(datas[i][j]);
                oracleSoluces[i][j] = oracle.run();

                ShortestPathAlgorithm algorithm = createAlgorithm(datas[i][j]);
                algorithmSoluces[i][j] = algorithm.run();
            }
        }

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {

                ShortestPathSolution oracleSoluce = oracleSoluces[i][j];
                ShortestPathSolution algorithmSoluce = algorithmSoluces[i][j];

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
        }
    }

    @After
    public void tearDown() throws Exception {
    }
}