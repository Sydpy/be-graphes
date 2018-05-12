package org.insa.algo.shortestpath;

import org.junit.Test;

import static org.junit.Assert.*;

public class AStarAlgorithmTest extends ShortestPathAlgorithmTest {

    @Override
    protected ShortestPathAlgorithm createAlgorithm(ShortestPathData data) {
        return new AStarAlgorithm(data);
    }
}