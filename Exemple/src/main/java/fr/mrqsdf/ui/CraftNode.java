package fr.mrqsdf.ui;

public sealed interface CraftNode permits MaterialNode, FactoryNode {

    String id();      // unique node id
    int x();
    int y();
    int w();
    int h();

    void setPos(int x, int y);
}
