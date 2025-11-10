package com.delivery.routing.algorithm;

import com.delivery.routing.domain.DistanceMatrix;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MinimumSpanningTreeBuilder {

    public List<Edge> buildMST(DistanceMatrix matrix) {
        int n = matrix.getLocations().size();
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingDouble(Edge::getWeight));
        boolean[] inMST = new boolean[n];
        List<Edge> mst = new ArrayList<>();

        initializePriorityQueue(pq, matrix, n);
        inMST[0] = true;

        buildMSTFromQueue(pq, inMST, mst, matrix, n);

        return mst;
    }

    private void initializePriorityQueue(PriorityQueue<Edge> pq, DistanceMatrix matrix, int n) {
        for (int i = 1; i < n; i++) {
            pq.offer(new Edge(0, i, matrix.getDistance(0, i)));
        }
    }

    private void buildMSTFromQueue(
            PriorityQueue<Edge> pq,
            boolean[] inMST,
            List<Edge> mst,
            DistanceMatrix matrix,
            int n
    ) {
        while (!pq.isEmpty() && mst.size() < n - 1) {
            Edge edge = pq.poll();
            if (inMST[edge.getTo()]) {
                continue;
            }

            mst.add(edge);
            int v = edge.getTo();
            inMST[v] = true;

            addNewEdgesToQueue(pq, inMST, matrix, v, n);
        }
    }

    private void addNewEdgesToQueue(
            PriorityQueue<Edge> pq,
            boolean[] inMST,
            DistanceMatrix matrix,
            int vertex,
            int n
    ) {
        for (int i = 0; i < n; i++) {
            if (!inMST[i]) {
                pq.offer(new Edge(vertex, i, matrix.getDistance(vertex, i)));
            }
        }
    }
}

