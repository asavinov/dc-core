package com.conceptoriented.com;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// Copied from here: https://code.google.com/p/yet-another-tree-structure/
public class TreeNode<T> implements Iterable<TreeNode<T>> {

	public T item;
	public TreeNode<T> parent;
	public List<TreeNode<T>> children;

	public boolean isRoot() {
		return parent == null;
	}
    public TreeNode<T> getRoot() { 
	    TreeNode<T> node = this; 
	    while (node.parent != null) node = node.parent; 
	    return node;
    }

	public boolean isLeaf() {
		return children.size() == 0;
	}

	private List<TreeNode<T>> elementsIndex;

	public TreeNode() {
		this(null);
		this.item = (T) (Object) this;
	}

	public TreeNode(T item) {
		this.item = item;
		this.children = new LinkedList<TreeNode<T>>();
		this.elementsIndex = new LinkedList<TreeNode<T>>();
		this.elementsIndex.add(this);
	}

	public TreeNode<T> addChild(T child) {

		TreeNode<T> childNode = null;

		if (child instanceof TreeNode<?>) {
			childNode = (TreeNode<T>) child;
			childNode.parent = this;

		} else {
			childNode = new TreeNode<T>(child);
			childNode.parent = this;
		}

		this.children.add(childNode);

		this.registerChildForSearch(childNode);

		return childNode;
	}

	public int getLevel() {
		if (this.isRoot())
			return 0;
		else
			return parent.getLevel() + 1;
	}

	private void registerChildForSearch(TreeNode<T> node) {
		elementsIndex.add(node);
		if (parent != null)
			parent.registerChildForSearch(node);
	}

	public TreeNode<T> findTreeNode(Comparable<T> cmp) {
		for (TreeNode<T> element : this.elementsIndex) {
			T elItem = element.item;
			if (cmp.compareTo(elItem) == 0)
				return element;
		}

		return null;
	}

	@Override
	public String toString() {
		return item != null ? item.toString() : "[item null]";
	}

	@Override
	public Iterator<TreeNode<T>> iterator() {
		// Commented because additional class is needed: // Copied from here:
		// https://code.google.com/p/yet-another-tree-structure/
		// TreeNodeIter<T> iter = new TreeNodeIter<T>(this);
		// return iter;
		return null;
	}

}
