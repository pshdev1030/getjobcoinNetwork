package Server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;

public class Block implements Serializable {

    private transient Block previousBlock;
    private byte[] previousHash;
    private byte[] currentHash;
    private ArrayList<Transaction> transactions;

    public Block() {
        transactions = new ArrayList<>();
    }

    public byte[] getPreviousHash() {
        return previousHash;
    }
    public byte[] getCurrentHash() {
        return currentHash;
    }
    public Block getPreviousBlock() {
        return previousBlock;
    }
    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public void setPreviousHash(byte[] prevHash) {
        this.previousHash = prevHash;
    }
    public void setCurrentHash(byte[] currentHash) {
        this.currentHash = currentHash;
    }
    public void setPreviousBlock(Block previousBlock) {
        this.previousBlock = previousBlock;
    }
    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
        this.currentHash = calculateHash();
    }

    public byte[] calculateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            ByteArrayOutputStream byteArrOut = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteArrOut);
            out.write(previousHash);

            //블록 내 모든 트랜잭션을 포함하는 것으로 보아 본 메소드는 머클루트 계산 메소드 같음
            for (Transaction tx : transactions) {
                out.writeUTF("TX|" + tx.getSender() + "|" + tx.getContent() + "|" + tx.getRecipient());
            }
            byte[] bytes = byteArrOut.toByteArray();
            return digest.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public byte[] calculateHashWithNonce(int nonce) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            ByteArrayOutputStream byteArrOut = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteArrOut);
            out.write(nonce);
            out.write(previousHash);
            for (Transaction tx : transactions) {
                out.writeUTF("TX|" + tx.getSender() + "|" + tx.getContent() + "|" + tx.getRecipient());
            }
            byte[] bytes = byteArrOut.toByteArray();
            return digest.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public String toString() {
        String cutOffRule = new String(new char[113]).replace("\0", "-") + "\n";
        String prevHashString = String.format("|PreviousHash:|%97s|\n", Base64.getEncoder().encodeToString(previousHash));
        String hashString = String.format("|CurrentHash:|%98s|\n", Base64.getEncoder().encodeToString(calculateHash()));
        String transactionsString = "";
        for (Transaction tx : transactions) {
            transactionsString += tx.toString();
        }
        return "src.Server.Block:\n"
                + cutOffRule
                + hashString
                + cutOffRule
                + transactionsString
                + cutOffRule
                + prevHashString
                + cutOffRule;
    }
}
