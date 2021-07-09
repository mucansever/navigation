import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TST<Value> {
    public Node<Value> root;

    public static class Node<Value> {
        public char c;
        public Node<Value> left, mid, right;
        public Value val;
    }

    /**
     * Inserts the key, value pair into ternary search tree
     * @param key: string
     * @param val: Value
     */
    public void put(String key, Value val) {
        // Skip if key is empty
        if (key==null)
            return;
        // Keeps index
        int i=0;
        // Initialize root if hadn't
        if (root==null) {
            root = new Node<>();
            root.c = key.charAt(i);
        }
        Node<Value> iterator = root;
        // Iterate through the tree, finding correct locations to insert new nodes to
        while (i<key.length()-1) {
            // Create new node object
            Node<Value> newNode = new Node<>();
            newNode.c = key.charAt(i);
            if (key.charAt(i) < iterator.c) {
                // If less than current node, move to left node
                if (iterator.left == null)
                    iterator.left = newNode;
                iterator = iterator.left;
            } else if (key.charAt(i)==iterator.c) {
                // If equal to current node, proceed with middle node
                if (iterator.mid == null)
                    iterator.mid = newNode;
                newNode.c = key.charAt(++i);
                iterator = iterator.mid;
            } else {
                // If greater than current node, move to right node
                if (iterator.right == null)
                    iterator.right = newNode;
                iterator = iterator.right;
            }
        }
        // Append the value to final node
        iterator.val = val;
    }

    /**
     * Returns a list of values starting with the given prefix
     * @param prefix: prefix of key
     * @return list of values
     */
    public List<Value> valuesWithPrefix(String prefix) {
        // If prefix is smaller than 2 characters, don't perform search
        if (prefix.length()<2)
            return new ArrayList<>();
        // Start from root
        Node<Value> iterator = root;
        // Keeps index
        int i=0;
        // Iterate through tst following prefix's path
        while (i<prefix.length()) {
            if (iterator==null)
                return new ArrayList<>();
            if (prefix.charAt(i)==iterator.c) {
                iterator = iterator.mid;
                i++;
            } else if (prefix.charAt(i)<iterator.c)
                iterator = iterator.left;
            else
                iterator = iterator.right;
        }

        Stack<Node<Value>> stack = new Stack<>();
        ArrayList<Value> answer = new ArrayList<>();
        stack.push(iterator);
        // Traverse the tree starting from last node of prefix
        while (!stack.isEmpty()) {
            Node<Value> curr = stack.pop();
            if (curr==null)
                continue;
            else if (curr.val != null)
                answer.add(curr.val);

            stack.push(curr.right);
            stack.push(curr.mid);
            stack.push(curr.left);
        }
        return answer;
    }
}