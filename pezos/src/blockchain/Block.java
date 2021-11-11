package blockchain;
import java.util.Arrays;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;

import main.BroadcastInsteadOfAnswerException;
import main.Utils;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public class Block {
	private int    level; 
	private byte[] predecessor;
	private byte[] timestamp; 
	private byte[] operationsHash;
	private byte[] stateHash;
	private byte[] signature;
	private byte[] hashCurrentBlock;
	
	public Block(byte[] receivedMessage) throws IOException { 
		this.level          = Utils.toInt(Arrays.copyOfRange(receivedMessage,2,6)); 
        this.predecessor    = Arrays.copyOfRange(receivedMessage,6,38); 
        this.timestamp      = Arrays.copyOfRange(receivedMessage,38,46);
        this.operationsHash = Arrays.copyOfRange(receivedMessage,46,78);
        this.stateHash      = Arrays.copyOfRange(receivedMessage,78,110);
        this.signature      = Arrays.copyOfRange(receivedMessage,110,174);
        this.hashCurrentBlock = Utils.hash(this.encodeToBytes(),32);
    }
	
	public byte[] encodeToBytes() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(Utils.to4BytesArray(level));
		outputStream.write(predecessor); 
		outputStream.write(timestamp);
		outputStream.write(operationsHash);
		outputStream.write(stateHash);
		outputStream.write(signature);
		return outputStream.toByteArray();
	}

	public byte[] encodeToBytesWithoutSignature() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(Utils.to4BytesArray(level));
		outputStream.write(predecessor); 
		outputStream.write(timestamp);
		outputStream.write(operationsHash);
		outputStream.write(stateHash);
		return outputStream.toByteArray();
	}
	
	public void verifyErrors(DataOutputStream out, DataInputStream in, String pk, String sk, State state, int secondesBetweenBroadcastes) throws IOException, org.apache.commons.codec.DecoderException, InvalidKeyException, SignatureException, InvalidKeySpecException, NoSuchAlgorithmException, DataLengthException, CryptoException, BroadcastInsteadOfAnswerException {
		byte[] operationCorrection = null;
		
		System.out.println("I verify errors of the "+this);

		System.out.println("I verify state hash");
		if(!Arrays.equals(this.stateHash,state.hashTheState())){
			operationCorrection = Utils.concatArrays(Utils.to2BytesArray(4),state.hashTheState());
		}

		System.out.println("I verify timestamp      ");
		byte[] correctPredecessorTimestamp = state.getPredecessorTimestamp();
		long differenceTimestampsInSeconds = Utils.toLong(timestamp)-Utils.toLong(correctPredecessorTimestamp);
		if(differenceTimestampsInSeconds < secondesBetweenBroadcastes){
			long correctedTimestamp = Utils.toLong(correctPredecessorTimestamp) + secondesBetweenBroadcastes;
			operationCorrection = Utils.concatArrays(Utils.to2BytesArray(2),Utils.to8BytesArray(correctedTimestamp));
		}

		System.out.println("I verify predecessor    ");
		Block correctPredecessor               = new Block(Utils.getBlockOfLevel(level-1, out, in));
		if(!Arrays.equals(this.predecessor, correctPredecessor.getHash())){
			operationCorrection = Utils.concatArrays(Utils.to2BytesArray(1),correctPredecessor.getHash());
		}

		System.out.println("I verify operations hash");
	    ListOperations correctOperations = new ListOperations(Utils.getListOperations(level,out,in));
		if(!Arrays.equals(operationsHash, correctOperations.getHash())){
			operationCorrection = Utils.concatArrays(Utils.to2BytesArray(3),correctOperations.getHash());
		}

		System.out.println("I verify signature      ");
		byte[] hashBlockWithoutSignature = Utils.hash(this.encodeToBytesWithoutSignature(),32);
		if(!Utils.signatureIsCorrect(hashBlockWithoutSignature,this.signature,state.getDictatorPk(),out,in)) {
			operationCorrection = Utils.to2BytesArray(5);
		}
		
		if(operationCorrection!=null)
		    Utils.sendInjectOperation(operationCorrection, pk, sk, out);
	}

	public String toString() {
			return "BLOCK :"+
				 "\n  level:             "+level+ " (or "+Utils.toHexString(level) +" as Hex)"+
				 "\n  predecessor:       "+Utils.toHexString(predecessor)+
				 "\n  timestamp:         "+(Utils.toDateAsString(Utils.toLong(timestamp))+" (or "+Utils.toLong(timestamp)+" seconds, or "+Utils.toHexString(timestamp)+" as Hex)")+
				 "\n  operations hash:   "+Utils.toHexString(operationsHash)+
				 "\n  state hash:        "+Utils.toHexString(stateHash)+
				 "\n  signature:         "+Utils.toHexString(signature);
	}

	public long getTimeStamp() {
		return Utils.toLong(timestamp);
	}

	public byte[] getHash() {
		return this.hashCurrentBlock;
	}	
	
	public byte[] getSignature() {
		return this.signature;
	}

	public int getLevel() {
		return this.level;
	}
	}