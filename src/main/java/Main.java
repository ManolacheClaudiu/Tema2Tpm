import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class AddThread extends Thread {
    OptimisticList<Integer> list;

    public AddThread(OptimisticList<Integer> list) {
        this.list = list;
    }

    public void run() {
        for(int i =1; i<=100000;i++){
            var x = new Random().nextInt();
            list.add(x);
        }
    }
}
class RemoveThread extends Thread {
    OptimisticList<Integer> list;

    public RemoveThread(OptimisticList<Integer> list) {
        this.list = list;
    }

    public void run() {
        for(int i =1; i<=100000;i++){
            var x = new Random().nextInt();
            list.remove(x);
        }
    }
}
class ContainsThread extends Thread {
    OptimisticList<Integer> list;

    public ContainsThread(OptimisticList<Integer> list) {
        this.list = list;
    }

    public void run() {
        for(int i =1; i<=100000;i++){
            var x = new Random().nextInt();
            list.contains(x);
        }
    }
}

public class Main {

    public static void main(String[] args) {
        var optimisticList = new OptimisticList<Integer>();
        for( int i = 1; i<=100000; i++){
            optimisticList.add(i);
        }
        var startTime = System.currentTimeMillis();
//        var thread1 = new AddThread(optimisticList);
//        var thread2 = new AddThread(optimisticList);
//        var thread3 = new AddThread(optimisticList);
//        var thread4 = new AddThread(optimisticList);

//        var thread1 = new RemoveThread(optimisticList);
//        var thread2 = new RemoveThread(optimisticList);
//        var thread3 = new RemoveThread(optimisticList);
//        var thread4 = new RemoveThread(optimisticList);

        var thread1 = new ContainsThread(optimisticList);
        var thread2 = new ContainsThread(optimisticList);
        var thread3 = new ContainsThread(optimisticList);
        var thread4 = new ContainsThread(optimisticList);
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        var  stopTime = System.currentTimeMillis();
        System.out.println(stopTime-startTime);
        System.out.println();
        System.out.println(startTime);
        System.out.println();
        System.out.println(stopTime);

    }
}

class OptimisticList<T> {
    /**
     * First list entry
     */
    private Node head;
    /**
     * Constructor
     */
    private int version;

    public OptimisticList() {
        this.head  = new Node(Integer.MIN_VALUE);
        this.head.next = new Node(Integer.MAX_VALUE);
        this.version = 0;
    }
    /**
     * Add an element.
     * @param item element to add
     * @return true iff element was not there already
     */
    public boolean add(T item) {
        int key = item.hashCode();
        while (true) {
            int traversingVersion = version;
            Node pred = this.head;
            Node current = pred.next;
            while (current.key < key) {
                pred = current; current = current.next;
            }
            pred.lock(); current.lock();
            try {
                if (validate(traversingVersion)) {
                    if (current.key == key) { // present
                        return false;
                    } else {               // not present
                        Node entry = new Node(item);
                        entry.next = current;
                        pred.next = entry;
                        version++;
                        return true;
                    }
                }
            } finally {                // always unlock
                pred.unlock(); current.unlock();
            }
        }
    }

    /**
     * Remove an element.
     * @param item element to remove
     * @return true iff element was present
     */
    public boolean remove(T item) {
        int key = item.hashCode();
        while (true) {
            int traversingVersion = version;
            Node pred = this.head;
            Node current = pred.next;
            while (current.key < key) {
                pred = current; current = current.next;
            }
            pred.lock(); current.lock();
            try {
                if (validate(traversingVersion)) {
                    if (current.key == key) { // present in list
                        pred.next = current.next;
                        return true;
                    } else {               // not present in list
                        return false;
                    }
                }
            } finally {                // always unlock
                pred.unlock(); current.unlock();
            }
        }
    }

    /**
     * Test whether element is present
     * @param item element to test
     * @return true iff element is present
     */
    public boolean contains(T item) {
        int key = item.hashCode();
        while (true) {
            int traversingVersion = version;
            Node pred = this.head; // sentinel node;
            Node current = pred.next;
            while (current.key < key) {
                pred = current; current = current.next;
            }
            try {
                pred.lock(); current.lock();
                if (validate(traversingVersion)) {
                    return (current.key == key);
                }
            } finally {                // always unlock
                pred.unlock(); current.unlock();
            }
        }
    }


    private boolean validate(int traversingVersion) {
        return traversingVersion == version;
        /*Node entry = head;
        while (entry.key <= pred.key) {
            if (entry == pred)
                return pred.next == current;
            entry = entry.next;
        }
        return false;*/
    }

    /**
     * list node
     */
    private class Node {
        /**
         * actual item
         */
        T item;
        /**
         * item's hash code
         */
        int key;
        /**
         * next node in list
         */
        Node next;
        /**
         * Synchronizes node.
         */
        Lock lock;
        /**
         * Constructor for usual node
         * @param item element in list
         */
        Node(T item) {
            this.item = item;
            this.key = item.hashCode();
            lock = new ReentrantLock();
        }
        /**
         * Constructor for sentinel node
         * @param key should be min or max int value
         */
        Node(int key) {
            this.key = key;
            lock = new ReentrantLock();
        }
        /**
         * Lock entry
         */
        void lock() {lock.lock();}
        /**
         * Unlock entry
         */
        void unlock() {lock.unlock();}
    }
}
