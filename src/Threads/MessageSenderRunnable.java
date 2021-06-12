package Threads;

import Client.Wallet;
import Server.Peer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Base64;

//메세지 전송 스레드
public class MessageSenderRunnable implements Runnable{

    private Peer destServer;
    private String message;
    private Wallet myWallet;

    public MessageSenderRunnable(Peer destServer, String message) {
        this.destServer = destServer;
        this.message = message;
    }

    public MessageSenderRunnable(Peer destServer, String message, Wallet wallet) {
        this.destServer = destServer;
        this.message = message;
        this.myWallet = wallet;
    }

    @Override
    public void run() {
        try {
            // timeout 10초 Socket 생성
            Socket s = new Socket();
            s.connect(new InetSocketAddress(this.destServer.getHost(), this.destServer.getPort()), 10000);
            PrintWriter pw =  new PrintWriter(s.getOutputStream(), true);
            
            System.out.println("SEND TO " + this.destServer.getHost() + ":" + this.destServer.getPort() + " -> " + message);
            String[] tokens = message.split("\\|");

            //메시지 전송
            if(myWallet!=null && tokens[0].equals("TX")){
                byte[] signature = myWallet.generateSignature(myWallet.getPrivateKey(), myWallet.getPublicKeyString(), tokens[1], tokens[2]);
                String encoded = Base64.getEncoder().encodeToString(signature);
                pw.println(message + "|" + myWallet.getPublicKeyString() + "|" + encoded);
            }
            else{
                pw.println(message);
            }
        	pw.flush();

            // OutputStream 및 Socket Close
            pw.close();
            s.close();
            
        } catch (IOException e) {
        }
    }
}