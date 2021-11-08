package pezos;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import org.apache.commons.codec.DecoderException;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;

public class Connection {
	
	private DataOutputStream out    = null;
	private DataInputStream  in     = null;
	private Socket    socket = null; 
	
	public Connection(String hostname, int port, String pk, String sk) throws UnknownHostException, IOException, DecoderException, DataLengthException, CryptoException, InterruptedException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
		this.socket = new Socket(hostname, port); 			
		this.in	    = new DataInputStream (new BufferedInputStream (socket.getInputStream ()));
		this.out    = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		byte[] seed = Utils.getSeed(in); 
		Utils.sendPkToSocket(out,pk);
		Utils.sendSignatureToSocket(out,seed,sk);
		System.out.println("I have connected to the server");
	}
		
	public boolean isActive() throws IOException { 
		System.out.println("isActive?");
		boolean isActive = (socket.getInputStream().read()!=-1);
		System.out.println("isActive="+isActive);
		return isActive;
	}
	
	public DataOutputStream getOut() {
		return out;
	}

	public DataInputStream getIn() {
		return in;
	}
	
	public void close() throws IOException {
		this.in.close();
		this.out.close();
        this.socket.close();
	}
}
