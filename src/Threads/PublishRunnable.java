package Threads;

import Server.BlockChain;

import java.security.SecureRandom;

public class PublishRunnable implements Runnable{

    private volatile boolean isRunning;
    private int nonce;
    private BlockChain blockchain;
    private SecureRandom randomGenerator;

    public PublishRunnable(BlockChain blockchain) {
        isRunning = true;
        this.blockchain = blockchain;
        randomGenerator = new SecureRandom();
        nonce = randomGenerator.nextInt();
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public boolean getRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        while (isRunning) {
            blockchain.publish(nonce);
            nonce = randomGenerator.nextInt();
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {

            }
        }
    }
}
