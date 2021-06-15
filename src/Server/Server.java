package Server;

import Threads.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Security;
import java.util.Date;
import java.util.HashMap;

public class Server {

    public static void main(String[] args) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        //localPort, remoteHost(시드 IP), remotePort
        if (args.length != 3) {
            return;
        }

        int localPort = 0;
        int remotePort = 0;
        String remoteHost = null;

        try{
            localPort = Integer.parseInt(args[0]);
            remoteHost = args[1];
            remotePort = Integer.parseInt(args[2]);
        }
        catch(NumberFormatException e){ return;}
        System.out.println("Start on " + localPort);
        BlockChain blockchain = new BlockChain();
        HashMap<Peer, Date> serverStatus = new HashMap<Peer, Date>();
        serverStatus.put(new Peer(remoteHost, remotePort), new Date());

        //주기적으로 트랜잭션 목록을 확인하고 블록 생성
        PublishRunnable pRunnable = new PublishRunnable(blockchain);
        Thread pThread = new Thread(pRunnable);
        pThread.start();
        //주기적으로 Heartbeat msg를 보냄(모든 노드에게)
        new Thread(new HeartBeatRunnable(serverStatus, localPort)).start();
        //주기적으로 다른 노드들에게 블록체인 데이터 동기화
        new Thread(new CatchupRunnable(blockchain, serverStatus, localPort)).start();

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(localPort);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new BlockChainServerRunnable(clientSocket, blockchain, serverStatus, localPort)).start(); //본 서버와 연결된 소켓에서 오는 메시지 및 명령을 처리하기 위한 스레드
                new Thread(new HeartBeatReceiverRunnable(clientSocket, serverStatus, localPort)).start(); // HeartBeat MSG 전문 수신 스레드
            }
        }
        catch (IllegalArgumentException e) { }
        catch (IOException e) { }
        finally {
            try {
                pRunnable.setRunning(false);
                pThread.join();
                if (serverSocket != null){
                    serverSocket.close();
                }
            }
            catch (IOException e) { }
            catch (InterruptedException e) { }
        }
    }
}