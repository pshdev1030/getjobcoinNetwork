package Client;

import Server.CryptoUtil;

import java.security.*;
import java.security.spec.ECGenParameterSpec;

public class Wallet {
    private PrivateKey privateKey; //트랜잭션에 사인을 하는 기능
    public PublicKey publicKey; // 입금을 받는 주소

    public Wallet() {
        generateKeyPair();
    }

    // Elliptic Curve KeyPair 방식을 사용하여 두 키를 생성
    private void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //서명 생성하기
    public byte[] generateSignature(PrivateKey privateKey, String sender, String recipient, String content) {
        String data = sender + recipient + content;
        return CryptoUtil.applyECDSASig(privateKey, data); //return signature
    }

    public String getPublicKeyString() {
        return CryptoUtil.getStringFromKey(publicKey);
    }

    public PrivateKey getPrivateKey() { return privateKey;}
}