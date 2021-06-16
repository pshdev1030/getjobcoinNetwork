package Client;

import Server.Peer;
import Threads.MessageSenderRunnable;

import java.security.Security;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        //remoteHost, remotePort
        if (args.length != 2) {
            return;
        }

        int remotePort = 0;
        String remoteHost = null;

        try {
            remoteHost = args[0];
            remotePort = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return;
        }

        try {
            Wallet myWallet = new Wallet();
            Peer peer = new Peer(remoteHost, remotePort);
            Scanner sc = new Scanner(System.in);
            while (true) {
                String message = sc.nextLine();
                if(message.equals("EXIT")){
                    break;
                }
                else if(message.equals("KEY")){
                    System.out.println("내 지갑 주소 : " + myWallet.getPublicKeyString());
                    continue;
                }
                new Thread(new MessageSenderRunnable(peer, message, myWallet)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}