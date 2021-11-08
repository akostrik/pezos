package pezos;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

import blockchain.Block;

public class LoopREPL {
	DataOutputStream out = null;
	DataInputStream  in = null;
	String           pk;
	String           sk;
	Scanner          scanner = null;
	
	public LoopREPL(Connection connection, String pk, String sk) throws InvalidKeyException, DataLengthException, SignatureException, InvalidKeySpecException, NoSuchAlgorithmException, DecoderException, CryptoException {
		this.out     = connection.getOut();
		this.in      = connection.getIn();
		this.pk      = pk;
		this.sk      = sk;
		this.scanner = new Scanner(System.in);

		while(true) {
			try {
				int tag = getTag();
				if (tag == 1) {
					Block lastBlock = Utils.getCurrentHead(this.out,this.in);
					System.out.println("I verify errors of the block "+lastBlock);
					lastBlock.verifyErrors(out,in,pk,sk);
				} else {
					int level = getLevel();
					if (tag == 3) 
						System.out.println("I jave got the block of level "+level+" :\n"+Utils.getBlock(level,this.out,this.in));
					else if (tag == 5) 
						System.out.println("I have got the list of operations of level "+level+" :\n"+Utils.getListOperations(level,this.out,this.in));
					else if (tag == 7) 
						System.out.println("I have got the state of level "+level+" :\n"+Utils.getState(level,this.out,this.in));
				}
			} catch (IOException | NoSuchElementException e) {
				System.out.println("IOException or | NoSuchElementException, may be a problem with the socket or the internet connection");
				break;
			}
		}
	}
	
	public int getTag() throws NoSuchElementException {
		try {
			System.out.println("Give the tag (1, 3, 5 or 7): ");
			return scanner.nextInt();
		}
		catch(InputMismatchException e) {
			System.out.println("Only 1, 3, 5 or 7 are accepted, try again:");
            scanner.nextLine(); // to clear Scanner
            return getTag();
		}
	}
	
	private int getLevel() throws IOException, DecoderException  {
		int currentHeadLevel=0;
		try {
			currentHeadLevel = Utils.getCurrentHead(out,in).getLevel();
			System.out.println("Give the level between 2 and "+currentHeadLevel+" : ");
			int level = scanner.nextInt();
			if(level<2 || level>currentHeadLevel)
				throw new InputMismatchException();
			return level;
		}
		catch(InputMismatchException e) {
			System.out.println("Only an integer between 2 and "+currentHeadLevel +" is accepted, try again:");
            scanner.nextLine(); 
            return getLevel();
		}
	}
}
