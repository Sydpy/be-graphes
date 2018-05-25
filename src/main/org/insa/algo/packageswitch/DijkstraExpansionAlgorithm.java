package org.insa.algo.packageswitch;

import org.insa.algo.AbstractSolution;
import org.insa.algo.shortestpath.DijkstraAlgorithm;
import org.insa.algo.shortestpath.ShortestPathData;
import org.insa.graph.*;

import java.util.*;

public class DijkstraExpansionAlgorithm extends PackageSwitchAlgorithm {

    private DijkstraAlgorithm dijkstraO1, dijkstraO2, dijkstraD1, dijkstraD2;

    /**
     * Create a new PackageSwitchAlgorithm with the given data.
     *
     * @param data
     */
    protected DijkstraExpansionAlgorithm(PackageSwitchData data) {
        super(data);

        //Retrieve infos from data
        Graph graph = data.getGraph();
        Graph graphTranspose = graph.transpose();

        Node origin1 = getInputData().getOrigin1();
        Node origin2 = getInputData().getOrigin2();
        Node destination1 = getInputData().getDestination1();
        Node destination2 = getInputData().getDestination2();

        //Construct the algorithms
        ShortestPathData dataO1 = new ShortestPathData(graph,
                origin1,
                null,
                data.getArcInspector());
        this.dijkstraO1 = new DijkstraAlgorithm(dataO1);

        ShortestPathData dataO2
                = new ShortestPathData(graph,
                origin2,
                null,
                data.getArcInspector());
        this.dijkstraO2 = new DijkstraAlgorithm(dataO2);

        ShortestPathData dataD1
                = new ShortestPathData(graphTranspose,
                destination1,
                null,
                data.getArcInspector());
        this.dijkstraD1 = new DijkstraAlgorithm(dataD1);

        ShortestPathData dataD2
                = new ShortestPathData(graphTranspose,
                destination2,
                null,
                data.getArcInspector());
        this.dijkstraD2 = new DijkstraAlgorithm(dataD2);
    }

    @Override
    protected PackageSwitchSolution doRun() {

        List<DijkstraAlgorithm> dijkstraAlgorithms
                = Arrays.asList(dijkstraO1, dijkstraO2, dijkstraD1, dijkstraD2);

        Graph graph = data.getGraph();
        int graphSize = graph.size();

        int[] reached = new int[graphSize];
        Arrays.fill(reached, 0);

        Node lastNodeReached;
        DijkstraAlgorithm algorithmToStep;
        int count = 0;
        do {

            algorithmToStep = null;
            double minCost = Double.POSITIVE_INFINITY;

            //Choose the less advanced algorithm
            for (DijkstraAlgorithm dijkstraAlgorithm : dijkstraAlgorithms) {
                double cost = dijkstraAlgorithm.getMinCost();

                if (cost < minCost) {
                    algorithmToStep = dijkstraAlgorithm;
                    minCost = cost;
                }
            }

            //Step the algorithm and get the chosen min node
            lastNodeReached = null;
            if (algorithmToStep != null)
                lastNodeReached = algorithmToStep.step();

            if (lastNodeReached != null) {

                //Increase the number of times the nodes has been reached
                count = ++reached[lastNodeReached.getId()];
                notifyNodeReached(lastNodeReached, count);
            }

        } while (algorithmToStep != null && count < 4);

        if (lastNodeReached == null) {
            return new PackageSwitchSolution(getInputData(), AbstractSolution.Status.INFEASIBLE);
        }

        notifyMeetingReached(lastNodeReached);

        Node origin1 = getInputData().getOrigin1();
        Node origin2 = getInputData().getOrigin2();
        Node destination1 = getInputData().getDestination1();
        Node destination2 = getInputData().getDestination2();

        Node meeting = lastNodeReached;

        Node current;

        Path fromO1ToMeeting = dijkstraO1.getPathTo(meeting);
        Path fromD1ToMeeting = dijkstraD1.getPathTo(meeting);
        List<Arc> arcsFromMeetingToD1 = new ArrayList<>();
        for (Arc arc : fromD1ToMeeting.getArcs()) {
            arcsFromMeetingToD1.add(reverseArc(graph, arc));
        }
        Collections.reverse(arcsFromMeetingToD1);
        Path fromMeetingToD1 = new Path(graph, arcsFromMeetingToD1);

        Path fromO2ToMeeting = dijkstraO2.getPathTo(meeting);
        Path fromD2ToMeeting = dijkstraD2.getPathTo(meeting);
        List<Arc> arcsFromMeetingToD2 = new ArrayList<>();
        for (Arc arc : fromD2ToMeeting.getArcs()) {
            arcsFromMeetingToD2.add(reverseArc(graph, arc));
        }
        Collections.reverse(arcsFromMeetingToD2);
        Path fromMeetingToD2 = new Path(graph, arcsFromMeetingToD2);

        System.out.println("o1 :  " + origin1.getId());
        System.out.println("meeting :  " + meeting.getId());
        System.out.println("d1 : " + destination1.getId());

        System.out.println("o1 -> meeting origin : " + fromO1ToMeeting.getOrigin().getId());
        System.out.println("o1 -> meeting dest : " + fromO1ToMeeting.getDestination().getId());

        System.out.println("d1 -> meeting origin : " + fromD1ToMeeting.getOrigin().getId());
        System.out.println("d1 -> meeting dest : " + fromD1ToMeeting.getDestination().getId());

        System.out.println("meeting -> d1 origin : " + fromMeetingToD1.getOrigin().getId());
        System.out.println("meeting -> d1 dest : " + fromMeetingToD2.getDestination().getId());

        return new PackageSwitchSolution(getInputData(),
                AbstractSolution.Status.FEASIBLE,
                meeting,
                Path.concatenate(fromO1ToMeeting, fromMeetingToD1),
                Path.concatenate(fromO2ToMeeting, fromMeetingToD2));
    }

    private Arc reverseArc(Graph graph, Arc arc) {
        return new Arc() {
            @Override
            public Node getOrigin() {
                return graph.get(arc.getDestination().getId());
            }

            @Override
            public Node getDestination() {
                return graph.get(arc.getOrigin().getId());
            }

            @Override
            public float getLength() {
                return arc.getLength();
            }

            @Override
            public RoadInformation getRoadInformation() {
                return arc.getRoadInformation();
            }

            @Override
            public List<Point> getPoints() {
                return arc.getPoints();
            }
        };
    }
}
