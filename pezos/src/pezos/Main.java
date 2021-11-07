// level 1?
// tag 3 level 1 la réponse est très très long
package pezos;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.codec.DecoderException;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;

public class Main {
	public static void main(String[] args) throws DataLengthException, DecoderException, CryptoException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException {
		final String hostname = "78.194.168.67";
		final int    port     = 1337;
		final String pk       = "b8b606dba2410e1f3c3486e0d548a3053ba3f907860fada6fab2835fb27b3f21"; // public
		final String sk       = "1f06949f1278fcbc0590991180d5b567d240c0b0576d1d34cad66db49d4eea4a"; // secret
		final int    secondesBetweenBroadcastes = 600;
	
		Connection connection = null;
		try {
			connection = new Connection(hostname,port,pk,sk);
			new LoopAuto(connection,pk,sk,secondesBetweenBroadcastes);
			//new LoopREPL(connection, pk, sk);
		} catch (IOException e) {
			System.out.println("IOException in main, problem with the socket or the internet connection");
			return;
		}
	}
}