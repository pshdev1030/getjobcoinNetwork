package Server;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

public class Transaction implements Serializable {

    private String sender;
    private String recipient;
    private String content;
    private boolean isValid;

    private byte[] signature;

    public Transaction() {
        isValid = false;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.sender = sender;
        isValid = validate();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.content = content;
        isValid = validate();
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.recipient = recipient;
        isValid = validate();
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.signature = signature;
        if(verifySignature()){
            isValid = true;
        }
        else{
            isValid = false;
        }
    }

    //서명 생성하기
    public void generateSignature(PrivateKey privateKey) {
        String data = sender + recipient + content;
        signature = CryptoUtil.applyECDSASig(privateKey, data);
    }

    //데이터 검증
    public boolean verifySignature() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String data = sender + recipient + content;
        return CryptoUtil.verifyECDSASig(CryptoUtil.getKeyFromString(sender), data, signature);
    }

    public String toString() {
        if (isValid) {
            return String.format("|Sender:%104s|\n" +
                    "|Content:%103s|\n" +
                    "|Recipient:%101s|\n", sender, content, recipient);
        }
        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (!isValid) {
            return false;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        Transaction tx = (Transaction) other;
        if (content.equals(tx.getContent()) && sender.equals(tx.getSender())) {
            return true;
        }
        return false;
    }

    //규칙에 맞게 유효한지를 판단
    //BlockChainClient에서 "TX|수신자 주소|Content|송신자 주소" 형식에 맞게 입력
    private boolean validate() throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (sender == null || recipient == null || content == null) {
            return false;
        }
        if (sender.length()!=100 || recipient.length()!=100) {
            return false;
        }
        if (content.contains("\\|") || content.length() > 70) {
            return false;
        }
        return true;
    }

    public boolean isValid() {
        return isValid;
    }

}

