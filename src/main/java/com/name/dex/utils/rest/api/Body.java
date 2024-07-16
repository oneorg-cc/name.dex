package com.name.dex.utils.rest.api;

import com.name.dex.utils.Callback;
import com.name.dex.utils.Promise;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class Body {

    public static class InputAlreadyPipedWithBodyException extends Exception {}

    public static class OutputAlreadyPipedWithBodyException extends Exception {}

    public static class InputNotPipedWithBodyException extends Exception {}

    public static class OutputNotPipedWithBodyException extends Exception {}

    public static class AlreadyLockedException extends Exception {}

    public static class NotLockedException extends Exception {}

    //

    public enum Usage {
        READABLE, WRITEABLE
    }

    //

    public static Body readOnly(Body body) {
        return wrap(body, Set.of(Usage.READABLE));
    }

    public static Body writeOnly(Body body) {
        return wrap(body, Set.of(Usage.WRITEABLE));
    }

    public static Body wrap(Body body, Set<Usage> usages) {
        return new Body(body, usages);
    }

    //

    private final PipedOutputStream out;
    private final PipedInputStream in;

    private final BufferedWriter writer;
    private final BufferedReader reader;

    private final Set<Usage> usages;

    private final Map<OutputStream, Thread> pipedOut = new HashMap<>();
    private final Map<InputStream, Thread> pipedIn = new HashMap<>();

    private final Object syncLocking = new Object();
    private Promise lockPromise = null;
    private Promise.Resolver lockResolver = null;

    //

    public Body() throws IOException {
        this.usages = Set.of(Usage.values());

        this.out = new PipedOutputStream();
        this.in = new PipedInputStream(this.out);

        this.writer = new BufferedWriter(new OutputStreamWriter(this.out()));
        this.reader = new BufferedReader(new InputStreamReader(this.in()));
    }

    private Body(Body body, Set<Usage> usages) {
        this.usages = usages;

        if(this.usages.contains(Usage.READABLE)) {
            this.in = body.in;
            this.reader = body.reader;
        }
        else {
            this.in = null;
            this.reader = null;
        }

        if(this.usages.contains(Usage.WRITEABLE)) {
            this.out = body.out;
            this.writer = body.writer;
        }
        else {
            this.out = null;
            this.writer = null;
        }
    }

    //

    public OutputStream out() { return this.out; }
    public InputStream in() { return this.in; }

    public BufferedWriter writer() { return this.writer; }
    public BufferedReader reader() { return this.reader; }

    //

    public boolean isWritable() { return this.usages.contains(Usage.WRITEABLE); }
    public boolean isReadable() { return this.usages.contains(Usage.READABLE); }

    public boolean isReadonly() { return this.isReadable() && !this.isWritable(); }
    public boolean isWriteonly() { return this.isWritable() && !this.isReadable(); }

    //

    public boolean locked() {
        synchronized(syncLocking) {
            return this.lockPromise != null && this.lockResolver != null;
        }
    }

    public void lock() throws AlreadyLockedException {
        synchronized(syncLocking) {
            if(this.locked()) {
                throw new AlreadyLockedException();
            }

            this.lockPromise = new Promise<Object>((resolver, rejector) -> {
                this.lockResolver = resolver;
            });
        }
    }

    public void unlock() throws NotLockedException {
        synchronized(syncLocking) {
            if(!this.locked()) {
                throw new NotLockedException();
            }
            this.lockResolver.resolve(null);

            this.lockPromise = null;
            this.lockResolver = null;
        }
    }

    public void waitUnlock() {
        if(this.locked()) {
            this.lockPromise.await();
        }
    }

    public void lockNwait() throws AlreadyLockedException {
        this.lock();
        this.waitUnlock();
    }

    //

    public boolean pipedFrom(InputStream in) {
        return this.pipedIn.containsKey(in);
    }

    public boolean pipedInto(OutputStream out) {
        return this.pipedOut.containsKey(out);
    }

    public void pipeFrom(InputStream in) throws InputAlreadyPipedWithBodyException {
        if(this.pipedFrom(in))
            throw new InputAlreadyPipedWithBodyException();

        Thread thread = new Thread(() -> this.pipeSyncFrom(in));

        this.pipedIn.put(in, thread);
        thread.start();
    }

    public void pipeInto(OutputStream out) throws OutputAlreadyPipedWithBodyException {
        if(this.pipedInto(out))
            throw new OutputAlreadyPipedWithBodyException();

        Thread thread = new Thread(() -> this.pipeSyncInto(out));

        this.pipedOut.put(out, thread);
        thread.start();
    }

    public void pipeSyncFrom(InputStream in) {
        try(in; OutputStream out = this.out()) {
            for(int b; (b = in.read()) > -1; )
                out.write(b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void pipeSyncInto(OutputStream out) {
        try(InputStream in = this.in(); out) {
            for(int b; (b = in.read()) > -1; )
                out.write(b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void unpipe(InputStream in) throws InputNotPipedWithBodyException {
        if(!this.pipedFrom(in))
            throw new InputNotPipedWithBodyException();

        this.pipedIn.get(in).interrupt();
        this.pipedIn.remove(in);
    }

    public void unpipe(OutputStream out) throws OutputNotPipedWithBodyException {
        if(!this.pipedInto(out))
            throw new OutputNotPipedWithBodyException();

        this.pipedOut.get(out).interrupt();
        this.pipedOut.remove(out);
    }

}
