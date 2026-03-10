import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class ThreadLimitTest {

	public static void main(String[] args) {
		var threadCount = new AtomicInteger(0);

		Thread monitor = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(60000); // 1 minuto
					System.out.println("Threads creados hasta el momento: " + threadCount.get());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		});
		monitor.setDaemon(true);
		monitor.start();

		try {
			while (true) {
				var thread = new Thread(() -> {
					threadCount.incrementAndGet();
					LockSupport.park();
				});
				thread.start();
			}
		} catch (OutOfMemoryError error) {
			System.out.println("Se alcanzó el límite de hilos: " + threadCount);
			error.printStackTrace();
		}
	}
}
