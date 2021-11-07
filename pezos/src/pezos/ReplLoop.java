package pezos;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.InputMismatchException;
import java.util.Scanner;
import org.apache.commons.codec.DecoderException;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;

import blockchain.Block;
import tools.Utils;
import tools.WrongTagFromSocketException;

public class ReplLoop {
	DataOutputStream out = null;
	DataInputStream  in = null;
	String           pk;
	String           sk;
	Scanner          scanner = null;
	
	public ReplLoop(Connection connection, String pk, String sk) throws InvalidKeyException, DataLengthException, SignatureException, InvalidKeySpecException, NoSuchAlgorithmException, IOException, DecoderException, CryptoException {
		this.out     = connection.getOut();
		this.in      = connection.getIn();
		this.pk      = pk;
		this.sk      = sk;
		this.scanner = new Scanner(System.in);

		while(true) {
			try {
				oneIteration();
			} catch (WrongTagFromSocketException e) {
				System.out.println(e.getMessage());
				return;
			}
		}
	}
	
	public void oneIteration() throws IOException, DecoderException, InvalidKeyException, DataLengthException, SignatureException, InvalidKeySpecException, NoSuchAlgorithmException, CryptoException, WrongTagFromSocketException {
		int tag = getTag();
		int level = (tag==3 || tag==5 || tag==7) ? getLevel() : 0;

		try {
			if (tag == 1) {
				Block lastBlock = Utils.getCurrentHead(this.out,this.in);
				System.out.println("I verify errors of the block "+lastBlock);
				lastBlock.verifyErrors(out,in,pk,sk);
			} else if (tag == 3) {
				System.out.println("I jave got the block of level "+level+" :\n"+Utils.getPredecessor(level,this.out,this.in));
			} else if (tag == 5) {
				System.out.println("I have got the list of operations of level "+level+" :\n"+Utils.getListOperations(level,this.out,this.in));
			} else if (tag == 7) {
				System.out.println("I have got the state of level "+level+" :\n"+Utils.getState(level,this.out,this.in));
			}
		} catch (WrongTagFromSocketException e) {
			e.printStackTrace();
		}
	}

	public int getTag() {
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
	
	private int getLevel() throws IOException, DecoderException, WrongTagFromSocketException {
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