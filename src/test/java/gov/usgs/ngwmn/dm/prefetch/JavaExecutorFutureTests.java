package gov.usgs.ngwmn.dm.prefetch;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JavaExecutorFutureTests {

	final static Set<ThreadPoolExecutor> executors = new HashSet<ThreadPoolExecutor>();


	@Before
	public void before() {
		System.out.println();
		System.out.println();
		System.out.println("=============================================");
		System.out.println("=============================================");
	}

	private static class Interruptor implements Callable<String> {
		private final Thread threadToInterrupt;

		Interruptor(Thread threadToInterrupt) {
			this.threadToInterrupt = threadToInterrupt;
		}

		@Override
		public String call() throws Exception {
			System.out.println("Interrupter started");
			Thread.sleep(1000);
			threadToInterrupt.interrupt();
			System.out.println("Interrupter finished");
			return "interrupted other thread";
		}
	}



	private static class SlowCallable implements Callable<String> {
		boolean before,after,error,finished;

		final String id;
		SlowCallable(String id) {
			this.id = id;
		}

		@Override
		public String call() throws Exception {
			try {
				before = true;
				System.out.println("slow started: " + id);
				Thread.sleep(5000);
				after  = true;
			} catch (InterruptedException e) {
				// just to see if it allows threads to shutdown
				error = true;
				String msg = toString() + " interrupted";
				System.out.println(msg);
				return msg;
			}
			finished = true;
			//File.createTempFile("foo", "bar");
			String msg = toString() + " finished";
			System.out.println(msg);
			return msg;
		}

		@Override
		public String toString() {
			return "SlowCallable [id=" + id
					+ ", before=" + before + ", after=" + after
					+ ", error=" + error + ", finished=" + finished + "]";
		}

	}

	private static class InterruptingPoolExecutor extends ThreadPoolExecutor {
		ThreadPoolExecutor interruptor;

		public InterruptingPoolExecutor() {
			this(1);
			executors.add(this);
		}

		public InterruptingPoolExecutor(int poolSize) {
			// max time does not effect the number of running threads
			super(poolSize, poolSize, 100L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
			interruptor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
			executors.add(interruptor);
		}

		@Override
		protected void beforeExecute(Thread t, Runnable r) {
			System.out.println("before interrupt");
			/*Future<String> two = */interruptor.submit( new Interruptor(t) );
		}

		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			System.out.println("after interrupt");
		};
	}




	@Test
	public void futureGetInterruptedWithCleanupTest() throws Exception {
		int initialThreads = java.lang.Thread.activeCount();

		ThreadPoolExecutor executor = new InterruptingPoolExecutor();
		executors.add(executor);
		SlowCallable slow           = new SlowCallable("futureGetInterruptedTest");
		Future<String> one          = executor.submit(slow);

		int startedThreads = java.lang.Thread.activeCount() - initialThreads;
		System.out.println( "started threads: " + startedThreads );
		// should be 2 but asynchronous execution can make this line run first so no test

		try {
			System.out.println( one.get() );
		} catch (InterruptedException e) {
			System.out.println("One was interrupted");
		}
		int remainingThreads = java.lang.Thread.activeCount() - initialThreads;
		System.out.println( "remaining threads: " + remainingThreads );
		Assert.assertEquals("now it should be two because enough time has passed", 2, remainingThreads);

		System.out.println("slow:" + slow.toString());
		System.out.println("one done:" + one.isDone());
		System.out.println("one cancelled:" + one.isCancelled());

		System.out.println("executor count:" + executors.size());

		for (ThreadPoolExecutor exe : executors) {
			if (!exe.isShutdown() || !exe.isTerminated() || !exe.isTerminating()  || exe.getActiveCount()>0) {
				System.out.println("shutting down executor threads: " +exe.getActiveCount());
				exe.shutdown();
			}
			if (!exe.isShutdown() || !exe.isTerminated() || !exe.isTerminating() || exe.getActiveCount()>0) {
				System.out.println("executor did not shutdown: waiting 3 sec");
				while ( ! exe.awaitTermination(3, TimeUnit.SECONDS) ) {
					System.out.println("executor did not shutdown: waiting another 3 sec");
				}
			}
		}

		long sleepTime = System.currentTimeMillis() + 5001;
		while (sleepTime > System.currentTimeMillis()) {
			try {
				Thread.sleep(5001);
			} catch (InterruptedException e) {
				// force a longer sleep
				System.out.println("sleep interrupt with time left: " + (sleepTime - System.currentTimeMillis()));
			}
		}
		int waitThreads = java.lang.Thread.activeCount() - initialThreads;
		System.out.println( "wait threads: " + waitThreads );
		Assert.assertEquals("Expect remaining threads cleaned up implemented", 0, waitThreads);
	}


	@Test
	public void futureGetInterruptedTest() throws Exception {
		int initialThreads = java.lang.Thread.activeCount();

		ThreadPoolExecutor executor = new InterruptingPoolExecutor();
		SlowCallable slow           = new SlowCallable("futureGetInterruptedTest");
		Future<String> one          = executor.submit(slow);

		int startedThreads = java.lang.Thread.activeCount() - initialThreads;
		System.out.println( "started threads: " + startedThreads );
		// should be 2 but asynchronous execution can make this line run first so no test

		try {
			System.out.println( one.get() );
		} catch (InterruptedException e) {
			System.out.println("One was interrupted");
		}
		int remainingThreads = java.lang.Thread.activeCount() - initialThreads;
		System.out.println( "remaining threads: " + remainingThreads );
		Assert.assertEquals("now it should be two because enough time has passed", 2, remainingThreads);

		System.out.println("slow:" + slow.toString());
		System.out.println("one done:" + one.isDone());
		System.out.println("one cancelled:" + one.isCancelled());

		long sleepTime = System.currentTimeMillis() + 5001;
		while (sleepTime > System.currentTimeMillis()) {
			try {
				Thread.sleep(5001);
			} catch (InterruptedException e) {
				// force a longer sleep
				System.out.println("sleep interrupt with time left: " + (sleepTime - System.currentTimeMillis()));
			}
		}
		int waitThreads = java.lang.Thread.activeCount() - initialThreads;
		System.out.println( "wait threads: " + waitThreads );
		Assert.assertEquals("Expect remaining threads until clean up implemented", 2, waitThreads);
	}




	private static class InterruptExecutorMaker implements Callable<String> {
		ThreadPoolExecutor executor = new InterruptingPoolExecutor();

		final String id;
		InterruptExecutorMaker(String id) {
			this.id = id;
		}

		@Override
		public String call() throws Exception {
			SlowCallable slow    = new SlowCallable(id+"-fromExeMkr");
			/*Future<String> one =*/ executor.submit(slow);
			return id+": slow:"+slow.toString();
		}
	}
	@Test
	public void loopInterruptExecutorCreatorTest() throws Exception {

		int initialThreads = java.lang.Thread.activeCount();
		System.out.println("initial thread count: " + initialThreads);
		for (int i=0; i<100; i++) {
			System.out.println( new InterruptExecutorMaker("loop_"+i).call() );
		}

		int loopThreads = java.lang.Thread.activeCount() - initialThreads;
		System.out.println("loop threads started: " + loopThreads);
		// it might be a bit less because of asynchronous start times
		Assert.assertTrue("expect nearly 200 new threads", 190 <= loopThreads);

		long sleepTime = System.currentTimeMillis() + 5001;
		while (sleepTime > System.currentTimeMillis()) {
			try {
				Thread.sleep(5001);
			} catch (InterruptedException e) {
				// force a longer sleep
				System.out.println("sleep interrupt with time left: " + (sleepTime - System.currentTimeMillis()));
			}
		}

		int waitThreads = java.lang.Thread.activeCount() - initialThreads;
		System.out.println("end thread count after 5 sec: " + waitThreads);
		// by now it should be exactly 200 because of long enough wait
		Assert.assertEquals("expect 200 threads still running until proper cleanup", 200, waitThreads);
	}

	private static class ExecutorMaker implements Callable<String> {
		ThreadPoolExecutor executor =  (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

		final String id;
		ExecutorMaker(String id) {
			this.id = id;
			executors.add(executor);
		}

		@Override
		public String call() throws Exception {
			SlowCallable slow  = new SlowCallable(id+"-fromExeMkr");
			/*Future<String> one =*/ executor.submit(slow);
			return id+": slow:"+slow.toString();
		}
	}
	@Test
	public void loopExecutorCreatorTest() throws Exception {

		int initialThreads = java.lang.Thread.activeCount();
		System.out.println("initial thread count: " + initialThreads);
		for (int i=0; i<100; i++) {
			System.out.println( new ExecutorMaker("loop_"+i).call() );
		}

		int loopThreads = java.lang.Thread.activeCount() - initialThreads;
		System.out.println("loop threads started: " + loopThreads);
		// it might be a bit less because of asynchronous start times
		Assert.assertTrue("expect nearly 100 new threads but was " + loopThreads, 100 <= loopThreads);

		long sleepTime = System.currentTimeMillis() + 5001;
		while (sleepTime > System.currentTimeMillis()) {
			try {
				Thread.sleep(5001);
			} catch (InterruptedException e) {
				// force a longer sleep
				System.out.println("sleep interrupt with time left: " + (sleepTime - System.currentTimeMillis()));
			}
		}

		int waitThreads = java.lang.Thread.activeCount() - initialThreads;
		System.out.println("end thread count after 5 sec: " + waitThreads);
		// by now it should be exactly 100 because of long enough wait
		Assert.assertEquals("expect 100 threads still running until proper cleanup", 100, waitThreads);
	}






	//	private static class ExecutorMakerWithCleanup implements Callable<String> {
	//		ExecutorService executor = Executors.newSingleThreadExecutor();
	//
	//		final String id;
	//		ExecutorMakerWithCleanup(String id) {
	//			this.id = id;
	//		}
	//
	//		@Override
	//		public String call() throws Exception {
	//			try {
	//				SlowCallable slow        = new SlowCallable(id+"-fromExeMkrClean");
	//				Future<String> one       = executor.submit(slow);
	//				Future<String> two       = executor.submit( new Interruptor(Thread.currentThread()) );
	//			} catch (Exception e) {
	//				executor.shutdownNow();
	//				throw e;
	//			}
	//			return "finished ExecutorMaker";
	//		}
	//	}

	@Test
	public void loopExecutorCreatorWithProperCleanupTest() throws Exception {

		int initialThreads = java.lang.Thread.activeCount();
		System.out.println("initial thread count: " + initialThreads);

		for (int i=0; i<100; i++) {
			new ExecutorMaker(i+"").call();
			System.out.println(" thread count: " + java.lang.Thread.activeCount());
		}

		int loopThreads = java.lang.Thread.activeCount() - initialThreads;
		System.out.println("loop thread count: " + loopThreads);
		Assert.assertEquals(100, loopThreads);

		for (ThreadPoolExecutor exe : executors) {
			if (!exe.isShutdown() || !exe.isTerminated() || !exe.isTerminating()) {
				System.out.println("shutting down executor");
				exe.shutdown();
			}
			if (!exe.isShutdown() || !exe.isTerminated() || !exe.isTerminating()) {
				System.out.println("executor did not shutdown");
			}
		}

		long sleepTime = System.currentTimeMillis() + 5001;
		while (sleepTime > System.currentTimeMillis()) {
			try {
				Thread.sleep(5001);
			} catch (InterruptedException e) {
				// force a longer sleep
				System.out.println("sleep interrupt with time left: " + (sleepTime - System.currentTimeMillis()));
			}
		}
		int waitThreads = java.lang.Thread.activeCount() - initialThreads;
		System.out.println("end thread count after 5 sec: " + waitThreads);
		// if this test is the only one run then it would be 0
		// if the other tests are run and the number is <0 then it properly shutdown all interrupted as well
		System.out.println("active thread count: " + java.lang.Thread.activeCount() );
		Assert.assertTrue("expect all threads shutdown by now with proper shutdown", waitThreads < 1);

		for (ThreadPoolExecutor exe : executors) {
			if (!exe.isShutdown() || !exe.isTerminated() || !exe.isTerminating()) {
				System.out.println("shutting down NOW executor");
				exe.shutdownNow();
			}
			if (!exe.isShutdown() || !exe.isTerminated() || !exe.isTerminating()) {
				System.out.println("executor did not shutdown");
				exe.awaitTermination(5, TimeUnit.SECONDS);
			}
		}

		System.out.println("active thread count after another 5 sec: " + java.lang.Thread.activeCount() );
		Assert.assertTrue("expect all threads shutdown by now with proper shutdown", java.lang.Thread.activeCount() < 10);
	}
}