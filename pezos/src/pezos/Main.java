package pezos;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.commons.codec.DecoderException;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;

public class Main {
	public static void main(String[] args) throws DataLengthException, DecoderException, CryptoException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException, IOException {
		final String hostname                  = "78.194.168.67";
		final int    port                      = 1337;
		final String pk                        = "b8b606dba2410e1f3c3486e0d548a3053ba3f907860fada6fab2835fb27b3f21"; // public
		final String sk                        = "1f06949f1278fcbc0590991180d5b567d240c0b0576d1d34cad66db49d4eea4a"; // secret
		final int    secondsBetweenBroadcastes = 600;
		Connection   connection                = null;
		Scanner      scanner                   = new Scanner(System.in);

		int mode = getModeFromUser(secondsBetweenBroadcastes,scanner);
		
		try {
			connection = new Connection(hostname,port,pk,sk);
			if(mode==1)
				new LoopAuto(connection,pk,sk,secondsBetweenBroadcastes);
			else if(mode==2)
				new LoopREPL(connection, pk, sk,secondsBetweenBroadcastes,scanner);
		} catch (IOException e) {
		}
		finally {
			try {
				connection.close();
				scanner.close();
			}
			catch (NullPointerException e) {
			}
		}
	}
	
	public static int getModeFromUser(int secondesBetweenBroadcastes,Scanner scanner) throws NoSuchElementException {
		try {
			System.out.println("\n1 - automatic (every "+secondesBetweenBroadcastes+" seconds)\n2 - manual");
			int mode = scanner.nextInt();
			if(mode!=1 && mode!=2) {
				throw new InputMismatchException("only 1 or 2 is accepted");
			}
			return mode;
		}
		catch(InputMismatchException e) {
			System.out.println("Only 1 or 2 is accepted, try again:");
            scanner.nextLine(); // to clear Scanner
            return getModeFromUser(secondesBetweenBroadcastes,scanner);
		}
	}
}