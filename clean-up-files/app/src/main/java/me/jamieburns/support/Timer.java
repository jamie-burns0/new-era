package me.jamieburns.support;

public class Timer<T> {

    private long start;
    private long duration;
    private String as;

    public Timer( Class<T> client ) {
        this(client.getSimpleName());
    }

    public Timer( String as ) {
        this.as = as;
    }

    public Timer<T> as( String as ) {
        this.as = as;
        return this;
    }

    public Timer<T> start() {
        start = System.currentTimeMillis();
        return this;
    }

    public void stop() {
        duration = System.currentTimeMillis() - start;
        System.out.println( this );
    }

    @Override
    public String toString() {
        return "[%s took %s ms]".formatted( as, duration );
    }
}
