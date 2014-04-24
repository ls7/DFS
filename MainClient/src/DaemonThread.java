
public abstract class DaemonThread implements Runnable{
	/**
     * Whether or not this thread is active.
     */
    private boolean active = false;

    /**
     * The interval in seconds to run this thread
     */
    private int interval = -1;

    /**
     * The name of this pool (for loggin/display purposes).
     */
    //private String name;

    /**
     * This instance's thread
     */
    private Thread runner;
    
    /**
     * Construct a new interval thread that will run on the given interval
     * with the given name.
     *
     * @param intervalSeconds the number of seconds to run the thread on
     * @param name            the name of the thread
     */
    public DaemonThread(int intervalSeconds) {

        this.interval = intervalSeconds * 1000;
      //  this.name = name;
    }

    
        public void start() {

        active = true;

        //If we don't have a thread yet, make one and start it.
        if (runner == null && interval > 0) {
            runner = new Thread(this);
            runner.start();
        }
    }

    /**
     * Not for public use.  This thread is automatically
     * started when a new instance is retrieved with the getInstance method.
     * Use the start() and stop() methods to control the thread.
     *
     * @see Thread#run()
     */
    public void run() {

        //Make this a relatively low level thread
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        System.out.println("Starting monitoring...");
        //Pause this thread for the amount of the interval
        while (active) {
            try {
                doInterval();
                Thread.sleep(interval);

            } catch (InterruptedException e) {
                //Ignore
            }
        }
    }
    
    /**
     * The interval has expired and now it's time to do something.
     */
    protected abstract void doInterval();
    
    
}