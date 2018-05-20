package org.insa.algo.packageswitch;

import org.insa.algo.AbstractAlgorithm;
import org.insa.graph.Node;

public abstract class PackageSwitchAlgorithm extends AbstractAlgorithm<PackageSwitchObserver> {

    /**
     * Create a new PackageSwitchAlgorithm with the given data.
     * 
     * @param data
     */
    protected PackageSwitchAlgorithm(PackageSwitchData data) {
        super(data);
    }

    @Override
    public PackageSwitchSolution run() {
        return (PackageSwitchSolution) super.run();
    }

    @Override
    protected abstract PackageSwitchSolution doRun();

    @Override
    public PackageSwitchData getInputData() {
        return (PackageSwitchData) super.getInputData();
    }

    protected void notifyNodeReached(Node none, int count) {
        for (PackageSwitchObserver observer : observers) {
            observer.notifyNodeReached(none, count);
        }
    }

    protected void notifyMeetingReached(Node node) {
        for (PackageSwitchObserver observer : observers) {
            observer.notifyMeetingReached(node);
        }
    }
}
