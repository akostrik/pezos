package pezos;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import ove.crypto.digest.Blake2b;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import blockchain.Block;
import blockchain.ListOperations;
import blockchain.State;

import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.*;

public class Utils {
	public static ArrayList<Integer> addPointsAfter2Bytes;
	public static ArrayList<Integer> addPointsAfter2and4Bytes;
	public static ArrayList<Integer> addPointsToBlock;
	
	public Utils() {
		addPointsAfter2Bytes = new ArrayList<Integer>();
		addPointsAfter2Bytes.add(2);
		addPointsAfter2and4Bytes = new ArrayList<Integer>();
		addPointsAfter2and4Bytes.add(2);
		addPointsAfter2and4Bytes.add(4);
		addPointsToBlock = new ArrayList<Integer>();
		addPointsToBlock.add(2);
		addPointsToBlock.add(6);
		addPointsToBlock.add(38);
		addPointsToBlock.add(46);
		addPointsToBlock.add(78);
		addPointsToBlock.add(110);
	}
	
	/////////////// SEND TO SOCKET 

	public static void sendToSocket(byte[] bytesArrayToSend, DataOutputStream out, String comment, ArrayList<Integer> positionsOfPoints) throws IOException, DecoderException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(to2BytesArray(bytesArrayToSend.length));
		outputStream.write(bytesArrayToSend);
		bytesArrayToSend = outputStream.toByteArray(); 
		out.write(bytesArrayToSend); 
		out.flush(); // the last of the data gets out to the file
		// System.out.println(whatIHaveSent(bytesArrayToSend, comment, positionsOfPoints));
	}

	public static void sendPkToSocket(DataOutputStream out,String pk) throws IOException, DecoderException {
		sendToSocket(toBytesArray(pk),out,"pk",addPointsAfter2Bytes);
	}

	public static void sendSignatureToSocket(DataOutputStream out, byte[] seed, String sk) throws DataLengthException, DecoderException, CryptoException, IOException {
		byte[] hashSeed = hash(seed,32);
		byte[] signature = signature(hashSeed,sk);
		Utils.sendToSocket(signature,out,"signature",addPointsAfter2Bytes);
	}


	public static void sendInjectOperationTag9(byte[] content, String pk, String sk, DataOutputStream out) throws DataLengthException, org.apache.commons.codec.DecoderException, CryptoException, IOException{
		byte[] pkBytes   = toBytesArray(pk);
		byte[] signature = signature(hash(concatArrays(content, pkBytes),32), sk);
		byte[] message   = concatArrays(to2BytesArray(9),content,pkBytes,signature);
		sendToSocket(message, out,"",addPointsAfter2and4Bytes);
        System.out.println("INJECT OPERATION "+Utils.toHexString(Arrays.copyOfRange((message),0,2))+"."+Utils.toHexString(Arrays.copyOfRange((message),2,4))+"."+Utils.toHexString(Arrays.copyOfRange((message),4,message.length)));
	}

	public static String whatIHaveSent(byte[] bytesArrayToSend, String comment, ArrayList<Integer> positionsOfPoints) {
		String result = "*** SOCKET : "+(comment==""?"":comment+" ")+"sent : ";
		for(int pos=0;pos<bytesArrayToSend.length;pos++) {
			result += toHexString(Arrays.copyOfRange(bytesArrayToSend,pos,pos+1));
			if(positionsOfPoints!=null && positionsOfPoints.contains(pos+1)) 
				result+=".";
			}
		return result;
	}

	/////////////// GET FROM TO SOCKET 

	public static byte[] getBytes(int nbBytesWanted, DataInputStream in, String comment, ArrayList<Integer> positionsOfPoints) throws IOException {
		byte[] receivedMessage = new byte[nbBytesWanted];
		int nbBytesReceived=0;
		nbBytesReceived = in.read(receivedMessage,0,nbBytesWanted); // if no wifi, blocked for 15 minutes then IOExcetion // blocks until data available / EOF / exception 
		// System.out.println(whatIHaveReceived(receivedMessage, nbBytesReceived, comment, positionsOfPoints));
		return receivedMessage;
	}	
	
	public static String whatIHaveReceived(byte[] receivedMessage, int nbBytesReceived, String comment, ArrayList<Integer> positionsOfPoints) {
		String result = "*** SOCKET : "+(comment==""?"":comment+" ")+"received : "+nbBytesReceived+" bytes : ";
		for(int pos=0;pos<receivedMessage.length;pos++) {
			result += toHexString(Arrays.copyOfRange(receivedMessage,pos,pos+1));
			if(positionsOfPoints!=null && positionsOfPoints.contains(pos+1)) 
				result+=".";
		}
		return result;
	}

	public static byte[] getBytesFromSocket(int nbBytesWanted, DataInputStream in, String comment) throws IOException {
		return getBytes(nbBytesWanted,in,comment,null);
	}
	
	public static Block getBlock(DataInputStream  in) throws IOException, org.apache.commons.codec.DecoderException {
		byte[] receivedMessage = getBytes(174,in,"block",addPointsToBlock);
		byte[] receivedTag = Arrays.copyOfRange(receivedMessage,0,2);
		if(toShort(receivedTag)!=2 && toShort(receivedTag)!=4) {
			throw new IOException("I waited for tag 2 or 4 from socket, I received tag " +toShort(receivedTag));
		}
		return new Block(receivedMessage);
	}

	public static Block getCurrentHead(DataOutputStream out, DataInputStream  in) throws IOException, org.apache.commons.codec.DecoderException {
		sendToSocket(to2BytesArray(1),out,"tag 1",addPointsAfter2Bytes);
		Block block = getBlock(in);
		return block;
	}
	
	public static Block getBlock(int level, DataOutputStream out, DataInputStream  in) throws org.apache.commons.codec.DecoderException, IOException {
        byte[] msg = concatArrays(to2BytesArray(3),to4BytesArray(level));
        sendToSocket(msg,out,"tag 3 level "+level,addPointsAfter2and4Bytes);
        return getBlock(in);
	}
	
	public static ListOperations getListOperations(int level, DataOutputStream out, DataInputStream  in) throws org.apache.commons.codec.DecoderException, IOException {
        byte[] msg = concatArrays(to2BytesArray(5),to4BytesArray(level));
        sendToSocket(msg,out,"tag 5 level "+level,addPointsAfter2and4Bytes);

        byte[] receivedTag       = getBytesFromSocket(2,in,"tag");
		if(toShort(receivedTag)!=6) {
			throw new IOException("I waited for tag 6 from socket, I received tag " +toShort(receivedTag));
		}
        byte[] nbBytesToRead     = getBytesFromSocket(2,in,"nbBytesToRead");
        byte[] operationsAsBytes = getBytesFromSocket(toShort(nbBytesToRead),in,"operations"); 
        byte[] receivedMessage   = concatArrays(receivedTag,nbBytesToRead,operationsAsBytes);

        return new ListOperations(receivedMessage, level);
	}
	
	public static State getState(int level, DataOutputStream out, DataInputStream  in) throws org.apache.commons.codec.DecoderException, IOException {
        byte[] msg = concatArrays(to2BytesArray(7),to4BytesArray(level));
        sendToSocket(msg,out,"tag 7 level "+level,addPointsAfter2and4Bytes);

        byte[] receivedTag           = getBytesFromSocket(2,in,"tag");
		if(toShort(receivedTag)!=8) {
			throw new IOException("I waited for tag 8 from socket, I received tag " +toShort(receivedTag));
		}       

		byte[] dictatorPk           = getBytesFromSocket(32,in,"dict pk");
		byte[] predTimestamp        = getBytesFromSocket(8,in,"pred timestamp");
		byte[] nbBytesNextSequence  = getBytesFromSocket(4,in,"nb bytes next seq");
        byte[] accounts             = getBytesFromSocket(toInt(nbBytesNextSequence),in,"accounts");
        byte[] receivedMessage      = concatArrays(receivedTag,dictatorPk,predTimestamp,nbBytesNextSequence,accounts);

        String toPrint = toHexString(receivedTag)+"."+toHexString(dictatorPk)+"."+toDateAsString(predTimestamp)+"."+toHexString(nbBytesNextSequence)+"."+toHexString(accounts);
        //System.out.println("tag7 answer = " + toPrint + " = "+(new State(receivedMessage)).toString());
        return new State(receivedMessage, level);
	}
	
	public static byte[] getSeed(DataInputStream  in) throws IOException {
		return Utils.getBytesFromSocket(24,in,"seed"); 
	}

	public static void waitForTheNextBroadcast(int secondesBetweenBroadcastes, State state) throws InterruptedException {
		long whenReceivedLastBroadcast = Utils.currentDateTimeAsSeconds();
		long correctPredecessorTimestamp = Utils.toLong(state.getPredecessorTimestamp());
		long secondsAfterPreviousBroadcast = whenReceivedLastBroadcast-correctPredecessorTimestamp;
		long secondsToWait = secondesBetweenBroadcastes*2 - secondsAfterPreviousBroadcast + 2;
		System.out.println("I am waiting "+secondsToWait+" seconds, I will fetch the next broadcast at "+Utils.toDateAsString(whenReceivedLastBroadcast+secondsToWait));
		TimeUnit.SECONDS.sleep(secondsToWait);			
	}

	/////// CRYPTO
	
	public static byte[] hash(byte[] valeurToHash, int hashParamNbBytes) {
		Blake2b.Param param = new Blake2b.Param().setDigestLength(hashParamNbBytes);
		final Blake2b blake2b = Blake2b.Digest.newInstance(param);        
		return blake2b.digest(valeurToHash);
	}
	
	public static byte[] signature(byte[] msgToSign, String sk) throws DecoderException, DataLengthException, CryptoException {
		Ed25519PrivateKeyParameters sk2 = new Ed25519PrivateKeyParameters(toBytesArray(sk));
		Signer signer = new Ed25519Signer();
		signer.init(true, sk2);
		signer.update(msgToSign, 0, 32);
		return signer.generateSignature();
	}
	
	public static boolean signatureIsCorrect(byte[] signedData, byte[] signatureToVerify, byte[] pkAsBytes, DataOutputStream out, DataInputStream in) throws InvalidKeyException, SignatureException, InvalidKeySpecException, NoSuchAlgorithmException, IOException, org.apache.commons.codec.DecoderException{
		// Edwards-curve Digital Signature Algorithm (EdDSA) 
		// bonnes performances + en évitant les problèmes de sécurité qui sont apparus dans les autres schémas 
		// résistance aux attaques comparable à celle des chiffrements de 128-bits de qualité
		// неизвестны субэкспоненциальные алгоритмы дискретного логарифмирования
		// EdDSA calcule ce nonce unique pour chaque signature =hachage(sk, data), plutôt que de dépendre d'un générateur de nombre aléatoire => réduit les risques d'une attaque sur le générateur de nombres aléatoires, sans l'éliminer complètement lorsque des nombres aléatoires sont utilisés pour la génération des clés
		SubjectPublicKeyInfo pkInfo               = new SubjectPublicKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519),pkAsBytes);
		X509EncodedKeySpec   keySpec              = new X509EncodedKeySpec(pkInfo.getEncoded());
		BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
		Signature            signatureToVerify2   = Signature.getInstance("Ed25519",bouncyCastleProvider);
		PublicKey            pkAsPublicKey        = KeyFactory.getInstance("Ed25519",bouncyCastleProvider).generatePublic(keySpec);
		signatureToVerify2.initVerify(pkAsPublicKey);
		signatureToVerify2.update(signedData);
		return signatureToVerify2.verify(signatureToVerify);
	 }
	
	///////////// CONVERTERS

	public static String toHexString(byte[] bytes) {
		if(bytes==null || bytes.length==0) return "";
		final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
	    byte[] hexChars = new byte[bytes.length * 2];
	    for (int j = 0; j < bytes.length; j++) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	    }
	    return new String(hexChars, StandardCharsets.UTF_8);
	}
	
	public static long toDateAsSeconds(String dateAsString) throws ParseException { 
		DateTimeFormatter formatter     = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC")); 
		LocalDateTime     localDateTime = LocalDateTime.parse(dateAsString, formatter);
		return localDateTime.atZone(ZoneId.of("UTC")).toEpochSecond(); 
	}
	
	public static long currentDateTimeAsSeconds() {
		return LocalDateTime.now(ZoneId.of("UTC")).atZone(ZoneId.of("UTC")).toEpochSecond(); 
	}
	
	public static int toInt(byte[] bytes) {
	    return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	}

	public static short toShort(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getShort();
	}

	public static long toLong(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getLong();
	}
	
	public static byte[] to2BytesArray(int n) { 
		ByteBuffer convertedToBytes = ByteBuffer.allocate(2);
		convertedToBytes.putShort((short)n);
		return convertedToBytes.array();
	}
	
	public static byte[] to4BytesArray(int n) {
		ByteBuffer convertedToBytes = ByteBuffer.allocate(4);
		convertedToBytes.putInt(n);
		return convertedToBytes.array();
	}
	
	public static byte[] to8BytesArray(long n) { 
		ByteBuffer convertedToBytes = ByteBuffer.allocate(8);
		convertedToBytes.putLong(n);
		return convertedToBytes.array();
	}
	
	public static byte[] to32bytesArray(int n) { 
		ByteBuffer convertedToBytes = ByteBuffer.allocate(32);
		convertedToBytes.putInt(n);
		return convertedToBytes.array();
	}
	
	public static String toStringOfHex(int n) {
		return toHexString(to4BytesArray(n));
	}
	
	public static byte[] toBytesArray(char[] charArray) throws DecoderException {
		return Hex.decodeHex(charArray);
	}
	
	public static byte[] toBytesArray(String str) throws DecoderException {
		return Hex.decodeHex(str.toCharArray());
	}
	
	public static String toDateAsString(long seconds) { 
		LocalDateTime     dateTime      = LocalDateTime.ofEpochSecond(seconds, 0, ZoneOffset.UTC);
		DateTimeFormatter formatter     = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String            formattedDate = dateTime.format(formatter);
		return formattedDate.toString();
	}

	public static String toDateAsString(byte[] dateAsBytes) { 
		LocalDateTime     dateTime      = LocalDateTime.ofEpochSecond(toLong(dateAsBytes), 0, ZoneOffset.UTC);
		DateTimeFormatter formatter     = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String            formattedDate = dateTime.format(formatter);
		return formattedDate.toString();
	}

	public static byte[] concatArrays(byte[] a, byte[] b) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(a);
		outputStream.write(b);
		return outputStream.toByteArray(); 
	}

	public static byte[] concatArrays(byte[] a, byte[] b, byte[] c) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(a);
		outputStream.write(b);
		outputStream.write(c);
		return outputStream.toByteArray(); 
	}

	public static byte[] concatArrays(byte[] a, byte[] b, byte[] c, byte[] d) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(a);
		outputStream.write(b);
		outputStream.write(c);
		outputStream.write(d);
		return outputStream.toByteArray(); 
	}

	public static byte[] concatArrays(byte[] a, byte[] b, byte[] c, byte[] d, byte[] e) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(a);
		outputStream.write(b);
		outputStream.write(c);
		outputStream.write(d);
		outputStream.write(e);
		return outputStream.toByteArray(); 
	}		
}
