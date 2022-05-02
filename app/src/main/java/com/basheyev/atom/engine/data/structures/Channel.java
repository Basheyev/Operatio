package com.basheyev.atom.engine.data.structures;

import java.io.Serializable;

/**
 * Очередь для многопоточной работы и доступа к элементам по индексу
 * @param <T>
 */
public class Channel<T> implements Serializable {

    public static final int MIN_CAPACITY = 64;
    private final Object[] queue;
    private final int front, capacity;
    private int rear;

    public Channel() {
        this(MIN_CAPACITY);
    }

    public Channel(int size) {
        capacity = size;
        front = 0;
        rear = 0;
        queue = new Object[capacity];
    }

    public synchronized boolean push(T element) {
        if (rear==capacity || element==null) return false;
        queue[rear] = element;
        rear++;
        return true;
    }

    @SuppressWarnings("unchecked")
    public synchronized T poll() {
        if (front==rear) return null;
        Object element = queue[front];
        if (rear - 1 >= 0) System.arraycopy(queue, 1, queue, 0, rear - 1);
        rear--;
        return (T) element;
    }

    // fixme по идее доступ по индексу - это плохой дизайн так как количество может меняться
    @SuppressWarnings("unchecked")
    public synchronized T get(int index) {
        if (front==rear) return null;
        if (index<0 || index>=rear) return null;
        return (T) queue[index];
    }

    @SuppressWarnings("unchecked")
    public synchronized T peek() {
        if (front==rear) return null;
        Object element = queue[front];
        return (T) element;
    }

    public synchronized boolean remove(T element) {
        if (front==rear) return false;
        for (int i=0; i<rear; i++) {
            if (queue[i]==element) {
                if (rear - 1 - i >= 0)
                    System.arraycopy(queue, i + 1, queue, i, rear - 1 - i);
                rear--;
                break;
            }
        }
        return true;
    }

    public synchronized void clear() {
        rear = 0;
    }

    public synchronized int remainingCapacity() {
        return capacity - rear;
    }

    public synchronized int size() {
        return rear;
    }

}
