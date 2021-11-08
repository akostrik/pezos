package pezos;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.DecoderException;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;

import blockchain.Block;
import blockchain.State;
public class LoopAuto {

	public LoopAuto(Connection connection, String pk, String sk, int secondesBetweenBroadcastes) throws DecoderException, InterruptedException, InvalidKeyException, DataLengthException, SignatureException, InvalidKeySpecException, NoSuchAlgorithmException, CryptoException {
		DataOutputStream out                   = connection.getOut();
		DataInputStream  in                    = connection.getIn();
		Block            previousBroadcast     = null; 
		Block            broadcastedBlock      = null;
		State            state                 = null;
		boolean          firstIteration        = true;

		while(true) {
			System.out.println("--------------------------- ITERATION "+Utils.toDateAsString(Utils.currentDateTimeAsSeconds()));
			try {
				broadcastedBlock = firstIteration ? Utils.getCurrentHead(out,in) : Utils.getBlock(in);
				if(previousBroadcast!=null && Arrays.equals(broadcastedBlock.getHash(),previousBroadcast.getHash())) {
					System.out.println("I have got the same broadcast, I am waiting 60 secondes");
					TimeUnit.SECONDS.sleep(60);
					continue;
				}
				state = Utils.getState(broadcastedBlock.getLevel(),out,in);
				System.out.println("My account : "+state.getAccount(pk).toString());
				System.out.println(broadcastedBlock);
				broadcastedBlock.verifyErrors(out,in,pk,sk,state);
			} catch (IOException | NoSuchElementException e) {
				System.out.println("IOException or | NoSuchElementException, may be a problem with the socket or the internet connection");
				break;
			}

			firstIteration = false;
			previousBroadcast = broadcastedBlock;
			Utils.waitForTheNextBroadcast(secondesBetweenBroadcastes,state);
		}
	}
}
