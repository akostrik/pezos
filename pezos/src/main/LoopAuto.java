package main;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.DecoderException;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import blockchain.Block;
import blockchain.State;
import utils.BroadcastInsteadOfAnswerException;
import utils.Utils;
public class LoopAuto {

	public LoopAuto(Connection connection, String pk, String sk, int secondsBetweenBroadcastes) throws DecoderException, InterruptedException, InvalidKeyException, DataLengthException, SignatureException, InvalidKeySpecException, NoSuchAlgorithmException, CryptoException, IOException  {
		// when waits for a broadcast, ignore tags 4,6,8
		// when waits for tags 4, 6, 8, treats a broadcast
		DataOutputStream out                = connection.getOut();
		DataInputStream  in                 = connection.getIn();
		ArrayList<Block> broadcasts = new ArrayList<Block>(); 
		broadcasts.add(new Block(Utils.getCurrentHead(out,in))); // ignore tags 4,6,8
		Block broadcastToVerify = null;
		State state = null;
		
		while(true) {
			System.out.println("--------------------------- ITERATION "+Utils.toDateAsString(Utils.currentDateTimeAsSeconds()));

			broadcasts = removeDoubleBroadcastsIfAny(broadcasts);

			broadcastToVerify = broadcasts.get(0);

			try {
				state = new State(Utils.getState(broadcastToVerify.getLevel(),out,in));
        	} catch (BroadcastInsteadOfAnswerException e) {
        		broadcasts.add(new Block(e.getUnexpectedBroadcast()));
        		continue;
        	}
			
			System.out.println("My account : "+state.getAccount(pk).toString());
			
			try {
				broadcastToVerify.verifyErrors(out,in,pk,sk,state,secondsBetweenBroadcastes);
        	} catch (BroadcastInsteadOfAnswerException e) {
        		broadcasts.add(new Block(e.getUnexpectedBroadcast()));
        		continue;
        	}
			broadcasts.remove(broadcastToVerify);

			waitForTheNextBroadcast(secondsBetweenBroadcastes,state);
			broadcasts.add(new Block(Utils.getBroadcast(in)));// ignore tags 4,6,8
		}
	}

	public ArrayList<Block> removeDoubleBroadcastsIfAny(ArrayList<Block> broadcastes) throws InterruptedException {
		if(broadcastes.size()<2)
			return broadcastes;
		while(true) {
			Block previousBroadcast = broadcastes.get(broadcastes.size()-1);
			Block broadcast         = broadcastes.get(broadcastes.size());
			if(Arrays.equals(broadcast.getHash(),previousBroadcast.getHash())) {
				broadcastes.remove(broadcastes.size()-1);
			}
			else 
				break;
		}
		return broadcastes;
	}
	
	public static void waitForTheNextBroadcast(int secondesBetweenBroadcastes, State state) throws InterruptedException {
		long whenReceivedLastBroadcast = Utils.currentDateTimeAsSeconds();
		long correctPredecessorTimestamp = Utils.toLong(state.getPredecessorTimestamp());
		long secondsAfterPreviousBroadcast = whenReceivedLastBroadcast-correctPredecessorTimestamp;
		long secondsToWait = secondesBetweenBroadcastes*2 - secondsAfterPreviousBroadcast + 2;
		System.out.println("I am waiting "+secondsToWait+" seconds, I will fetch the next broadcast at "+Utils.toDateAsString(whenReceivedLastBroadcast+secondsToWait));
		TimeUnit.SECONDS.sleep(secondsToWait);			
	}
}