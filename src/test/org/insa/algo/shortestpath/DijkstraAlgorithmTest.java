package org.insa.algo.shortestpath;

import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.*;

public class DijkstraAlgorithmTest extends ShortestPathAlgorithmTest {

    @Override
    public ShortestPathAlgorithm createAlgorithm(ShortestPathData data) {
        return new DijkstraAlgorithm(data);
    }
}