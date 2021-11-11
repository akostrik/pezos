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
import blockchain.ListOperations;
import blockchain.State;

public class LoopREPL {
	DataOutputStream out     = null;
	DataInputStream  in      = null;
	Scanner          scanner = null;
	
	public LoopREPL(Connection connection, String pk, String sk, int secondesBetweenBroadcastes, Scanner scanner) throws InvalidKeyException, DataLengthException, SignatureException, InvalidKeySpecException, NoSuchAlgorithmException, DecoderException, CryptoException {
		// answers to request , ignores broadcasts
		this.out     = connection.getOut();
		this.in      = connection.getIn();
		this.scanner = scanner;

		while(true) {
			try {
				int tag = getTagFromUser();
				int level = (tag==1 ? 0 : getLevelFromUser());
				
				if (tag == 1) {
					Block currentHead = new Block(Utils.getCurrentHead(this.out,this.in)); 	 // ignore tags 4,6,8
					State state = new State(Utils.getState(currentHead.getLevel(),out,in));  // exception if answer tag!=expected tag
					currentHead.verifyErrors(out,in,pk,sk,state,secondesBetweenBroadcastes); // ignores tags 4,6,8
				}
				if (tag == 3)                                                                // exception if answer tag!=expected tag
					System.out.println("I have got the block of level "+level+" :\n"+new Block(Utils.getBlockOfLevel(level,this.out,this.in))); 
				if (tag == 5)                                                                // exception if answer tag!=expected tag
					System.out.println("I have got the list of operations of level "+level+" :\n"+new ListOperations(Utils.getListOperations(level,this.out,this.in)));
				if (tag == 7)                                                                // exception if answer tag!=expected tag
					System.out.println("I have got the state of level "+level+" :\n"+new State(Utils.getState(level,out,in)));
			} catch (IOException | NoSuchElementException e) {
				System.out.println(e.getMessage() + "\n*** IOException or | NoSuchElementException, may be a problem with the socket or the internet connection");
				break; 
			} catch (BroadcastInsteadOfAnswerException e) { // ignores if tag!=expecteedTag
			}
		}
	}
	
	public int getTagFromUser() throws NoSuchElementException {
		try {
			System.out.println("\n1 - GET CURRENT HEAD and verify its errors\n3 - GET BLOCK\n5 - GET BLOCK OPERATIONS\n7 - GET BLOCK STATE");
			int tag = scanner.nextInt();
			if(tag!=1 && tag!=3 && tag !=5 && tag!=7)
				throw new InputMismatchException();
			return tag;
		}
		catch(InputMismatchException e) {
			System.out.println("Only 1, 3, 5 or 7 is accepted, try again:");
            scanner.nextLine(); // to clear Scanner
            return getTagFromUser();
		}
	}
	
	private int getLevelFromUser() throws IOException, DecoderException  {
		int currentHeadLevel=0;
		try {
			currentHeadLevel = new Block(Utils.getCurrentHead(out,in)).getLevel();
			System.out.println("Give the level between 2 and "+currentHeadLevel+" : ");
			int level = scanner.nextInt();
			if(level<2 || level>currentHeadLevel)
				throw new InputMismatchException();
			return level;
		}
		catch(InputMismatchException e) {
			System.out.println("Only an integer between 2 and "+currentHeadLevel +" is accepted, try again:");
            scanner.nextLine(); // to clear Scanner
            return getLevelFromUser();
		}
	}
}