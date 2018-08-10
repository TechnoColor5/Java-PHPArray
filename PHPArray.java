/**
 * @author Daniel Mailloux
 * @version v1.0
 *
 * Main PHPArray class that holds most of the required functionality
 * of a PHP Array data structure.
*/

import java.util.*;

public class PHPArray<V> implements Iterable<V>{
	private int N;		//number of items
	private int M;		//size
	private PHPnode<V> table[];
	private PHPnode<V> root;
	private PHPnode<V> tail;
	private PHPnode<V> currNode;
	private PHPnode<V> prevNode;

	public PHPArray(int size) {
		M = size;
		N = 0;
		table = (PHPnode<V>[]) new PHPnode<?>[M];
	}
	//Places an item into the Array
	public void put(Object k, V val) {
		String key = k.toString();		//Turns the object into a string

		if ( N >= M / 2) {
	 		resize(2 * M);
	 	}

		int i;
		//if the spot is empty, place it, if not go to the next spot
		for (i = hash(key); table[i] != null; i = (i + 1) % M) {
			Pair<V> pair = table[i].pair;
			if (pair.key.equals(key)){	//if key is found, replace the value
				pair.value = val;
				return;
			}
		}

		table[i] = new PHPnode<V>(key, val);		//if spot is empty, it makes a new node
		if (N == 0) {	//if this is the first node, make the root
			root = table[i];
			tail = root;
			currNode = root;
			prevNode = null;
		}
		else {	//otherwise make the next node
			tail.setNext(table[i]);
			table[i].setPrev(tail);
			tail = table[i];
		}

		N++;
	}

	//Mirror of put() except it allows the next and prev nodes to be
	//sent in as well
	public void reHash(Object k, V val, PHPnode prev, PHPnode next) {
		String key = k.toString();


		if ( N >= M / 2) {
	 		resize(2 * M);
	 	}

		int i;
		for (i = hash(key); table[i] != null; i = (i + 1) % M) {
			if (table[i].pair.key.equals(key)){	//if key is found, replace the value
				table[i].pair.value = val;
				return;
			}
		}
		//Places the node inbetween next and prev
		table[i] = new PHPnode<V>(key, val);		//if spot is empty, it makes a new node
		table[i].setNext(next);
		table[i].setPrev(prev);
		next.setPrev(table[i]);
		prev.setNext(table[i]);
		N++;
	}

	//Resizes the array if N >= M/2
	public void resize(int newCapacity) {
		PHPArray<V> temp = new PHPArray<V>(newCapacity);
		PHPnode<V> node = root;
		System.out.println("\t Size "+ N + " -- Resizing from " + M + " to "+ newCapacity);
		while(node != null) {
			temp.put(node.pair.key, node.pair.value);
			node = node.next;
		}
		//Have to reassign these values
		tail = temp.tail;
		root = temp.root;
		table = temp.table;
		currNode = root;
		prevNode = null;
		M = temp.M;
	}

	//Removes the node that matches the key that is passed through
	public void unset(Object k) {
		String key = k.toString();

		//if key doesn't exist
		if (get(key) == null) {
			return;
		}

		int i = hash(key);
		while (!key.equals(table[i].pair.key)) {
			i = (i + 1 ) % M;
		}
		PHPnode prev = table[i].prev;
		PHPnode next = table[i].next;

		//If the null is the root
		if (prev == null) {
			root = next;
		}
		else prev.setNext(next);

		//If the node is the tail
		if (next == null) {
			tail = prev;
		}
		else next.setPrev(prev);

		//deletes is the node
		table[i] = null;

		if(prev != null)
	  	N--;  
	  
	  // rehash all keys in same cluster
	  i = (i + 1) % M;
	  while (table[i] != null) {
	  	Object keyToHash = table[i].pair.key;
	  	V valToHash = table[i].pair.value;
	  	PHPnode reHashPrev = table[i].prev;
	  	PHPnode reHashNext = table[i].next;
	  	table[i] = null;
	  	System.out.println("Key "+keyToHash + " rehashed...\n");

	  	N--;
	  	
	  	reHash(keyToHash, valToHash, reHashPrev, reHashNext);

	  	i = (i + 1) % M;
	  }
	}

	//Hashes the key
	public int hash(String k) {
	return ((k.hashCode() & 0x7fffffff) % M);
	}

	//Uses a key to return the corresponding value
	public V get(Object k) {
		String key = k.toString();

		for (int i = hash(key); table[i] != null; i = (i + 1) % M){
			if (table[i].pair.key.equals(key)){
				return table[i].pair.value;
			}
		}
		return null;
	}

	//Returns the next Pair<V> in the array
	public Pair<V> each() {
		Pair<V> temp;
		if (currNode != null) {
			temp = currNode.pair;
			if (currNode.prev != null)
				prevNode = currNode.prev;
			currNode = currNode.next;
		}
		else return null;
		return temp;
	}

	//Returns the number of items in the array
	public int length() {
		return N;
	}

	//Resets currNode to the root
	public void reset(){
		currNode = root;
		prevNode = null;
	}

	//Returns an iterator
	public Iterator<V> iterator() {
		return new PHPArrayIterator();
	}

	//Returns the ArrayList of all the keys
	public ArrayList<String> keys() {
		ArrayList<String> keys = new ArrayList<String>();
		PHPnode node = root;
		while (node != null) {
			keys.add(node.pair.key);
			node = node.next;
		}
		return keys;
	}

	//Returns an ArrayList of all the Values
	public ArrayList<V> values() {
		ArrayList<V> values = new ArrayList<V>();
		//Able to use a for each loop becuase the iterator returns the value
		for (V x : this) {
			values.add(x);
		}
		return values;
	}

	//Prints out a hash table
	public void showTable() {
		System.out.println("\t Raw Hash Table Contents: \n");
		for (int i = 0; i < M; i++) {
			System.out.print(i+": ");
			if(table[i] == null)
				System.out.print("null\n");
			else System.out.print("Key: "+ table[i].pair.key + " Value: " + table[i].pair.value + "\n");
		}
	}

	//Sorts all of the items in the array
	public void sort() {
		//If V is not comparable, throw an exception
		if(!(root.pair.value instanceof Comparable)) {
			throw new ClassCastException();
		} else {

			ArrayList<V> vals = values();
			PHPArray<V> temp = new PHPArray<V>(M);
			vals = mergeSort(vals);
			for (int i = 0; i < length() - 1; i++) {
					V inputVal = vals.get(i);
					temp.put(i, inputVal);
				}
			tail = temp.tail;
			root = temp.root;
			table = temp.table;
			M = temp.M;
			N = temp.N;
		}
	}

	//Sorts the array while preserving the keys for each value
	public void asort() {
		if(!(root.pair.value instanceof Comparable)) {
			throw new ClassCastException();
		} else {
			ArrayList<Pair<V>> pairs = new ArrayList<Pair<V>>();

			//This gets all of the Pairs and puts them in an ArrayList to be sorted
			PHPnode<V> curr = root;
			while (curr != null) {
				pairs.add(curr.pair);
				curr = curr.next;
			}


			PHPArray<V> temp = new PHPArray<V>(M);

			pairs = mergeASort(pairs);
			for (int i = 0; i < length(); i++) {
					Pair<V> inputPair = pairs.get(i);
					temp.put(inputPair.key, inputPair.value);
				}
				tail = temp.tail;
				root = temp.root;
				table = temp.table;
				M = temp.M;
				N = temp.N;
		}
	}

	//Conducts a simple mergeSort on the ArrayList
	public ArrayList<V> mergeSort(ArrayList<V> values) {
		ArrayList<V> left = new ArrayList<V>();
		ArrayList<V> right = new ArrayList<V>();
		int mid = values.size()/2;

		if (values.size() == 1) {
			return values;
		} else {
			for (int i = 0; i < mid; i++) {
				left.add(values.get(i));
			}
			for (int i = mid; i <values.size(); i++) {
				right.add(values.get(i));
			}

			left = mergeSort(left);
			right = mergeSort(right);

			values = merge(values, left, right);
		}
		return values;
	}

	//Merges the arrayLists together
	public ArrayList<V> merge(ArrayList<V> values, ArrayList<V> left, ArrayList<V> right) {
		int l = 0;
		int r = 0;
		int v = 0;

		while (l < left.size() && r < right.size()) {
			Comparable leftObj = (Comparable) left.get(l);
			Comparable rightObj = (Comparable) right.get(r);
			if (leftObj.compareTo(rightObj) < 0) {
				values.set(v, left.get(l));
				l++;
			} else {
				values.set(v, right.get(r));
				r++;
			}
			v++;
		}

		ArrayList<V> remainder;
		int remainderIndex;
		if (l >= left.size()) {
			remainder = right;
			remainderIndex = r;
		} else {
			remainder = left;
			remainderIndex = l;
		}

		for (int i = remainderIndex; i < remainder.size(); i++) {
			values.set(v, remainder.get(i));
			v++;
		}

		return values;
	}

	//MergeSort for asort()
	public ArrayList<Pair<V>> mergeASort(ArrayList<Pair<V>> values) {
		ArrayList<Pair<V>> left = new ArrayList<Pair<V>>();
		ArrayList<Pair<V>> right = new ArrayList<Pair<V>>();
		int mid = values.size()/2;

		if (values.size() == 1) {
			return values;
		} else {
			for (int i = 0; i < mid; i++) {
				left.add(values.get(i));
			}
			for (int i = mid; i < values.size(); i++) {
				right.add(values.get(i));
			}

			left = mergeASort(left);
			right = mergeASort(right);

			values = amerge(values, left, right);
		}
		return values;
	}

	//Merge fucntion for asort()
	public ArrayList<Pair<V>> amerge(ArrayList<Pair<V>> values, ArrayList<Pair<V>> left, ArrayList<Pair<V>> right) {
		int l = 0;
		int r = 0;
		int v = 0;

		while (l < left.size() && r < right.size()) {
			Comparable leftObj = (Comparable) left.get(l).value;
			Comparable rightObj = (Comparable) right.get(r).value;
			if (leftObj.compareTo(rightObj) < 0) {
				values.set(v, left.get(l));
				l++;
			} else {
				values.set(v, right.get(r));
				r++;
			}
			v++;
		}

		ArrayList<Pair<V>> remainder;
		int remainderIndex;
		if (l >= left.size()) {
			remainder = right;
			remainderIndex = r;
		} else {
			remainder = left;
			remainderIndex = l;
		}

		for (int i = remainderIndex; i < remainder.size(); i++) {
			values.set(v, remainder.get(i));
			v++;
		}

		return values;
	}

	//Flips all of the keys and values
	public PHPArray<String> array_flip() {
		PHPArray<String> flipped = new PHPArray<String>(M);
		if (!(root.pair.value instanceof String)){
			throw new ClassCastException("Cannot convert class "+root.pair.value.getClass()+" to String");
		} else {
			PHPnode curr = root;
			while (curr != null) {
				flipped.put(curr.pair.value, curr.pair.key);
				curr = curr.next;
			}
		}
		return flipped;
	}

	//Private inner class that iterates through the linked list for the iterator() method
	class PHPArrayIterator implements Iterator<V>{
		PHPnode curr = root;

		public boolean hasNext() {
			return (curr != null);
		}

		public V next() {
			PHPnode<V> temp = curr;

			if(hasNext())
				curr = curr.next;
			return temp.pair.value;
		}
	}

	//Pair class that holds the Value and Key for each item
	public static class Pair<V> {
		public String key;
		public V value;

		public Pair(String k, V val) {
			key = k;
			value = val;
		}
	}

	//Node that holds the next and prev nodes and a Pair
	private static class PHPnode<V> {
		public Pair<V> pair;
		public PHPnode next;
		public PHPnode prev;

		public PHPnode (String k, V val) {
			pair = new Pair<V>(k, val);
		}

		public void setNext(PHPnode n){
			next = n;
		}

		public void setPrev(PHPnode n){
			prev = n;
		}
	}

	//Backs up currNode to the most recent node
	public V prev() {
		currNode = prevNode;
		if (currNode == root) {
			prevNode = null;
		} else {
			if (prevNode != null) {
				prevNode = currNode.prev;
			}
		}
		if (currNode == null){
			return null;
		} else return currNode.pair.value;
	}

}