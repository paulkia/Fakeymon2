import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Node tree = new Node();
        tree.val = 2;
        Node leftSubTree = new Node(new Node(5), new Node(0));
        Node rightSubTree = new Node(new Node(0), new Node(5));
        leftSubTree.val = 2;
        rightSubTree.val = 4;
        tree.left = leftSubTree;
        tree.right = rightSubTree;
        System.out.println(tree);
        System.out.print(leftTreeSum(tree));
    }

    private static class Node {
        int val;
        Node left, right;

        // Added this construction to help building, not required for algorithm
        Node() { }
        Node(int x) {
            val = x;
        }
        Node(Node l, Node r) {
            left = l;
            right = r;
        }

        public String toString() {
            return toString(this, layerToNodes());
        }
        private String toString(Node node, Map<Integer, List<Node>> layerToNodes) {
            String result = "";
            for (int i : layerToNodes.keySet()) {
                List<Node> thisLayersNodes = layerToNodes.get(i);
                for (Node n : thisLayersNodes) {
                    result += n.val + " ";
                }
                result = result.substring(0, result.length() - 1) + "\n";
            }
            return result;
        }
        private Map<Integer, List<Node>> layerToNodes() {
            return layerToNodes(this, 0, new TreeMap<>());
        }
        private Map<Integer, List<Node>> layerToNodes(Node node, int depth, Map<Integer, List<Node>> result) {
            if (node != null) {
                if (result.containsKey(depth)) {
                    result.get(depth).add(node);
                } else {
                    List<Node> nodes = new ArrayList<>();
                    nodes.add(node);
                    result.put(depth, nodes);
                }
                layerToNodes(node.left, depth + 1, result);
                layerToNodes(node.right, depth + 1, result);
            }
            return result;
        }
        //
    }

    public static Node leftTreeSum(Node root) {
        if(root == null || root.left == null && root.right == null){
            return root;
        }
        List<Integer> currSum = new ArrayList<>();
        helper(currSum, root, 0);
        return root;
    }


    public static void helper(List<Integer> currSum, Node root, int depth){
        if(root.left == null && root.right == null){
            return;
        }
        Node left = root.left, right = root.right;
        if(left != null){
            if(depth < currSum.size()){
                left.val += currSum.get(depth);
                currSum.set(depth, left.val);
            } else {
                currSum.add(left.val);
            }

        }
        if(right != null){
            if(depth < currSum.size()){
                right.val += currSum.get(depth);
                currSum.set(depth, right.val);
            } else {
                currSum.add(right.val);
            }
        }
        helper(currSum, root.left, depth + 1);
        helper(currSum, root.right, depth + 1);

    }

/*
    public static Node leftTreeSum(Node tree) {
        Map<Integer, Integer> depthToSum = new HashMap<>();
        changeTree(tree, 0, depthToSum);
        return tree;
    }
    public static void changeTree(Node node, int depth, Map<Integer, Integer> depthToSum) {
        if (node != null) {
            changeTree(node.left, depth + 1, depthToSum);
            if (depthToSum.get(depth) != null) {
                node.val += depthToSum.get(depth);
            }
            depthToSum.put(depth, node.val);
            changeTree(node.right, depth + 1, depthToSum);
        }
    }

 */
    /**
     * Aman behavioral:
     *      quick decision, result/reprecussions
     *          election, Aman was a TA (lots of tension, midterms, stressful time)
     *          staff meeting
     *          Justin going thru logistics of upcoming exam: slightly harder, 5 hours, students "are likely going to cheat"
     *          test more difficult due to online environment
     *          Justin leaves time for questions at the end typically in staff meetings
     *          Quick decision to speak up: "too long; not our job to prevent students from cheating; deter it, not at the
     *              cost of students mental health, especially at a time with a lot of tension"
     *          Compromised at 3 hours
     *          Result: good for students
     *          Learned: speaking up when you feel strongly about things is important, especially when nobody else is
     *              For a lot of TA's, it might be tough to think outside of your own interest
     *              Disagreeing in a controlled and respectful way can reap benefits
     *      challenge you faced in a project, how did you solve it?
     *          UW motorsports team since freshman year; mechanical design role when joining the team
     *          worked with safety structures: break in controlled way
     *          measure strain on safety structure; many want to test their projects
     *          worked on wireless torque sensing from sophomore year onward
     *          challenge: find out way to conveniently measure & test parts
     *          
     */
}