package org.insa.algo.shortestpath;

import org.insa.algo.ArcInspector;
import org.insa.algo.ArcInspectorFactory;
import org.insa.graph.*;
import org.insa.graph.io.BinaryGraphReader;
import org.insa.graph.io.GraphReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public abstract class ShortestPathAlgorithmTest {

    @Parameterized.Parameters
    public static Collection<Object> data() throws IOException {
        Collection<Object> data = new ArrayList<>();

        //Construction des ShortestPathData d'un graphe simple
        Node[] nodes = new Node[6];
        for (int i = 0; i < 6; i++) {
            nodes[i] = new Node(i, null);
        }

        RoadInformation.RoadType rt = RoadInformation.RoadType.UNCLASSIFIED;
        AccessRestrictions ar = new AccessRestrictions();

        Node.linkNodes(
                nodes[0], nodes[1], 7,
                new RoadInformation(rt, ar, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[0], nodes[2], 8,
                new RoadInformation(rt, ar, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[2], nodes[0], 7,
                new RoadInformation(rt, ar, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[1], nodes[3], 4,
                new RoadInformation(rt, ar, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[1], nodes[4], 1,
                new RoadInformation(rt, ar, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[4], nodes[3], 2,
                new RoadInformation(rt, ar, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[2], nodes[1], 2,
                new RoadInformation(rt, ar, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[1], nodes[5], 5,
                new RoadInformation(rt, ar, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[4], nodes[2], 2,
                new RoadInformation(rt, ar, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[4], nodes[5], 3,
                new RoadInformation(rt, ar, true, 1, null),
                new ArrayList<>());
        Node.linkNodes(
                nodes[2], nodes[5], 2,
                new RoadInformation(rt, ar, true, 1, null),
                new ArrayList<>());

        Graph graph1 = new Graph("ID", "", Arrays.asList(nodes), null);

        for (ArcInspector inspector : ArcInspectorFactory.getAllFilters()) {
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    data.add(new ShortestPathData(graph1, nodes[i], nodes[j], inspector));
                }
            }
        }

        //Construction de ShortestPathData relatifs à une map
        GraphReader reader = new BinaryGraphReader(
                new DataInputStream(new BufferedInputStream(new FileInputStream("res/Maps/bordeaux.mapgr"))));

        Graph graph2 = reader.read();
        int graph2Size = graph2.size();

        Random rand = new Random();
        for (ArcInspector inspector : ArcInspectorFactory.getAllFilters()) {
            for (int i = 0; i < 10; i++) {
                Node origin = graph2.get(rand.nextInt(graph2Size));
                Node dest = graph2.get(rand.nextInt(graph2Size));
                data.add(new ShortestPathData(graph2, origin, dest, inspector));
            }
        }

        return data;
    }

    protected abstract ShortestPathAlgorithm createAlgorithm(ShortestPathData data);

    @Parameterized.Parameter
    public ShortestPathData data;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testRunWithOracle() {

        ShortestPathAlgorithm oracle = new BellmanFordAlgorithm(data);
        ShortestPathSolution oracleSoluce = oracle.run();

        ShortestPathAlgorithm algorithm = createAlgorithm(data);
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

    @Test
    public void testRunWithoutOracle() {
        //TODO
    }

    @After
    public void tearDown() throws Exception {
    }
}