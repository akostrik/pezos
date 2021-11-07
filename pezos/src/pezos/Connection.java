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

import tools.Utils;

public class Connection {
	
	private DataOutputStream out;
	private DataInputStream  in;
	
	public Connection(String hostname, int port, String pkString, String skString) throws UnknownHostException, IOException, DecoderException, DataLengthException, CryptoException, InterruptedException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
			Socket socket = new Socket(hostname, port); 
			this.in	 = new DataInputStream (new BufferedInputStream (socket.getInputStream ()));
			this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
						
			byte[] seed = Utils.getBytesFromSocket(24,this.in,"seed"); 

			Utils.sendToSocket(Utils.toBytesArray(pkString),this.out,"pk",Utils.addPointsAfter2Bytes);

			byte[] hashSeed = Utils.hash(seed, 32);
			byte[] signature = Utils.signature(hashSeed, skString);
			Utils.sendToSocket(signature,this.out,"signature",Utils.addPointsAfter2Bytes);
			
			System.out.println("I have connected to the server");
	}
		
	public DataOutputStream getOut() {
		return out;
	}

	public DataInputStream getIn() {
		return in;
	}
	
	public void closeConnection(Socket socket) throws IOException {
		this.in.close();
		this.out.close();
		Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
	    try {
	        socket.close();
	        System.out.println("The server is shut down!");
	    } catch (IOException e) {  }
		}});
	}
	
	
}