package org.insa.algo.packageswitch;

import org.insa.algo.AbstractSolution;
import org.insa.algo.shortestpath.ShortestPathData;
import org.insa.graph.Arc;
import org.insa.graph.Node;
import org.insa.graph.Path;

public class PackageSwitchSolution extends AbstractSolution {

    private Node meeting;
    private Path path1;
    private Path path2;

    protected PackageSwitchSolution(PackageSwitchData data, Status status) {
        super(data, status);
    }

    protected PackageSwitchSolution(PackageSwitchData data, Status status, Node meeting, Path p1, Path p2) {
        super(data, status);
        this.meeting = meeting;
        this.path1 = p1;
        this.path2 = p2;
    }

    public Node getMeeting() {
        return meeting;
    }
    public Path getPath1() {
        return this.path1;
    }

    public Path getPath2() {
        return this.path2;
    }

    @Override
    public String toString() {
        String info = null;
        if (!isFeasible()) {
            info = String.format("No meeting point found for #%d to #%d and #%d to #%d",
                    ((PackageSwitchData) getInputData()).getOrigin1().getId(),
                    ((PackageSwitchData) getInputData()).getDestination1().getId(),
                    ((PackageSwitchData) getInputData()).getOrigin2().getId(),
                    ((PackageSwitchData) getInputData()).getDestination2().getId());
        }
        else {
            info = String.format("Found a meeting for #%d to #%d and #%d to #%d on #%d",
                    ((PackageSwitchData) getInputData()).getOrigin1().getId(),
                    ((PackageSwitchData) getInputData()).getDestination1().getId(),
                    ((PackageSwitchData) getInputData()).getOrigin2().getId(),
                    ((PackageSwitchData) getInputData()).getDestination2().getId(),
                    meeting.getId());
        }
        info += " in " + getSolvingTime().getSeconds() + " seconds.";
        return info;
    }
}
