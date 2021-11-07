package blockchain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.codec.DecoderException;

import pezos.Utils;

public class State {

	private byte[] dictateurPk;
	private byte[] predecessor_timestamp;
	private byte[] nbBytesInNextSequence;
	private byte[] accountsAsBytes;
	private byte[] stateAsBytes;
	private byte[] hashCurrentState;
	private ArrayList<Account> listAccounts;
	private int    level;
	
	public State(byte[] receivedMessage, int level) throws IOException {
		listAccounts = new ArrayList<Account>();
		this.stateAsBytes          = Arrays.copyOfRange(receivedMessage,2,receivedMessage.length);
		this.dictateurPk           = Arrays.copyOfRange(receivedMessage,2,34);
		this.predecessor_timestamp = Arrays.copyOfRange(receivedMessage,34,42);
		this.nbBytesInNextSequence = Arrays.copyOfRange(receivedMessage,42,46);
		this.accountsAsBytes       = Arrays.copyOfRange(receivedMessage,46,receivedMessage.length);
		extractAccounts(accountsAsBytes);
        this.hashCurrentState = Utils.hash(this.encodeToBytes(),32);
        this.level=level;
	}
	
	public byte[] encodeToBytes() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(dictateurPk);
		outputStream.write(predecessor_timestamp); 
		outputStream.write(nbBytesInNextSequence);
		outputStream.write(accountsAsBytes);
		return outputStream.toByteArray();
	}

	public void extractAccounts(byte[] accountsAsBytes) {
		if (accountsAsBytes.length >= 52) {
			Account account = new Account(accountsAsBytes);
			this.listAccounts.add(account);
			if (accountsAsBytes.length > 52) {
				accountsAsBytes = Arrays.copyOfRange(accountsAsBytes,52,accountsAsBytes.length);
				extractAccounts(accountsAsBytes);	
			}
		}
	}

	public byte[] hashTheState() {
		return Utils.hash(this.stateAsBytes, 32);
	}
	
	public byte[] getAccountsBytes() {
		return this.accountsAsBytes;
	}

	public byte[] getPredecessorTimestamp(){
		return this.predecessor_timestamp;
	}
	
	public Account getAccount(String pk) throws DecoderException {
		for(Account account: listAccounts)
			if(Arrays.equals(account.getUserPubkey(),Utils.toBytesArray(pk)))
				return account;
		return null;
	}

	public byte[] getDictatorPk() {
		return this.dictateurPk;
	}

	public String toString() {
		String accounts = "";
		for(Account account : listAccounts)
			accounts +="    "+account.toString()+"\n";
		return "STATE :"+
				"\n  level                 = "+level+
				"\n  dictateurPubkey       = "+Utils.toHexString(dictateurPk)+
  			    "\n  predecessor_timestamp = "+(Utils.toDateAsString(Utils.toLong(predecessor_timestamp))+" (or "+Utils.toLong(predecessor_timestamp)+" seconds, or "+Utils.toHexString(predecessor_timestamp)+" as Hex)")+
				"\n  nbBytesInNextSequence = "+Utils.toInt(nbBytesInNextSequence)+" (or "+Utils.toHexString(nbBytesInNextSequence)+" as Hex) ("+(Utils.toInt(nbBytesInNextSequence)/52)+" accounts)"+
			    "\n  hash of this state    = "+Utils.toHexString(hashCurrentState)+
				"\n  accounts :\n"+accounts;
	}
}