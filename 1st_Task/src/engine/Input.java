package engine;

import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class Input {
    private final LinkedBlockingQueue<String> q = new LinkedBlockingQueue<>();
    private final Scanner scanner = new Scanner(System.in);
    private volatile boolean running = true;
    private final Thread worker = new Thread(this::run, "input-thread");

    public void start() {
        worker.setDaemon(true); // 프로세스 종료 시 함께 정리
        worker.start();
    }

    private void run() {
        while (running) {
            try {
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine();
                q.offer(line.trim());
            } catch (Exception e) {
                break;
            }
        }
    }

    // 타임아웃 없이 한 줄 읽기
    public String takeLine() throws InterruptedException {
        return q.take();
    }

    // 타임아웃 내 한 줄 읽기 (없으면 null)
    public String pollLine(long timeout, TimeUnit unit) throws InterruptedException {
        return q.poll(timeout, unit);
    }

    public void clear() {
        q.clear();
    }

    public void stop() {
        running = false;
    }
}
