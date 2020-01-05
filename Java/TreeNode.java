package Java;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TreeNode {
    public String fact;
    public List<Set<TreeNode>> branches = new ArrayList<>();

    public TreeNode(String fact){
        this.fact = fact;
    }

    @Override
    public boolean equals(Object obj) {
        TreeNode node = (TreeNode) obj;
        if (node == null)
            return false;
        return fact.equals(node.fact) && branches.equals(node.branches);
    }

    @Override
    public int hashCode() {
        return fact.hashCode();
    }

    public String toString(int index) {
        StringBuilder builder = new StringBuilder();
        for (TreeNode node : branches.get(index)){
            builder.append(node.fact).append(", ");
        }
        builder.deleteCharAt(builder.length() - 2);
        builder.append("-> ").append(fact);
        return builder.toString();
    }
}
