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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
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
                    if (i != j)
                        data.add(new ShortestPathData(graph1, nodes[i], nodes[j], inspector));
                }
            }
        }

        //Build random ShortestPathData from a map
        GraphReader reader = new BinaryGraphReader(
                new DataInputStream(new BufferedInputStream(new FileInputStream("res/Maps/bordeaux.mapgr"))));

        Graph graph2 = reader.read();
        int graph2Size = graph2.size();

        Random rand = new Random();
        for (ArcInspector inspector : ArcInspectorFactory.getAllFilters()) {
            for (int i = 0; i < 5; i++) {

                //Get two random nodes that must be different
                Node origin = graph2.get(rand.nextInt(graph2Size));
                Node dest = graph2.get(rand.nextInt(graph2Size));
                while (dest.equals(origin))
                    dest = graph2.get(rand.nextInt(graph2Size));

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

        //Get oracle solution
        ShortestPathAlgorithm oracle = new BellmanFordAlgorithm(data);
        ShortestPathSolution oracleSoluce = oracle.run();

        //Get algorithm solution
        ShortestPathAlgorithm algorithm = createAlgorithm(data);
        ShortestPathSolution algorithmSoluce = algorithm.run();

        //If oracle is feasible, algo must be feasible
        //If oracle is not feasible, algo must be not feasible
        assertEquals(oracleSoluce.isFeasible(), algorithmSoluce.isFeasible());

        if (oracleSoluce.isFeasible() && algorithmSoluce.isFeasible()) {

            Path oraclePath = oracleSoluce.getPath();
            Path algoritmPath = algorithmSoluce.getPath();

            //Their path should be the same
            assertArrayEquals(oraclePath.getArcs().toArray(), algoritmPath.getArcs().toArray());
        }
    }

    @Test
    public void testRunWithoutOracle() {
        //Get algo solution
        ShortestPathAlgorithm algorithm = createAlgorithm(data);
        ShortestPathSolution algorithmSoluce = algorithm.run();

        //Test : cost(a->c) <= cost(a->b) + cost(b->c) for every b

        //Get data parameters
        Node origin = data.getOrigin();
        Node destination = data.getDestination();
        ArcInspector inspector = data.getArcInspector();

        Graph graph = data.getGraph();
        int graphSize = graph.size();

        Random rand = new Random();
        for (int i = 0; i < 5; i++) {

            //Get a random third node different from origin and destination
            Node thirdNode = data.getGraph().get(rand.nextInt(graphSize));
            while (thirdNode.equals(origin) || thirdNode.equals(destination))
                thirdNode = data.getGraph().get(rand.nextInt(graphSize));

            //Get solution for path from origin to thirdNode
            ShortestPathData firstPathData = new ShortestPathData(graph, origin, thirdNode, inspector);
            ShortestPathAlgorithm firstPathAlgo = createAlgorithm(firstPathData);
            ShortestPathSolution firstPathSoluce = firstPathAlgo.run();

            //Get solution for path from thirdNode to destination
            ShortestPathData secondPathData = new ShortestPathData(graph, thirdNode, destination, inspector);
            ShortestPathAlgorithm secondPathAlgo = createAlgorithm(secondPathData);
            ShortestPathSolution secondPathSoluce = secondPathAlgo.run();

            if (algorithmSoluce.isFeasible()) {

                if(firstPathSoluce.isFeasible() && secondPathSoluce.isFeasible()) {

                    //Compute costs of each path
                    //We use BigDecimal to prevent arithmetic error due to the addition of doubles
                    BigDecimal soluceCost = BigDecimal.ZERO;
                    for (Arc a : algorithmSoluce.getPath().getArcs())
                        soluceCost = soluceCost.add(BigDecimal.valueOf(inspector.getCost(a)));

                    BigDecimal firstPathCost = BigDecimal.ZERO;
                    for (Arc a : firstPathSoluce.getPath().getArcs())
                        firstPathCost = firstPathCost.add(BigDecimal.valueOf(inspector.getCost(a)));

                    BigDecimal secondPathCost = BigDecimal.ZERO;
                    for (Arc a : secondPathSoluce.getPath().getArcs())
                        secondPathCost = secondPathCost.add(BigDecimal.valueOf(inspector.getCost(a)));

                    BigDecimal altPathCost = firstPathCost.add(secondPathCost);

                    //altPathCost should be greater than or equal to soluce cost
                    assertTrue(altPathCost.compareTo(soluceCost) >= 0);
                }

            } else {
                //If the solution is not feasible, then it means thatat least one of this paths
                //is not feasible, else it would mean than the solution is feasible
                assertTrue(!firstPathSoluce.isFeasible()
                        || !secondPathSoluce.isFeasible());
            }
        }
    }

    @After
    public void tearDown() throws Exception {
    }
}