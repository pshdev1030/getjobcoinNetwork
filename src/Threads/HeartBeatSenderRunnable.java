package Threads;


import Server.Peer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class HeartBeatSenderRunnable implements Runnable{

    private Peer destServer; //시드 IP를 포함한 블록체인 네트워크의 모든 노드
    private String message; //PeriodicHeartBeatRunnable에서 파라미터로 입력한 메시지

    public HeartBeatSenderRunnable(Peer destServer, String message) {
        this.destServer = destServer;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            //3초당 소켓 하나를 만들어 메시지 전송
            Socket s = new Socket();
            s.connect(new InetSocketAddress(this.destServer.getHost(), this.destServer.getPort()), 3000);
            PrintWriter pw =  new PrintWriter(s.getOutputStream(), true);
            
            //메시지 입력 후 flush
        	pw.println(message);
        	pw.flush();

            //소켓의 writer close
            pw.close();
            //2초 대기
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) { }

            //소켓 close
            s.close();
        } catch (IOException e) { }
    }
}