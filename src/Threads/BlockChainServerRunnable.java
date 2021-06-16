package Threads;

import Server.Block;
import Server.BlockChain;
import Server.Peer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;


public class BlockChainServerRunnable implements Runnable{
    private Socket clientSocket; //본 서버와 연결된 노드 소켓
    private BlockChain blockchain;
    private HashMap<Peer, Date> serverStatus;
    String remoteIP;
    private int localPort;

    public BlockChainServerRunnable(Socket clientSocket, BlockChain blockchain, HashMap<Peer, Date> serverStatus, int localPort) {
        this.clientSocket = clientSocket;
        this.blockchain = blockchain;
        this.serverStatus = serverStatus;
        this.localPort = localPort;
    }

    public void run() {
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        	PrintWriter outWriter = new PrintWriter(clientSocket.getOutputStream(), true);

        	while (true) {
        		String inputLine = inputReader.readLine();
        		if (inputLine == null) {
        			break;
        		}
				//Client의 메시지 읽기
        		String[] tokens = inputLine.split("\\|");
        		switch (tokens[0]) {
        			case "TX":
        			case "PRINT":
        				this.serverHandler(inputLine, outWriter, tokens);
        				
        			case "HB":
        			case "SI":
        				this.heartBeatHandler(inputReader, inputLine, tokens);
        			
        			case "IS":
        				if (this.infoHandler(inputLine, tokens)) {
        					break;
        				}
        				
        			case "CU":
        				if (tokens[0].equals("CU")) {
        					this.catchUpHandler(tokens);
        					break;
        				}
        				
        			default:
                       	outWriter.print("Error\n\n");
                       	outWriter.flush();
        		}
        	}
            clientSocket.close();
        } catch (IOException e) {
        }
    }

//Request handlers//-----------------------

	//Catch-up(데이터 갱신하기)
	public synchronized void catchUpHandler(String[] tokens) {
		try (ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream())){

			//cu만 명령받은 경우 -> 가장 끝 블록을 공유해줌
			if (tokens.length == 1) {
				outStream.writeObject(blockchain.getLatestBlock());
				outStream.flush();
				return;
			}
			else {
			//CU|<해쉬주소> 의 경우
				Block cur = blockchain.getLatestBlock(); //가장 끝 블록
				while (true) {
					if (Base64.getEncoder().encodeToString(cur.calculateHash()).equals(tokens[1])) { //해쉬주소와 일치하는 블록 리턴
						outStream.writeObject(cur);
						outStream.flush();
						return;
					}
					if (cur == null) {
						break;
					}
					cur = cur.getPreviousBlock();
				}
				outStream.writeObject(cur);
				outStream.flush();
			}
		} catch (Exception e) { }
	}

	//Information Signal
	//IS| remotePort | chain length | Hash값
    public synchronized boolean infoHandler(String inputLine, String[] tokens) {

    	try {
    		String encodedHash;
    		if (blockchain.getLatestBlock() != null) { //블록체인에 값이 있는 경우
    			byte[] latestHash = blockchain.getLatestBlock().calculateHash();
    			encodedHash = Base64.getEncoder().encodeToString(latestHash);
    		} else { //블록체인이 비어있는 경우
    			encodedHash = "null";
    		}

			//본 서버 블록체인의 최신 해시값이 (추가하도록) 요청 받은 해시값과 같거나, 요청 받은 해시값이 짧은 경우(난이도가 더 낮은 경우)
			//또는 메시지에 적힌 체인 길이보다 본 서버의 체인 길이가 같거나 길다면 데이터 갱신을 하지 않는다.
    		if (this.blockchain.getSize() > Integer.parseInt(tokens[2]) ||
					this.blockchain.getSize() == Integer.parseInt(tokens[2]) && tokens[3].length() < encodedHash.length()){
    			return true;
    		}
    		else{ //데이터 갱신을 해야하는 경우
    			//새 연결 생성
    			String remoteIP = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
    			Socket s;
    			s = new Socket(remoteIP, Integer.parseInt(tokens[1]));
    			PrintWriter outWriter;
    			outWriter = new PrintWriter(s.getOutputStream(), true);
    			
    			//갱신 시키기
    			ArrayList<Block> blocks = new ArrayList<Block>();
    			outWriter.println("CU"); // 다른 노드에게 최신 블록 요청하기
    			outWriter.flush();
				ObjectInputStream inputStream;
				inputStream = new ObjectInputStream(s.getInputStream());
    			Block b = (Block) inputStream.readObject(); // 요청한 블록 저장
    			
    			inputStream.close();
    			s.close();
    			blocks.add(b);
    			String prevHash = Base64.getEncoder().encodeToString(b.getPreviousHash());

    			while (!prevHash.startsWith("A")) { //헤드 블럭까지 복사
    				s = new Socket(remoteIP, Integer.parseInt(tokens[1]));
    				outWriter = new PrintWriter(s.getOutputStream(), true);

    				outWriter.println("CU|" + prevHash);
    				outWriter.flush();
    				
    				inputStream = new ObjectInputStream(s.getInputStream());

    				b = (Block) inputStream.readObject();
    				inputStream.close();
    				s.close();
    				blocks.add(b);
    				prevHash = Base64.getEncoder().encodeToString(b.getPreviousHash());
    			}
    			this.blockchain.setLatestBlock(blocks.get(0));
    			this.blockchain.setSize(blocks.size());
    			
    			Block cur = this.blockchain.getLatestBlock();
    			for (int i = 0; i < blocks.size(); i++) {
    				if (i < blocks.size() - 1) {
    					cur.setPreviousBlock(blocks.get(i + 1));
    				}
    				else {
    					cur.setPreviousBlock(null);
    				}
    				cur = cur.getPreviousBlock();
    			}			
    			
    			return false;
    		}
    	
    	} catch (Exception e) { }

    	return false;
    }

    public synchronized void serverHandler(String inputLine, PrintWriter outWriter, String[] tokens) {
        try {
            switch (tokens[0]) {
            	//TX|수신 지갑 주소|Content|송신 지갑 주소
                case "TX":
                    if (this.blockchain.addTransaction(inputLine))
                        outWriter.print("Accepted\n\n");
                    else
                        outWriter.print("Rejected\n\n");
                        outWriter.flush();
                        break;
                case "PRINT": //블록체인 콘솔에 찍기
                    outWriter.print(blockchain.toString() + "\n");
                    System.out.println(blockchain.toString() + "\n");
                    outWriter.flush();
                    break;
                default:
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    //각 노드의 피어리스트를 갱신시켜줌
    public void heartBeatHandler(BufferedReader bufferedReader, String line, String[] tokens) {
        try {	
            String remoteIP = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
            	
            Peer serverInQuestion;
            switch (tokens[0]) {
                case "HB":
                	serverInQuestion = new Peer(remoteIP, Integer.parseInt(tokens[1]));

                	if (!serverStatus.containsKey(serverInQuestion)) {
                		String forwardMessage = "SI|" + localPort + "|" + remoteIP + "|" + tokens[1];
                    	this.broadcastHB(forwardMessage, new ArrayList<Peer>());
                	}
                		
                	serverStatus.put(serverInQuestion, new Date());
                	this.removeUnresponsive(); //반응 없는 노드 피어리스트에서 삭제
            			
                case "SI":
                	serverInQuestion = new Peer(tokens[2], Integer.parseInt(tokens[3]));
                	Peer originator = new Peer(remoteIP, Integer.parseInt(tokens[1]));
                		
                	if (!serverStatus.containsKey(serverInQuestion)) {
                    	ArrayList<Peer> exempt = new ArrayList<Peer>();
                    	exempt.add(originator);
                    	exempt.add(serverInQuestion);
                    	String relayMessage = "SI|" + localPort + "|" + tokens[2] + "|" + tokens[3];
                    	this.broadcastHB(relayMessage, exempt);
                	}
                		
                	serverStatus.put(serverInQuestion, new Date());
                	serverStatus.put(originator, new Date());
                	this.removeUnresponsive(); //반응 없는 노드 피어리스트에서 삭제
                    	
                default:     
            }
        } catch (Exception e) {
    	}
    }

//Helper Functions//--------------------------

	//4초 내에 응답하지 않은 서버는 피어 리스트에서 삭제
    public void removeUnresponsive() {
        for (Peer server: serverStatus.keySet()) {
            if (new Date().getTime() - serverStatus.get(server).getTime() > 4000) {
            	serverStatus.remove(server);
            	System.out.println("removed " + server.getHost());
            }
        }
    }

    //연결된 서버(노드)들에게 HeartBeatMSG 전송
    public void broadcastHB(String message, ArrayList<Peer> exempt) {
    	ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
    	for (Peer info: this.serverStatus.keySet()) {
            if (!exempt.contains(info)) {
                Thread thread = new Thread(new Threads.MessageSenderRunnable(info, message));
                thread.start();
                threadArrayList.add(thread);
            }
        }
        
        for (int i = 0; i < threadArrayList.size(); i++) {
            try {
            	threadArrayList.get(i).join();
            } catch (InterruptedException e) { }
        }
    }
}

