package Threads;


import Server.BlockChain;
import Server.Peer;

import java.util.*;

public class CatchupRunnable implements Runnable {
	private BlockChain blockchain;
	private HashMap<Peer, Date> peerList;
	private int localPort;
	
	public CatchupRunnable(BlockChain blockchain, HashMap<Peer, Date> peerList, int localPort) {
		this.blockchain = blockchain;
		this.peerList = peerList;
		this.localPort = localPort;
	}
	
	@Override
	public void run() {
		while(true) {
			String InfoMsg = "IS|" + localPort + "|" + blockchain.getSize() + "|";
			if (blockchain.getLatestBlock() != null){
				byte[] latestHash = blockchain.getLatestBlock().calculateHash();
				if (latestHash != null){
					InfoMsg += Base64.getEncoder().encodeToString(latestHash);
				}
				else{
					InfoMsg += "null";
				}
			}
			else{
				InfoMsg += "null";
			}
			 

			//메시지를 뿌릴 대상
			if (peerList.size() <= 5) {
				this.broadcast(InfoMsg);
			}
			else {
				//네트워크 참여자가 5명이 넘어가면 랜덤으로 5명을 골라서 보냄 (과부하를 막기 위함)
				ArrayList<Peer> targetPeers = new ArrayList<Peer>();
				ArrayList<Peer> allPeers = new ArrayList(peerList.keySet());
				for (int i = 0; i < 5; i++) {
					Collections.shuffle(allPeers);
					targetPeers.add(allPeers.remove(0));
				}
				this.broadcast2(targetPeers, InfoMsg);
			}

			//2초 대기
			try {
				Thread.sleep(2000);
			} catch (Exception e) { }
		}
	}

	//메시지 전체 뿌리기
    public void broadcast(String message) {
    	ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
    	for (Peer info: this.peerList.keySet()) {
            Thread thread = new Thread(new MessageSenderRunnable(info, message));
            thread.start();
            threadArrayList.add(thread);
        }
    }

    //피어리스트 5명 초과시에 사용하는 메소드
    public void broadcast2(ArrayList<Peer> toPeers, String message) {
    	ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
    	for (int i = 0; i < toPeers.size(); i++) {
    		Thread thread = new Thread(new MessageSenderRunnable(toPeers.get(i), message));
    		thread.start();
    		threadArrayList.add(thread);
    	}
    }
}