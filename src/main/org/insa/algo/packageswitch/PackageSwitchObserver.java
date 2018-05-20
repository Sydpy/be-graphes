package org.insa.algo.packageswitch;

import org.insa.graph.Node;

public interface PackageSwitchObserver {

    void notifyNodeReached(Node node, int count);

    void notifyMeetingReached(Node node);
}
