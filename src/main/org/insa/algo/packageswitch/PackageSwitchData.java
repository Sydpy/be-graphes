package org.insa.algo.packageswitch;

import org.insa.algo.AbstractInputData;
import org.insa.algo.ArcInspector;
import org.insa.graph.Graph;
import org.insa.graph.Node;

public class PackageSwitchData extends AbstractInputData {

    private final Node origin1, origin2, destination1, destination2;

    public PackageSwitchData(Graph graph,
                                Node origin1, Node origin2,
                                Node destination1, Node destination2,
                                ArcInspector arcFilter) {
        super(graph, arcFilter);
        this.origin1 = origin1;
        this.origin2 = origin2;
        this.destination1 = destination1;
        this.destination2 = destination2;
    }

    public Node getOrigin1() {
        return origin1;
    }

    public Node getOrigin2() {
        return origin2;
    }

    public Node getDestination1() {
        return destination1;
    }

    public Node getDestination2() {
        return destination2;
    }


    @Override
    public String toString() {
        return "Package switch for #" +
                origin1.getId() + " to #" + destination1.getId() + " and #" +
                origin2.getId() + " to #" + destination2.getId() + " ["
                + this.arcInspector.toString().toLowerCase() + "]";
    }
}
