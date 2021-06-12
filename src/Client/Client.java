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

        //TX, PRINT, HB, SI, IS, CU 의 명령어가 존재함

        //TX => 트랜잭션을 블록체인에 추가하는 명령어
        // BlockChainClient에서
        // TX|받는 사람(영어4자리+숫자4자리)|Content(String 아무거나) 입력

        //PRINT => 블록체인 정보 출력

        // HB => 시드 노드에게 HB 메시지 보내기(?)
        // HB|remotePort

        // SI => 피어(노드) 리스트에 IP를 새롭게 추가하는 명령어
        // SI|remotePort|피어에 추가할 IP|피어에 추가할 IP의 포트

        // IS => 블록체인 데이터 갱신을 위해 사용하는 명령어
        // IS|remotePort|블록 길이값|블록해시값

        // CU => 블록체인 정보 얻어오기 명령어
        // CU 혹은 CU|블록해시값 으로 명령어 입력
        // CU 단독으로 명령어 입력 시, 블록 가장 끝에 달려있는 블록을 출력
        // CU|블록해시값 으로 명령어 입력 시, 블록체인을 순차적으로 탐색하여 대상 해시값을 갖는 블록을 출력
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