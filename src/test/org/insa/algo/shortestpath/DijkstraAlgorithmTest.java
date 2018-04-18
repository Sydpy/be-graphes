package org.insa.algo.shortestpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.insa.algo.ArcInspector;
import org.insa.algo.AbstractInputData.Mode;
import org.insa.algo.ArcInspectorFactory;
import org.insa.graph.*;
import org.insa.graph.RoadInformation.RoadType;
import org.insa.graph.io.BinaryGraphReader;
import org.insa.graph.io.GraphReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.io.*;
import java.util.*;


public class DijkstraAlgorithmTest extends ShortestPathAlgorithmTest{

    @Override
    protected ShortestPathAlgorithm createAlgorithm(ShortestPathData data) {
        return new DijkstraAlgorithm(data);
    }
}