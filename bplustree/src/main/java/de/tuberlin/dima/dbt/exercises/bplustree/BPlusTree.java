package de.tuberlin.dima.dbt.exercises.bplustree;

import java.util.*;

/**
 * Implementation of a B+ tree.
 * <p>
 * The capacity of the tree is given by the capacity argument to the
 * constructor. Each node has at least {capacity/2} and at most {capacity} many
 * keys. The values are strings and are stored at the leaves of the tree.
 * <p>
 * For each inner node, the following conditions hold:
 * <p>
 * {pre}
 * Integer[] keys = innerNode.getKeys();
 * Node[] children = innerNode.getChildren();
 * {pre}
 * <p>
 * - All keys in {children[i].getKeys()} are smaller than {keys[i]}.
 * - All keys in {children[j].getKeys()} are greater or equal than {keys[i]}
 * if j > i.
 */
public class BPlusTree {

    ///// Implement these methods

    private LeafNode findLeafNode(Integer key, Node node,
                                  Deque<InnerNode> parents) {
        if (node instanceof LeafNode) {
            return (LeafNode) node;
        } else {
            InnerNode innerNode = (InnerNode) node;
            if (parents != null) {
                parents.push(innerNode);
            }
            // TODO: traverse inner nodes to find leaf node
            int branch = -1;

            Integer[] currentNodeKeys = innerNode.getKeys();
            for (int i = 0; i< currentNodeKeys.length; i++) {
                if (currentNodeKeys[i] == null) break;
                branch = i; // take left child
                if (currentNodeKeys[i] <= key) {
                    branch = i+1; // take right child
                } else {
                    break;
                }
            }

            Node[] children = innerNode.getChildren();
            if (branch != -1 && branch < children.length) {

                Node child = children[branch];
                if (child instanceof InnerNode) {
                    return findLeafNode(key, child, parents);
                }
                return (LeafNode) child;
            }

            return null;
        }
    }

    private String lookupInLeafNode(Integer key, LeafNode node) {
        // TODO: lookup value in leaf node
        if (node == null) return null;
        Integer[] keys = node.getKeys();
        String[] values = node.getValues();

        for (int i =0; i < keys.length; i++) {
            if (keys[i] == null) continue;

            if (keys[i].equals(key)) {
                return values[i];
            }
        }
        return null;
    }

    private void insertIntoLeafNode(Integer key, String value,
                                    LeafNode node, Deque<InnerNode> parents) {
        // TODO: insert value into leaf node (and propagate changes up)
        if (node == null) return;
        if (nodeHasSpace(node)) {
            fillLeafNode(key, value, node);

        } else { // full node -> split!
            List<Integer> keys = new ArrayList<>(Arrays.asList(node.getKeys()));
            List<String> values = new ArrayList<>(Arrays.asList(node.getValues()));

            int insertPos = findInsertPosition(keys, key);
            keys.add(insertPos, key);
            values.add(insertPos, value);

            // split node into two with smaller one on the left (1) and bigger one on the right (2)
            int splitPoint = keys.size() / 2;
            List<Integer> lKeys = keys.subList(0, splitPoint);
            List<String> lValues = values.subList(0, splitPoint);
            List<Integer> rKeys = keys.subList(splitPoint, keys.size());
            List<String> rValues = values.subList(splitPoint, values.size());

            // current Node is the left child
            node.setKeys(lKeys.toArray(new Integer[0]));
            node.setValues(lValues.toArray(new String[0]));

            // create new leaf node for right child node
            LeafNode rNode = new LeafNode(rKeys.toArray(new Integer[0]), rValues.toArray(new String[0]), BPlusTreeUtilities.CAPACITY);
            // propagate split to the parents, splitKey is the first key in the right leaf node (leaf node exception)
            propagateToParents(rKeys.get(0), node, rNode, parents);
        }
    }


    private Boolean nodeHasSpace(Node node) {
        Integer[] keys = node.getKeys();
        int counter = 0;
        for (Integer key : keys) {
            if (key == null) counter++;
        }
        return counter != 0;
    }

    private void fillLeafNode(Integer key, String value, LeafNode node) {
        List<Integer> keys = new ArrayList<>(Arrays.asList(node.getKeys()));
        List<String> values = new ArrayList<>(Arrays.asList(node.getValues()));

        int insertPos = findInsertPosition(keys, key);
        keys.add(insertPos, key);
        values.add(insertPos, value);

        node.setKeys(keys.toArray(new Integer[0]));
        node.setValues(values.toArray(new String[0]));
    }

    private void fillInnerNode(Integer key, Node parent, Node node) {
        List<Integer> keys = new ArrayList<>(Arrays.asList(parent.getKeys()));
        List<Node> pChildren = new ArrayList<>(Arrays.asList(((InnerNode)parent).getChildren()));

        int insertPos = findInsertPosition(keys, key);
        keys.add(insertPos, key);
        pChildren.add(insertPos + 1, node);

        parent.setKeys(keys.toArray(new Integer[0]));
        ((InnerNode)parent).setChildren(pChildren.toArray(new Node[0]));
    }

    private int findInsertPosition(List<Integer> keys, Integer key) {
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i) == null || keys.get(i) > key) {
                return i;
            }
        }
        return keys.size(); // Insert at the end when key bigger than all keys
    }

    private void propagateToParents(Integer splitKey, Node leftNode, Node rightNode, Deque<InnerNode> parents) {
        // we keep the leftNode in case we have to add it as a child of the root
        if (parents.isEmpty()) {
            // new root if there's no parent: split reached the root
            InnerNode newRoot = new InnerNode(BPlusTreeUtilities.CAPACITY);
            newRoot.setKeys(new Integer[]{splitKey});
            newRoot.setChildren(new Node[]{leftNode,rightNode});

            this.root = newRoot;
            return;
        }

        // get parent node from stack
        InnerNode parent = parents.pop();

        if (nodeHasSpace(parent)) {
            fillInnerNode(splitKey, parent, rightNode);
        } else {
            // Parent is full, split and propagate further
            splitInnerNode(parent, splitKey, rightNode, parents);
        }

    }

    private void splitInnerNode(InnerNode lNode, Integer splitKey, Node newChild, Deque<InnerNode> parents) {
        // add splitKey to overflow
        List<Integer> keys = new ArrayList<>(Arrays.asList(lNode.getKeys()));
        List<Node> children = new ArrayList<>(Arrays.asList(lNode.getChildren()));

        int indexToInsert = findInsertPosition(keys, splitKey);
        keys.add(indexToInsert, splitKey);
        children.add(indexToInsert + 1, newChild);

        // split the keys and children without middle key (ignore key at index splitPoint)
        int splitPoint = keys.size() / 2;

        List<Integer> lKeys = keys.subList(0, splitPoint);
        List<Node> lChildren = children.subList(0, splitPoint + 1);
        List<Integer> rKeys = keys.subList(splitPoint + 1, keys.size());
        List<Node> rChildren = children.subList(splitPoint + 1, children.size());

        // update current node with the left half
        lNode.setKeys(lKeys.toArray(new Integer[0]));
        lNode.setChildren(lChildren.toArray(new Node[0]));

        // create new inner node for right half
        InnerNode rNode = new InnerNode(rKeys.toArray(new Integer[0]), rChildren.toArray(new Node[0]), BPlusTreeUtilities.CAPACITY);

        // propagate the split key (= middle key m) to the parent
        propagateToParents(keys.get(splitPoint), lNode, rNode, parents);
    }

    private String deleteFromLeafNode(Integer key, LeafNode node,
                                      Deque<InnerNode> parents) {
        // TODO: delete value from leaf node (and propagate changes up)
        String value = lookupInLeafNode(key, node);
        removeFromLeafNode(key, node);

        // easy steal when node is at least capacity+1 -> no changes, simply return deleted key's value
        if (getNodeSize(node) >= BPlusTreeUtilities.CAPACITY/2) {
            return value;
        }

        // node is underfilled after deletion
        if (!parents.isEmpty()) {
            InnerNode parent = parents.pop();
            // steal
            if (checkSibling(parent, node, true)) return value;
            // merge
            if (checkSibling(parent, node, false)) return value; // merge shouldnt return false, if it does sth is wrong...
        }

        return null;
    }
    private int getNodeSize(Node node) {
        List<Integer> keys = new ArrayList<>(Arrays.asList(node.getKeys()));
        int counter = 0;

        for (Integer key: keys) {
            if (key != null) counter++;
        }
        return counter;
    }

    private void removeFromLeafNode(Integer key, LeafNode node) {
        List<Integer> keys = new ArrayList<>(Arrays.asList(node.getKeys()));
        List<String> values = new ArrayList<>(Arrays.asList(node.getValues()));

        int pos = findPosition(keys, key);
        keys.remove(pos);
        values.remove(pos);

        node.setKeys(keys.toArray(new Integer[0]));
        node.setValues(values.toArray(new String[0]));
    }

    private int findPosition(List<Integer> keys, Integer key) {
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i) == null) break; // this should never occur, not possible due to if condition
            if (keys.get(i).equals(key)) {
                return i;
            }
        }
        return keys.size()-1; // Insert at the end when key bigger than all keys
    }

    private Boolean checkSibling(InnerNode parent, Node currentNode, Boolean isSteal) {
        List<Node> children = new ArrayList<>(Arrays.asList(parent.getChildren()));
        int currentIdx = -1;

        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) == null) break;
            if (children.get(i) == currentNode) {
                currentIdx = i;
                break;
            }
        }

        if (isSteal) {
            // check left sibling
            if (currentIdx > 0) {
                Node leftSibling = children.get(currentIdx - 1);
                if (stealFromSibling(leftSibling, currentNode, parent, currentIdx, true)) {
                    return true;
                }
            }

            // check right sibling
            if (currentIdx < children.size() - 1) {
                Node rightSibling = children.get(currentIdx + 1);
                if (stealFromSibling(rightSibling, currentNode, parent, currentIdx, false)) {
                    return true;
                }
            }
        }
        else { // merge
            // check right sibling
            if (currentIdx < children.size() - 1) {
                Node rightSibling = children.get(currentIdx + 1);
                mergeWithSibling(rightSibling, currentNode, parent, currentIdx, true);
                return true;
            }

            // check left sibling
            if (currentIdx > 0) {
                Node leftSibling = children.get(currentIdx - 1);
                mergeWithSibling(leftSibling, currentNode, parent, currentIdx, false);
                return true;
            }
        }
        return false;
    }

    private boolean stealFromSibling(Node sibling, Node currentNode, InnerNode parent, int currentIdx, boolean isLeftSibling) {
        int size = getNodeSize(sibling);

        // only steal when sibling has more than the minimum keys (capacity/2+1)
        if (size > BPlusTreeUtilities.CAPACITY / 2) {
            List<Integer> siblingKeys = new ArrayList<>(Arrays.asList(sibling.getKeys()));
            List<Integer> currentKeys = new ArrayList<>(Arrays.asList(currentNode.getKeys()));

            String value = null;
            int idx = isLeftSibling ? size - 1 : 0; // when left sibling, take last key, when right take first key

            // remove key from sibling
            Integer key = siblingKeys.remove(idx);
            sibling.setKeys(siblingKeys.toArray(new Integer[0]));
            int insertPos = findInsertPosition(currentKeys, key);

            if (currentNode instanceof LeafNode) {
                List<String> siblingValues = new ArrayList<>(Arrays.asList(((LeafNode) sibling).getValues()));
                value = siblingValues.remove(idx);
                ((LeafNode) sibling).setValues(siblingValues.toArray(new String[0]));
            }

            // add key to the current node
            currentKeys.add(insertPos, key);
            currentNode.setKeys(currentKeys.toArray(new Integer[0]));

            if (currentNode instanceof LeafNode && value != null) {
                List<String> currentValues = new ArrayList<>(Arrays.asList(((LeafNode) currentNode).getValues()));
                currentValues.add(insertPos, value);
                ((LeafNode) currentNode).setValues(currentValues.toArray(new String[0]));
            }

            // update the parent
            List<Integer> parentKeys = new ArrayList<>(Arrays.asList(parent.getKeys()));
            if (isLeftSibling) {
                int updatePos = findInsertPosition(parentKeys, key);
                parentKeys.set(updatePos, key);
            } else {
                int siblingKey = sibling.getKeys()[0];
                if (currentIdx == 0) currentIdx++;
                parentKeys.set(currentIdx-1, siblingKey);
            }
            parent.setKeys(parentKeys.toArray(new Integer[0]));

            return true;
        }

        return false;
    }

    private void mergeWithSibling(Node sibling, Node currentNode, InnerNode parent, int currentIdx, boolean isRightSibling) {

    }

    ///// Public API
    ///// These can be left unchanged

    /**
     * Lookup the value stored under the given key.
     * @return The stored value, or {null} if the key does not exist.
     */
    public String lookup(Integer key) {
        LeafNode leafNode = findLeafNode(key, root);
        return lookupInLeafNode(key, leafNode);
    }

    /**
     * Insert the key/value pair into the B+ tree.
     */
    public void insert(int key, String value) {
        Deque<InnerNode> parents = new LinkedList<>();
        LeafNode leafNode = findLeafNode(key, root, parents);
        insertIntoLeafNode(key, value, leafNode, parents);
    }

    /**
     * Delete the key/value pair from the B+ tree.
     * @return The original value, or {null} if the key does not exist.
     */
    public String delete(Integer key) {
        Deque<InnerNode> parents = new LinkedList<>();
        LeafNode leafNode = findLeafNode(key, root, parents);
        return deleteFromLeafNode(key, leafNode, parents);
    }

    ///// Leave these methods unchanged

    private int capacity = 0;

    private Node root;

    public BPlusTree(int capacity) {
        this(new LeafNode(capacity), capacity);
    }

    public BPlusTree(Node root, int capacity) {
        assert capacity % 2 == 0;
        this.capacity = capacity;
        this.root = root;
    }

    public Node rootNode() {
        return root;
    }

    public String toString() {
        return new BPlusTreePrinter(this).toString();
    }

    private LeafNode findLeafNode(Integer key, Node node) {
        return findLeafNode(key, node, null);
    }

}
