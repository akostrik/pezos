package blockchain;
import java.io.IOException;
import java.util.Arrays;

import main.Utils;

public class Operation {
	private byte[] operationAsBytes = null;
	private int  tag                = 0;
	private byte[] hash             = null;
	private byte[] time             = null;
	private byte[] pubkey           = null;
	private byte[] signature        = null;
	
	public Operation (byte[] operationAsBytes) {
		this.operationAsBytes=operationAsBytes;
		this.tag = Utils.toInt(Arrays.copyOfRange(operationAsBytes,0,2));
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
	
	// 0006
	// 01040001AE3C32BF5D2854B2993D55000738F18894886D50B175830F5EBE70CFB081334A013375B4CE8AB350CFE690E73F17B791010FD709822F841B3DA43D604DEEE6AE9CBE4F5C109640A019656C572EE536BF4202E1167FFD0114E7B146AE07A3BD178567F0D09292514A31A188B72C157BF86730B4B63AB50C207A390C27BE380E0C0001AE3C32BF5D2854B2993D55000738F18894886D50B175830F5EBE70CFB081334AB8B606DBA2410E1F3C3486E0D548A3053BA3F907860FADA6FAB2835FB27B3F21675635971BF8AFA465F3DE425377705035EB45823D278368D1BD0BD2592FD68DA4411F87C091D541F093A61D001883396DCD95AF2D6F152A22A88FD10182EF0D
	
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
