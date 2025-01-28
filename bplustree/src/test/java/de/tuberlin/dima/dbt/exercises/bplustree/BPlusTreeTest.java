package de.tuberlin.dima.dbt.exercises.bplustree;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import static de.tuberlin.dima.dbt.grading.bplustree.BPlusTreeMatcher.isTree;
import static de.tuberlin.dima.dbt.exercises.bplustree.BPlusTreeUtilities.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class BPlusTreeTest {

    // fail each test after 1 second
    @Rule
    public Timeout globalTimeout = new Timeout(1000);

    private BPlusTree tree;

    ///// Lookup tests

    @Test
    public void findKeyInLeaf() {
        // given
        tree = newTree(newLeaf(keys(1, 2, 3), values("a", "b", "c")));
        // when
        String value = tree.lookup(2);
        // then
        assertThat(value, is("b"));
    }

    @Test
    public void findNoKeyInLeaf() {
        // given
        tree = newTree(newLeaf(keys(1, 3), values("a", "c")));
        // when
        String value = tree.lookup(2);
        // then
        assertThat(value, is(nullValue()));
    }

    @Test
    public void findKeyInChild() {
        // given
        tree = newTree(newNode(keys(3),
                               nodes(newLeaf(keys(1, 2), values("a", "b")),
                                     newLeaf(keys(3, 4), values("c", "d")))));
        // when
        String value = tree.lookup(1);
        // then
        assertThat(value, is("a"));
    }

    @Test
    public void findNoKeyInChild() {
        // given
        tree = newTree(newNode(keys(3),
                               nodes(newLeaf(keys(1, 3), values("a", "c")),
                                     newLeaf(keys(5, 7), values("e", "g")))));
        // when
        String value = tree.lookup(6);
        // then
        assertThat(value, is(nullValue()));
    }

    ///// Insertion tests

    @Test
    public void insertIntoLeaf() {
        // given
        tree = newTree(newLeaf(keys(1, 3), values("a", "c")));
        // when
        tree.insert(2, "b");
        // then
        assertThat(tree, isTree(
                newTree(newLeaf(keys(1, 2, 3), values("a", "b", "c")))));
    }

    @Test
    public void splitLeafs() {
        // given (d=2)
        tree = newTree(newNode(keys(3),
                               nodes(newLeaf(keys(1, 2), values("a", "b")),
                                     newLeaf(keys(3, 4, 5, 6),
                                             values("c", "d", "e", "f")))));
        // when
        tree.insert(7, "g");
        // then
        assertThat(tree, isTree(newTree(newNode(
                keys(3, 5),
                nodes(newLeaf(keys(1, 2), values("a", "b")),
                      newLeaf(keys(3, 4), values("c", "d")),
                      newLeaf(keys(5, 6, 7), values("e", "f", "g")))))));
    }
    @Test
    public void insertToEmptyRoot() {
        // given
        tree = newEmptyTree();
        // when
        tree.insert(2, "b");
        // then
        assertThat(tree, isTree(
                newTree(newLeaf(keys(2), values("b")))));
    }

    @Test
    public void insertInto2dExampleEmptyLeafNode() { // order of tree doesn't count root
        // given
        tree = newTree(newNode(keys(51),
                nodes(newNode(keys(11,30),
                                nodes(newLeaf(keys(2,7), values("2", "7")), newLeaf(keys(12,15,22), values("12", "15", "22")), newLeaf(keys(35,41), values("35", "41")))),
                        newNode(keys(66,78),
                                nodes(newLeaf(keys(53,54,63), values("53", "54","63")), newLeaf(keys(68,69,71,76), values("68", "69", "71", "76")), newLeaf(keys(79,84,93), values("79", "84", "93"))))
                        )));
        // when
        tree.insert(57, "57");
        // then
        assertThat(tree, isTree(
                newTree(newNode(keys(51),
                        nodes(newNode(keys(11,30),
                                        nodes(newLeaf(keys(2,7), values("2", "7")), newLeaf(keys(12,15,22), values("12", "15", "22")), newLeaf(keys(35,41), values("35", "41")))),
                                newNode(keys(66,78),
                                        nodes(newLeaf(keys(53,54,57,63), values("53", "54","57","63")), newLeaf(keys(68,69,71,76), values("68", "69", "71", "76")), newLeaf(keys(79,84,93), values("79", "84", "93"))))
                        )))));
    }

    @Test
    public void insertInto2dExampleFullLeafNodeRight() { // order of tree doesn't count root
        // given
        tree = newTree(newNode(keys(51),
                nodes(newNode(keys(11,30),
                                nodes(newLeaf(keys(2,7), values("2", "7")), newLeaf(keys(12,15,22), values("12", "15", "22")), newLeaf(keys(35,41), values("35", "41")))),
                        newNode(keys(66,78),
                                nodes(newLeaf(keys(53,54,63), values("53", "54","63")), newLeaf(keys(68,69,71,76), values("68", "69", "71", "76")), newLeaf(keys(79,84,93), values("79", "84", "93"))))
                )));
        // when
        tree.insert(72, "72");
        // then
        assertThat(tree, isTree(
                newTree(newNode(keys(51),
                        nodes(newNode(keys(11,30),
                                        nodes(newLeaf(keys(2,7), values("2", "7")), newLeaf(keys(12,15,22), values("12", "15", "22")), newLeaf(keys(35,41), values("35", "41")))),
                                newNode(keys(66,71,78),
                                        nodes(newLeaf(keys(53,54,63), values("53", "54","63")), newLeaf(keys(68,69), values("68", "69")), newLeaf(keys(71,72,76), values("71", "72", "76")), newLeaf(keys(79,84,93), values("79", "84", "93"))))
                        )))));
    }

    @Test
    public void insertInto2dExampleFullLeafNodeRightWithFullParents() {
        // given
        tree = newTree(newNode(keys(51),
                nodes(newNode(keys(11,30),
                                nodes(newLeaf(keys(2,7), values("2", "7")), newLeaf(keys(12,15,22), values("12", "15", "22")), newLeaf(keys(35,41), values("35", "41")))),
                        newNode(keys(63,66,71,78),
                                nodes(newLeaf(keys(53,54), values("53", "54")), newLeaf(keys(63,64,65), values("63","64","65")), newLeaf(keys(68,69), values("68", "69")), newLeaf(keys(71,72,76), values("71", "72", "76")), newLeaf(keys(79,84,93,94), values("79", "84", "93","94"))))
                )));
        // when
        tree.insert(95, "95");
        // then
        assertThat(tree, isTree(
                newTree(newNode(keys(51,71),
                        nodes(newNode(keys(11,30),
                                        nodes(newLeaf(keys(2,7), values("2", "7")), newLeaf(keys(12,15,22), values("12", "15", "22")), newLeaf(keys(35,41), values("35", "41")))),
                                newNode(keys(63,66),
                                        nodes(newLeaf(keys(53,54), values("53", "54")), newLeaf(keys(63,64,65), values("63","64","65")), newLeaf(keys(68,69), values("68", "69")))),
                                newNode(keys(78, 93),
                                        nodes(newLeaf(keys(71,72,76), values("71", "72", "76")), newLeaf(keys(79,84), values("79", "84")), newLeaf(keys(93,94,95), values("93","94","95"))))
                        )))));
    }

    @Test
    public void insertInto2dExampleFullLeafNodeRightWithFullRoot() { // TODO
        // given
        tree = newTree(newNode(keys(51,71,78),
                nodes(newNode(keys(11,30),
                                nodes(newLeaf(keys(2,7), values("2", "7")), newLeaf(keys(12,15,22), values("12", "15", "22")), newLeaf(keys(35,41), values("35", "41")))),
                        newNode(keys(63,66),
                                nodes(newLeaf(keys(53,54), values("53", "54")), newLeaf(keys(63,64,65), values("63","64","65")), newLeaf(keys(68,69), values("68", "69")))),
                        newNode(keys(73, 75, 78, 93, 95),
                                nodes(newLeaf(keys(71,72), values("71", "72")), newLeaf(keys(73,74), values("73","74")), newLeaf(keys(75,76,77), values("75","76","77")), newLeaf(keys(79,84), values("79", "84")), newLeaf(keys(93,94), values("93","94")), newLeaf(keys(95, 96, 97), values("95","96","97"))))
                )));
        // when
        tree.insert(72, "72");
        // then
        assertThat(tree, isTree(
                newTree(newNode(keys(51),
                        nodes(newNode(keys(11,30),
                                        nodes(newLeaf(keys(2,7), values("2", "7")), newLeaf(keys(12,15,22), values("12", "15", "22")), newLeaf(keys(35,41), values("35", "41")))),
                                newNode(keys(66,71,78),
                                        nodes(newLeaf(keys(53,54,63), values("53", "54","63")), newLeaf(keys(68,69), values("68", "69")), newLeaf(keys(71,72,76), values("71", "72", "76")), newLeaf(keys(79,84,93), values("79", "84", "93"))))
                        )))));
    }

    @Test
    public void insertInto2dExampleFullLeafNodeLeft() { // order of tree doesn't count root
        // given
        tree = newTree(newNode(keys(51),
                nodes(newNode(keys(11,30),
                                nodes(newLeaf(keys(2,7), values("2", "7")), newLeaf(keys(12,15,22,23), values("12", "15", "22", "23")), newLeaf(keys(35,41), values("35", "41")))),
                        newNode(keys(66,78),
                                nodes(newLeaf(keys(53,54,63), values("53", "54","63")), newLeaf(keys(68,69,71,76), values("68", "69", "71", "76")), newLeaf(keys(79,84,93), values("79", "84", "93"))))
                )));
        // when
        tree.insert(26, "26");
        // then
        assertThat(tree, isTree(
                newTree(newNode(keys(51),
                        nodes(newNode(keys(11,22,30),
                                        nodes(newLeaf(keys(2,7), values("2", "7")), newLeaf(keys(12,15), values("12", "15")), newLeaf(keys(22,23,26), values("22", "23", "26")), newLeaf(keys(35,41), values("35", "41")))),
                                newNode(keys(66,78),
                                        nodes(newLeaf(keys(53,54,63), values("53", "54","63")), newLeaf(keys(68,69,71,76), values("68", "69", "71", "76")), newLeaf(keys(79,84,93), values("79", "84", "93"))))
                        )))));
    }

    @Test
    public void insertInto3dExampleFullRoot() { // Has to split at root and tree becomes 1 dimension larger
        // given
        tree = newTree(
                newNode(
                        keys(30, 50, 81, 95), // Full root
                        nodes(
                                newNode(
                                        keys(15, 25),
                                        nodes(
                                                newLeaf(keys(5, 10, 12, 14), values("5", "10", "12", "14")), // Full leaf node
                                                newLeaf(keys(16, 18, 20, 22), values("16", "18", "20", "22")),
                                                newLeaf(keys(26, 28), values("26", "28"))
                                        )
                                ),
                                newNode(
                                        keys(35, 45),
                                        nodes(
                                                newLeaf(keys(30, 32, 34), values("30", "32", "34")),
                                                newLeaf(keys(36, 38, 40, 44), values("36", "38", "40", "44")),
                                                newLeaf(keys(45, 46, 47, 48), values("45", "46", "47", "48"))
                                        )
                                ),
                                newNode(
                                        keys(60, 75),
                                        nodes(
                                                newLeaf(keys(51, 53, 55), values("51", "53", "55")),
                                                newLeaf(keys(61, 63, 65, 68), values("61", "63", "65", "68")),
                                                newLeaf(keys(76, 78, 79), values("76", "78", "80"))
                                        )
                                ),
                                newNode(
                                        keys(83, 88),
                                        nodes(
                                                newLeaf(keys(80, 81, 82), values("80", "81", "82")),
                                                newLeaf(keys(83, 84, 85, 86), values("83", "84", "85", "86")),
                                                newLeaf(keys(88, 89, 90), values("88", "89", "90"))
                                        )
                                ),
                                newNode(
                                        keys(96, 110, 150, 200), // Full inner node
                                        nodes(
                                                newLeaf(keys(91, 92, 93), values("91", "92", "93")),
                                                newLeaf(keys(99, 100, 101, 102), values("99", "100", "101", "102")), // Full leaf node
                                                newLeaf(keys(115, 116, 117), values("115", "116", "117")),
                                                newLeaf(keys(160, 165, 169), values("160", "165", "169")),
                                                newLeaf(keys(200, 209, 220, 230), values("200","209", "220", "230")) // Full leaf node
                                        )
                                )
                        )
                )
        );
        // when
        tree.insert(210, "210");
        // then
        assertThat(tree, isTree(
                newTree(
                        newNode(keys(81),
                                nodes(
                                        newNode(
                                                keys(30, 50),
                                                nodes(
                                                        newNode(
                                                                keys(15, 25),
                                                                nodes(
                                                                        newLeaf(keys(5, 10, 12, 14), values("5", "10", "12", "14")),
                                                                        newLeaf(keys(16, 18, 20, 22), values("16", "18", "20", "22")),
                                                                        newLeaf(keys(26, 28), values("26", "28"))
                                                                )
                                                        ),
                                                        newNode(
                                                                keys(35, 45),
                                                                nodes(
                                                                        newLeaf(keys(30, 32, 34), values("30", "32", "34")),
                                                                        newLeaf(keys(36, 38, 40, 44), values("36", "38", "40", "44")),
                                                                        newLeaf(keys(45, 46, 47, 48), values("45", "46", "47", "48"))
                                                                )
                                                        ),
                                                        newNode(
                                                                keys(60, 75),
                                                                nodes(
                                                                        newLeaf(keys(51, 53, 55), values("51", "53", "55")),
                                                                        newLeaf(keys(61, 63, 65, 68), values("61", "63", "65", "68")),
                                                                        newLeaf(keys(76, 78, 79), values("76", "78", "80"))
                                                                )
                                                        )
                                                )
                                        ),
                                        newNode(
                                                keys(95, 200),
                                                nodes(
                                                        newNode(
                                                                keys(83, 88),
                                                                nodes(
                                                                        newLeaf(keys(80, 81, 82), values("80", "81", "82")),
                                                                        newLeaf(keys(83, 84, 85, 86), values("83", "84", "85", "86")),
                                                                        newLeaf(keys(88, 89, 90), values("88", "89", "90"))
                                                                )
                                                        ),
                                                        newNode(
                                                                keys(96, 110), // Full inner node
                                                                nodes(
                                                                        newLeaf(keys(91, 92, 93), values("91", "92", "93")),
                                                                        newLeaf(keys(99, 100, 101, 102), values("99", "100", "101", "102")),
                                                                        newLeaf(keys(115, 116, 117), values("115", "116", "117"))

                                                                )
                                                        ),
                                                        newNode(
                                                                keys(200, 210),
                                                                nodes(
                                                                        newLeaf(keys(160, 165, 169), values("160", "165", "169")),
                                                                        newLeaf(keys(200, 209), values("200","209")),
                                                                        newLeaf(keys(210, 220, 230), values("210","220", "230"))
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
        )));
    }

    ///// Deletion tests

    @Test
    public void deleteFromLeaf() {
        // given
        tree = newTree(newLeaf(keys(1, 2, 3), values("a", "b", "c")));
        // when
        String value = tree.delete(2);
        // then
        assertThat(value, is("b"));
        assertThat(tree, isTree(
                newTree(newLeaf(keys(1, 3), values("a", "c")))));
    }

    @Test
    public void deleteFromChild() {
        // given
        tree = newTree(newNode(
                keys(4), nodes(newLeaf(keys(1, 2, 3), values("a", "b", "c")),
                               newLeaf(keys(4, 5), values("d", "e")))));
        // when
        String value = tree.delete(1);
        // then
        assertThat(value, is("a"));
        assertThat(tree, isTree(newTree(newNode(
                keys(4), nodes(newLeaf(keys(2, 3), values("b", "c")),
                               newLeaf(keys(4, 5), values("d", "e")))))));
    }

    @Test
    public void deleteFromChildStealFromSibling() {
        // given
        tree = newTree(newNode(
                keys(3), nodes(newLeaf(keys(1, 2), values("a", "b")),
                               newLeaf(keys(3, 4, 5), values("c", "d", "e")))));
        // when
        String value = tree.delete(1);
        // then
        assertThat(value, is("a"));
        assertThat(tree, isTree(newTree(newNode(
                keys(4), nodes(newLeaf(keys(2, 3), values("b", "c")),
                               newLeaf(keys(4, 5), values("d", "e")))))));

    }

    @Test
    public void deleteFromChildMergeWithSibling() {
        // given
        tree = newTree(newNode(keys(3, 5),
                               nodes(newLeaf(keys(1, 2), values("a", "b")),
                                     newLeaf(keys(3, 4), values("c", "d")),
                                     newLeaf(keys(5, 6), values("e", "f")))));
        // when
        String value = tree.delete(2);
        // then
        assertThat(value, is("b"));
        assertThat(tree, isTree(newTree(newNode(
                keys(5), nodes(newLeaf(keys(1, 3, 4), values("a", "c", "d")),
                               newLeaf(keys(5, 6), values("e", "f")))))));
    }

}
