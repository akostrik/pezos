package blockchain;
import java.util.Arrays;

import main.Utils;

public class Account {
	private byte[] userPubkey;
	private byte[] predPez;
	private byte[] timestampPez;
	private byte[] operationsHashPez;
	private byte[] contextHashPez;
	private byte[] signaturePez;
	
	public Account (byte[] accountsBytes) {
		this.userPubkey = Arrays.copyOfRange(accountsBytes,0,32);
		this.predPez = Arrays.copyOfRange(accountsBytes,32,36);
		this.timestampPez = Arrays.copyOfRange(accountsBytes,36,40);
		this.operationsHashPez = Arrays.copyOfRange(accountsBytes,40,44);
		this.contextHashPez = Arrays.copyOfRange(accountsBytes,44,48);
		this.signaturePez = Arrays.copyOfRange(accountsBytes,48,52);
	}

	public String toString() {
		String result = String.format("ACCOUNT %s : %4d + %4d + %4d + %4d + %4d = %4d",Utils.toHexString(userPubkey),Utils.toInt(predPez),Utils.toInt(timestampPez),Utils.toInt(operationsHashPez),Utils.toInt(contextHashPez),Utils.toInt(signaturePez),(Utils.toInt(predPez)+Utils.toInt(timestampPez)+Utils.toInt(operationsHashPez)+Utils.toInt(contextHashPez)+Utils.toInt(signaturePez)));
		return result;
	}

	public byte[] getUserPubkey() {
		return userPubkey;
	}
}
