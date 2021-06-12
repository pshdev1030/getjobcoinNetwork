package Threads;

import Server.Peer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class HeartBeatReceiverRunnable implements Runnable{

    private Socket toClient;
    private HashMap<Peer, Date> serverStatus;
    private int localPort;

    public HeartBeatReceiverRunnable (Socket toClient, HashMap<Peer, Date> serverStatus, int localPort) {
        this.toClient = toClient;
        this.serverStatus = serverStatus;
        this.localPort = localPort;
    }

    @Override
    public void run() {
        try {
			heartBeatServerHandler(toClient.getInputStream());
			toClient.close();
        } catch (IOException e) {
    	}
    }
    
    public void heartBeatServerHandler(InputStream clientInputStream) {
    	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientInputStream));
        
        try {
        	while (true) {
            	String line = bufferedReader.readLine();
            	if (line == null) {
            		break;
            	}
            	
            	String[] tokens = line.split("\\|");
            	String remoteIP = (((InetSocketAddress) toClient.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
				System.out.println("RECEIVED : " + tokens[0] + " Operation");
            	
            	Peer serverInQuestion;
            	switch (tokens[0]) {
                	case "HB":
                		serverInQuestion = new Peer(remoteIP, Integer.parseInt(tokens[1]));
                		
                		if (!serverStatus.containsKey(serverInQuestion)) {
                			String forwardMessage = "SI|" + localPort + "|" + remoteIP + "|" + tokens[1];
                    		this.broadcast(forwardMessage, new ArrayList<Peer>());
                		}
                		
                		serverStatus.put(serverInQuestion, new Date());
                		this.removeUnresponsive();
            			
                	case "SI":
                		serverInQuestion = new Peer(tokens[2], Integer.parseInt(tokens[3]));
                		Peer originator = new Peer(remoteIP, Integer.parseInt(tokens[1]));
                		
                		if (!serverStatus.containsKey(serverInQuestion)) {
                    		ArrayList<Peer> exempt = new ArrayList<Peer>();
                    		exempt.add(originator);
                    		exempt.add(serverInQuestion);
                    		String relayMessage = "SI|" + localPort + "|" + tokens[2] + "|" + tokens[3];
                    		this.broadcast(relayMessage, exempt);
                		}
                		
                		serverStatus.put(serverInQuestion, new Date());
                		serverStatus.put(originator, new Date());
                		this.removeUnresponsive();
                    	
                	default:
            	}
			}
        } catch (Exception e) {
    	}
    }
    
    public void removeUnresponsive() {
    	//check for servers that havent responded in 4 secs
        for (Peer server: serverStatus.keySet()) {
            if (new Date().getTime() - serverStatus.get(server).getTime() > 4000) {
            	serverStatus.remove(server);
            	System.out.println("removed " + server.getHost());
            }
        }
    }
    
    public void broadcast(String message, ArrayList<Peer> exempt) {
    	ArrayList<Thread> threadArrayList = new ArrayList<Thread>();
    	for (Peer info: this.serverStatus.keySet()) {
            if (!exempt.contains(info)) {
                Thread thread = new Thread(new MessageSenderRunnable(info, message));
                thread.start();
                threadArrayList.add(thread);
            }
        }
        
        for (int i = 0; i < threadArrayList.size(); i++) {
            try {
            	threadArrayList.get(i).join();
            } catch (InterruptedException e) {
            }
        }
    }
}
