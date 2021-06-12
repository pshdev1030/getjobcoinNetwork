package Server;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;

public class BlockChain {
    private Block latestBlock; //블록체인 가장 끝에 있는 블록
    private ArrayList<Transaction> transactions;
    private int size; //블록체인의 블록 개수

    public BlockChain() {
        transactions = new ArrayList<>();
        size = 0;
    }

    public synchronized void setLatestBlock(Block latestBlock) {
        this.latestBlock = latestBlock;
    }
    public synchronized void setSize(int size) {
        this.size = size;
    }
    public synchronized void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public synchronized Block getLatestBlock() {
        return latestBlock;
    }
    public synchronized int getSize() {
        return size;
    }
    public synchronized ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public synchronized boolean addTransaction(String txString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String[] tokens = txString.split("\\|");
        if (tokens.length != 5) {
            return false;
        }
        if (!tokens[0].equals("TX")) {
            return false;
        }
        Transaction transaction = new Transaction();
        transaction.setRecipient(tokens[1]);
        transaction.setContent(tokens[2]);
        transaction.setSender(tokens[3]);
        transaction.setSignature(Base64.getDecoder().decode(tokens[4]));
        if (!transaction.isValid()) {
            return false;
        }
        transactions.add(transaction);
        return true;
    }

    public synchronized boolean publish(int nonce) {
        if (transactions.size() == 0) { //블록에 기재할 트랜잭션 없으면 commit 안함
            return false;
        }

        Block newBlock = new Block();
        if (latestBlock == null) {
            newBlock.setPreviousHash(new byte[32]); //AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        }
        else {
            newBlock.setPreviousHash(latestBlock.calculateHash());
        }

        newBlock.setTransactions(transactions);
        byte[] hash = newBlock.calculateHashWithNonce(nonce);
        String hashString = Base64.getEncoder().encodeToString(hash);
        if(hashString.startsWith("A")) {
            newBlock.setPreviousBlock(latestBlock);
            latestBlock = newBlock;
            transactions = new ArrayList<>(); //src.Server.Transaction 목록 초기화
            size += 1;
            return true;
        }
        return false;
    }

    public synchronized String toString() {
        String cutOffRule = new String(new char[113]).replace("\0", "-") + "\n";
        String poolString = "";
        for (Transaction tx : transactions) {
            poolString += tx.toString();
        }

        String blockString = "";
        Block bl = latestBlock;
        while (bl != null) {
            blockString += bl.toString();
            bl = bl.getPreviousBlock();
        }

        return "Server.BlockChain:\n"
                + cutOffRule
                + poolString
                + cutOffRule
                + blockString;
    }
}