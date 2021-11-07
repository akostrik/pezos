package blockchain;
import java.util.Arrays;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import tools.Utils;
import tools.WrongTagFromSocketException;
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
	
	public void verifyErrors(DataOutputStream out, DataInputStream in, String pk, String sk) throws IOException, org.apache.commons.codec.DecoderException, InvalidKeyException, SignatureException, InvalidKeySpecException, NoSuchAlgorithmException, DataLengthException, CryptoException, WrongTagFromSocketException{
		verifyErrors(out,in,pk,sk,null);
	}
	
	public void verifyErrors(DataOutputStream out, DataInputStream in, String pk, String sk, State state) throws IOException, org.apache.commons.codec.DecoderException, InvalidKeyException, SignatureException, InvalidKeySpecException, NoSuchAlgorithmException, DataLengthException, CryptoException, WrongTagFromSocketException{
		System.out.print("I verify state hash     ");
		if(state==null)
			state = Utils.getState(level,out,in);
		if(!Arrays.equals(this.stateHash,state.hashTheState())){
			System.out.print(" ERROR -> ");
			byte[] operationContent = Utils.concatArrays(Utils.to2BytesArray(4),state.hashTheState());
		    Utils.sendInjectOperationTag9(operationContent, pk, sk, out);
		}
		else {
			System.out.println();
		}

		System.out.print("I verify timestamp      ");
		byte[] correctPredecessorTimestamp = state.getPredecessorTimestamp();
		long differenceTimestampsInSeconds = Utils.toLong(timestamp)-Utils.toLong(correctPredecessorTimestamp);
		if(differenceTimestampsInSeconds < 600){
			System.out.print(" ERROR -> ");
			long correctedTimestamp = Utils.toLong(correctPredecessorTimestamp) + 600;
			byte[] operationContent = Utils.concatArrays(Utils.to2BytesArray(2),Utils.to8BytesArray(correctedTimestamp));
		    Utils.sendInjectOperationTag9(operationContent, pk, sk, out);
		}
		else {
			System.out.println();
		}

		System.out.print("I verify predecessor    ");
		Block predecessor               = Utils.getPredecessor(level-1, out, in);
		if(!Arrays.equals(this.predecessor, predecessor.getHash())){
			System.out.print(" ERROR -> ");
			byte[] operationContent = Utils.concatArrays(Utils.to2BytesArray(1),predecessor.getHash());
		    Utils.sendInjectOperationTag9(operationContent, pk, sk, out);
		}
		else {
			System.out.println();
		}

		System.out.print("I verify operations hash");
	    ListOperations operations = Utils.getListOperations(level,out,in);
		if(!Arrays.equals(operationsHash, operations.getHash())){
			System.out.print(" ERROR -> ");
			byte[] operationContent = Utils.concatArrays(Utils.to2BytesArray(3),operations.getHash());
		    Utils.sendInjectOperationTag9(operationContent, pk, sk, out);
		}
		else {
			System.out.println();
		}

		System.out.print("I verify signature      ");
		if(!Utils.verifySignature(this,state,out,in)) {
			System.out.print(" ERROR -> ");
			byte[] operationContent = Utils.to2BytesArray(5);
		    Utils.sendInjectOperationTag9(operationContent, pk, sk, out);
		}
		else {
			System.out.println();
		}
	}

	public String toString() {
			return "BLOCK :"+
				 "\n  level:             "+level+ " (or "+Utils.toStringOfHex(level) +" as Hex)"+
				 "\n  predecessor:       "+Utils.toHexString(predecessor)+
				 "\n  timestamp:         "+(Utils.toDateAsString(Utils.toLong(timestamp))+" (or "+Utils.toLong(timestamp)+" seconds, or "+Utils.toHexString(timestamp)+" as Hex)")+
				 "\n  operations hash:   "+Utils.toHexString(operationsHash)+
				 "\n  state hash:        "+Utils.toHexString(stateHash)+
				 "\n  signature:         "+Utils.toHexString(signature)+
				 "\n  hash of the block: "+Utils.toHexString(hashCurrentBlock);
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