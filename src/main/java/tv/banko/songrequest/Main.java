package tv.banko.songrequest;

import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        new SongRequest();
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(TimeUnit.DAYS.toMillis(1));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(1);
            }
        }).start();
    }

}
