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

        //Building a very simple graph with 6 nodes
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

        ArcInspector insp = ArcInspectorFactory.getAllFilters().get(0);
        //For each pari of nodes, create a new test data
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (i != j)
                    data.add(new ShortestPathData(graph1, nodes[i], nodes[j], insp));
            }
        }

        //Build random ShortestPathData from a map
        GraphReader reader = new BinaryGraphReader(
                new DataInputStream(new BufferedInputStream(new FileInputStream("Maps/toulouse.mapgr"))));

        Graph graph2 = reader.read();
        int graph2Size = graph2.size();

        Random rand = new Random();
        for (ArcInspector inspector : ArcInspectorFactory.getAllFilters()) {
            for (int i = 0; i < 8; i++) {

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
    public void setUp() {
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
            Path algorithmPath = algorithmSoluce.getPath();

            //Assert that the cost of algo soluce equals the cost of oracle soluce
            switch (data.getMode()) {
                case TIME:
                    assertEquals(0, Double.compare(oraclePath.getMinimumTravelTime(), algorithmPath.getMinimumTravelTime()));
                    break;
                case LENGTH:
                    assertEquals(0, Double.compare(oraclePath.getLength(), algorithmPath.getLength()));
                    break;
            }
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

            //if algorithm soluce is feasible
            if (algorithmSoluce.isFeasible()) {

                //If the two new path are also feasible
                if(firstPathSoluce.isFeasible() && secondPathSoluce.isFeasible()) {

                    Path firstPath = firstPathSoluce.getPath();
                    Path secondPath = secondPathSoluce.getPath();

                    //Alt path is the conncatenation of firstPath and secondPath
                    //i.e. a -> c -> b
                    Path altPath = Path.concatenate(firstPath, secondPath);

                    //Soluce path is the direct path a-> b
                    Path solucePath = secondPathSoluce.getPath();

                    //Assert that the cost of the alternate path is at least as big as
                    //the cost of the direct path
                    switch (data.getMode()) {
                        case TIME:
                            assertTrue(Double.compare(altPath.getMinimumTravelTime(),
                                    solucePath.getMinimumTravelTime()) >= 0);
                            break;
                        case LENGTH:
                            assertTrue(Double.compare(altPath.getLength(),
                                    solucePath.getLength()) >= 0);
                            break;
                    }
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
    public void tearDown() {
    }
}