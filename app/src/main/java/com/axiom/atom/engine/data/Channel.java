package com.axiom.atom.engine.data;

/**
 * Очередь для многопоточной работы
 * Эта очередь реализована чтобы решить проблему доступа к элементам очереди
 * при многопоточном доступе и при том чтобы работать на API 16 (без forEach)
 * @param <T>
 */
public class Channel<T> {

    public static final int MIN_CAPACITY = 100;
    private int front, rear, capacity;
    private Object[] queue;

    public Channel() {
        this(MIN_CAPACITY);
    }

    public Channel(int size) {
        capacity = size;
        front = 0;
        rear = 0;
        queue = new Object[capacity];
    }

    public boolean add(T element) {
        if (rear==capacity) return false;
        synchronized (this) {
            queue[rear] = element;
            rear++;
            return true;
        }
    }


    public T poll() {
        if (front==rear) return null;
        synchronized (this) {
            Object element = queue[front];
            if (rear - 1 >= 0)
                System.arraycopy(queue, 1, queue, 0, rear - 1);
            rear--;
            return (T) element;
        }
    }

    public T get(int index) {
        if (front==rear) return null;
        synchronized (this) {
            if (index<0 || index>=rear) return null;
            return (T) queue[index];
        }
    }

    public T peek() {
        if (front==rear) return null;
        synchronized (this) {
            Object element = queue[front];
            return (T) element;
        }
    }


    public void remove(T element) {
        if (front==rear) return;
        synchronized (this) {
            for (int i=0; i<rear; i++) {
                if (queue[i]==element) {
                    if (rear - 1 - i >= 0)
                        System.arraycopy(queue, i + 1, queue, i, rear - 1 - i);
                    rear--;
                    break;
                }
            }
        }
    }

    public void clear() {
        rear = 0;
    }

    public int remainingCapacity() {
        synchronized (this) {
            return capacity - rear;
        }
    }

    public int size() {
        synchronized (this) {
            return rear;
        }
    }

}
