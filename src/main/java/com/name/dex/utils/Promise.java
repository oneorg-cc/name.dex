package com.name.dex.utils;

import java.util.ArrayList;
import java.util.List;

public class Promise<V> {

    public static class CancelledException extends Exception {}

    //

    private enum State {
        PENDING, SUCCEED, FAILED, CANCELLED
    }

    //

    public interface Resolver<V> {
        void resolve(V value);
    }

    public interface Rejector {
        void reject(Exception exception);
    }

    public interface Resolvable<V> {
        void call(Resolver<V> resolver, Rejector rejector) throws Exception;
    }

    //

    private State state;
    private final Object lock;

    private final Thread resolvableThread;
    private final Thread lockThread;

    private V value;
    private Exception exception = null;

    private final List<Resolver<V>> whenResolvedCallbacks = new ArrayList<>();
    private final List<Rejector> whenRejectedCallbacks = new ArrayList<>();

    //

    public Promise(Resolvable<V> resolvable) {
        this.state = State.PENDING;
        this.lock = new Object();

        this.resolvableThread = new Thread(() -> {
            try {
                resolvable.call(value -> {
                    if(State.PENDING.equals(this.state)) {
                        this.value = value;
                        this.unlock(State.SUCCEED);
                    }
                }, exception -> {
                    if(State.PENDING.equals(this.state)) {
                        this.exception = exception;
                        this.unlock(State.FAILED);
                    }
                });
            } catch(Exception e) {
                if(State.PENDING.equals(this.state)) {
                    this.exception = e;
                    this.unlock(State.FAILED);
                }
            }
        });

        this.lockThread = new Thread(() -> {
            try { this.lock(); }
            catch (InterruptedException e) { this.exception = e; }

            if(State.CANCELLED.equals(this.state))
                this.exception = new CancelledException();

            switch(this.state) {
                case SUCCEED -> {
                    for(Resolver<V> resolver : whenResolvedCallbacks)
                        resolver.resolve(this.value);
                }
                case FAILED, CANCELLED -> {
                    for(Rejector rejector : whenRejectedCallbacks)
                        rejector.reject(this.exception);
                }
            }
        });

        this.lockThread.start();
        this.resolvableThread.start();

    }

    //

    public boolean pending() { return State.PENDING.equals(this.state); }
    public boolean succeed() { return State.SUCCEED.equals(this.state); }
    public boolean failed() { return State.FAILED.equals(this.state); }
    public boolean cancelled() { return State.CANCELLED.equals(this.state); }

    //

    private void lock() throws InterruptedException {
        synchronized (lock) {
            this.lock.wait();
        }
    }

    private void unlock(State state) {
        synchronized (lock) {
            this.state = state;
            this.lock.notify();
        }
    }

    //

    public Promise<V> whenResolved(Resolver<V> resolver) {
        this.whenResolvedCallbacks.add(resolver);
        return this;
    }

    public Promise<V> whenRejected(Rejector rejector) {
        this.whenRejectedCallbacks.add(rejector);
        return this;
    }

    //

    public void synchronize() throws InterruptedException {
        this.lockThread.join();
    }

    public V await() {
        try {
            this.synchronize();

            if (State.SUCCEED.equals(this.state))
                return this.value;

            throw this.exception;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //

    public void cancel() {
        this.resolvableThread.interrupt();
        this.unlock(State.CANCELLED);
    }

}
