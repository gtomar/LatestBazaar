package basilica2.myagent;

import java.util.ArrayList;
import java.util.List;


public class PlanTree<String> {
    public Node<String> root;
    
    public PlanTree(String rootData) {
        root = new Node<String>();
        root.name = rootData;
        root.detailed_name = rootData;
        root.identified = false;
        root.children = new ArrayList<Node<String>>();
    }

    public static class Node<String> {
        public String name;
        public String detailed_name;
        public boolean identified;
        public PlanTree parent;
        public ArrayList<Node<String>> children;
    }
}