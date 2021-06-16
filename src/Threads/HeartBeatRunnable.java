package Threads;


import Server.Peer;

import java.util.Date;
import java.util.HashMap;

public class HeartBeatRunnable implements Runnable {

    private HashMap<Peer, Date> peerList;
    private int seqNum;
    private int localPort;

    public HeartBeatRunnable(HashMap<Peer, Date> peerList, int localPort) {
        this.peerList = peerList;
        this.seqNum = 0;
        this.localPort = localPort;
    }

    @Override
    public void run() {
    	String message;
        while(true) {
            // 모든 노드들에게 msg 전송
            message = "HB|" + localPort + "|" + seqNum;

            for (Peer info : peerList.keySet()) {
                Thread thread = new Thread(new HeartBeatSenderRunnable(info, message));
                thread.start();
            }

            // increment the sequenceNumber
            seqNum += 1;

            //2초 대기
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) { }
        }
    }
}
