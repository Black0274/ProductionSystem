package Java;

import java.util.Set;

public class Node {
    private Set<String> set;
    private Node parent;
    private Rule rule;

    Node(Set<String> set, Node parent, Rule rule) {
        this.set = set;
        this.parent = parent;
        this.rule = rule;
    }

    Node(Set<String> set){
        this.set = set;
    }

    public Set<String> getSet() {
        return set;
    }

    public void setSet(Set<String> set) {
        this.set = set;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    @Override
    public boolean equals(Object object){
        Node node = (Node) object;
        if (node == null)
            return false;
        return set.equals(node.set);
    }

    @Override
    public int hashCode() {
        set.stream().map(String::hashCode).reduce((x, y) -> x ^ y).get();
        return 1;
    }
}
