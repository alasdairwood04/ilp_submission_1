package uk.ac.ed.inf.ilpcw1.service;

import uk.ac.ed.inf.ilpcw1.data.LngLat;

/**
 * Node class for A* pathfinding algorithm
 */
public class Node {
    LngLat position;
    Node parent;
    double g; // Cost from start to this node
    double h; // Heuristic cost from this node to goal
    double f; // Total cost (g + h)

    Node(LngLat position, Node parent, double g, double h) {
        this.position = position;
        this.parent = parent;
        this.g = g;
        this.h = h;
        this.f = g + h;
    }

}
