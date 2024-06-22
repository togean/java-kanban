package models;

import java.util.Objects;

public class Node<T> {
    T nodeItem;
    Node<T> nexItem;
    Node<T> prevItem;

    public Node(T nodeItem, Node<T> nexItem, Node<T> prevItem) {
        this.nodeItem = nodeItem;
        this.nexItem = nexItem;
        this.prevItem = prevItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?> node = (Node<?>) o;
        return Objects.equals(nodeItem, node.nodeItem) && Objects.equals(nexItem, node.nexItem) && Objects.equals(prevItem, node.prevItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeItem);
    }

    public T getNodeItem() {
        return nodeItem;
    }

    public void setNodeItem(T nodeItem, Node<T> nextItem, Node<T> prevItem) {
        this.nodeItem = nodeItem;
        this.nexItem = nextItem;
        this.prevItem = prevItem;
    }

    public Node<T> getNexItem() {
        return nexItem;
    }

    public void setNexItem(Node<T> nexItem) {
        this.nexItem = nexItem;
    }

    public Node<T> getPrevItem() {
        return prevItem;
    }

    public void setPrevItem(Node<T> prevItem) {
        this.prevItem = prevItem;
    }
}