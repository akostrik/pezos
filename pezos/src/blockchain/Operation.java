package blockchain;
import java.io.IOException;
import java.util.Arrays;

import pezos.Utils;

public class Operation {
	private byte[] operationAsBytes = null;
	private short  tag              = 0;
	private byte[] hash             = null;
	private byte[] time             = null;
	private byte[] pubkey           = null;
	private byte[] signature        = null;
	
	public Operation (byte[] operationAsBytes) {
		this.operationAsBytes=operationAsBytes;
		this.tag = Utils.toShort(Arrays.copyOfRange(operationAsBytes,0,2));
		this.hash      = null;
		this.time      = null;
		if (tag==1 || tag==3 || tag==4) {
			this.hash      = Arrays.copyOfRange(operationAsBytes,2,34);
			this.pubkey    = Arrays.copyOfRange(operationAsBytes,34,66);
			this.signature = Arrays.copyOfRange(operationAsBytes,66,130);
		} else if (tag == 2) { 
			this.time      = Arrays.copyOfRange(operationAsBytes,2,10);
			this.pubkey    = Arrays.copyOfRange(operationAsBytes,10,42);
			this.signature = Arrays.copyOfRange(operationAsBytes,42,106);
		} else if (tag==5) { 
			this.pubkey    = Arrays.copyOfRange(operationAsBytes,2,34);
			this.signature = Arrays.copyOfRange(operationAsBytes,34,98);
		}
	}
	
	public byte[] getAsBytes() throws IOException {
		return operationAsBytes;
	}
	
	public String toString() {
		return "OPERATION tag="      +tag+
			            " hash="     +(hash!=null?Utils.toHexString(hash):null)+
			            " time="     +(time!=null?Utils.toDateAsString(time):null)+
			            " pk="       +Utils.toHexString(this.pubkey)+
			            " signature="+Utils.toHexString(this.signature);
	}
}
