import java.util.LinkedList;
import java.util.List;

/**
 * The TST class represents a map with string keys and non-null values. It also
 * provides character-based methods for returning the key that is the longest
 * prefix of a string and returning all keys that start with a given prefix.
 * @author Robert Sedgewick
 * @author Kevin Wayne
 * @author Kevin Lin
 */
public class TST<Value> {
    private int n;              // size
    private Node<Value> root;   // root of TST

    private static class Node<Value> {
        private char c;                        // character
        private Node<Value> left, mid, right;  // left, middle, and right subtrees
        private Value val;                     // value associated with string
    }

    /**
     * Returns the number of key-value pairs in this map.
     * @return the number of key-value pairs in this map
     */
    public int size() {
        return n;
    }

    /**
     * Does this map contain the given key?
     * @param key the key
     * @return true if this map contains key and false otherwise
     * @throws NullPointerException if key is null
     * @throws IllegalArgumentException if key is empty
     */
    public boolean containsKey(String key) {
        if (key == null) {
            throw new NullPointerException("calls containsKey() with null argument");
        }
        return get(key) != null;
    }

    /**
     * Returns the value associated with the given key.
     * @param key the key
     * @return the value associated with the given key if the key is in the map
     *     and null if the key is not in the map
     * @throws NullPointerException if key is null
     * @throws IllegalArgumentException if key is empty
     */
    public Value get(String key) {
        if (key == null) {
            throw new NullPointerException("calls get() with null argument");
        } else if (key.length() == 0) {
            throw new IllegalArgumentException("key must have length >= 1");
        }
        Node<Value> x = get(root, key, 0);
        if (x == null) return null;
        return x.val;
    }

    // Returns subtree corresponding to given key
    private Node<Value> get(Node<Value> x, String key, int d) {
        if (x == null) return null;
        char c = key.charAt(d);
        if      (c < x.c)              return get(x.left,  key, d);
        else if (c > x.c)              return get(x.right, key, d);
        else if (d < key.length() - 1) return get(x.mid,   key, d + 1);
        else                           return x;
    }

    /**
     * Inserts the key-value pair into the map, overwriting the old value with
     * the new value if the key is already in the map.
     * @param key the key
     * @param val the value
     * @throws NullPointerException if key or val is null
     * @throws IllegalArgumentException if key is empty
     */
    public void put(String key, Value val) {
        if (key == null) {
            throw new NullPointerException("calls put() with null key");
        } else if (val == null) {
            throw new NullPointerException("calls put() with null val");
        }
        if (!containsKey(key)) {
            n++;
            root = put(root, key, val, 0);
        }
    }

    private Node<Value> put(Node<Value> x, String key, Value val, int d) {
        char c = key.charAt(d);
        if (x == null) {
            x = new Node<Value>();
            x.c = c;
        }
        if      (c < x.c)               x.left  = put(x.left,  key, val, d);
        else if (c > x.c)               x.right = put(x.right, key, val, d);
        else if (d < key.length() - 1)  x.mid   = put(x.mid,   key, val, d + 1);
        else                            x.val   = val;
        return x;
    }

    /**
     * Returns the string in the map that is the longest prefix of query, or
     * null, if no such string.
     * @param query the query string
     * @return the string in the map that is the longest prefix of query, or
     *     null if no such string
     * @throws NullPointerException if query is null
     */
    public String longestPrefixOf(String query) {
        if (query == null) {
            throw new NullPointerException("calls longestPrefixOf() with null argument");
        }
        if (query.length() == 0) return null;
        int length = 0;
        Node<Value> x = root;
        int i = 0;
        while (x != null && i < query.length()) {
            char c = query.charAt(i);
            if      (c < x.c) x = x.left;
            else if (c > x.c) x = x.right;
            else {
                i++;
                if (x.val != null) length = i;
                x = x.mid;
            }
        }
        return query.substring(0, length);
    }

    /**
     * Returns all keys in the map as a List.
     * @return all keys in the map as a List
     */
    public List<String> keys() {
        List<String> list = new LinkedList<String>();
        collect(root, new StringBuilder(), list);
        return list;
    }

    /**
     * Returns all of the keys in the set that start with prefix.
     * @param prefix the prefix
     * @return all of the keys in the set that start with prefix as a list
     * @throws NullPointerException if prefix is null
     * @throws IllegalArgumentException if prefix is empty
     */
    public List<String> keysWithPrefix(String prefix) {
        if (prefix == null) {
            throw new NullPointerException("calls keysWithPrefix() with null argument");
        } else if (prefix.length() == 0) {
            throw new IllegalArgumentException("prefix must have length >= 1");
        }
        List<String> list = new LinkedList<String>();
        Node<Value> x = get(root, prefix, 0);
        if (x == null) return list;
        if (x.val != null) list.add(prefix);
        collect(x.mid, new StringBuilder(prefix), list);
        return list;
    }

    // Collects all keys in subtree rooted at x with the given prefix
    private void collect(Node<Value> x, StringBuilder prefix, List<String> list) {
        if (x == null) return;
        collect(x.left,  prefix, list);
        if (x.val != null) list.add(prefix.toString() + x.c);
        prefix.append(x.c);
        collect(x.mid,   prefix, list);
        prefix.deleteCharAt(prefix.length() - 1);
        collect(x.right, prefix, list);
    }

    public static void main(String[] args) {
        TST<Boolean> tst = new TST<Boolean>();
        for (String s : "she sells sea shells by the sea shore".split(" "))
            tst.put(s, true);

        System.out.println("keys():");
        for (String s : tst.keys())
            System.out.println(s);
        System.out.println();

        System.out.println("longestPrefixOf(\"shellsort\"):");
        System.out.println(tst.longestPrefixOf("shellsort"));
        System.out.println();

        System.out.println("longestPrefixOf(\"shell\"):");
        System.out.println(tst.longestPrefixOf("shell"));
        System.out.println();

        System.out.println("keysWithPrefix(\"sh\"):");
        for (String s : tst.keysWithPrefix("sh"))
            System.out.println(s);
        System.out.println();
    }
}