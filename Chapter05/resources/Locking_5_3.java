package Chapter05.resources;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Locking_5_3 {

    /*================================================================================
     * Smelly code
     *===============================================================================*/
    Lock lock = new ReentrantLock();

    protected void setLock(final Lock mock) {
        lock = mock;
    }

    public void doOp1() {
        lock.lock();
        try {
            System.out.println("doOp1");
        } finally {
            lock.unlock();
        }
    }


    /*================================================================================
     * 排他制御をラムダ式のExecute Around Methodパターンで実現するコード
     *===============================================================================*/
    public static void runLocked(Lock lock, Runnable block) {
        lock.lock();

        try {
            block.run();
        } finally {
            lock.unlock();
        }
    }

    public void doOp2() {
        runLocked(lock, () -> {
            System.out.println("doOp2");
        });
    }

    public void doOp3() {
        runLocked(lock, () -> {
            System.out.println("doOp3");
        });
    }

    public void doOp4() {
        runLocked(lock, () -> {
            System.out.println("doOp4");
        });
    }

    public static void main(final String[] args) throws IOException, InterruptedException {
        Locking_5_3 lock = new Locking_5_3();
        lock.doOp1();
        lock.doOp2();
        lock.doOp3();
        lock.doOp4();
    }
}