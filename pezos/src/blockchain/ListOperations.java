package blockchain;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import pezos.Utils;

public class ListOperations {
	private ArrayList<Operation> listOperations = null;
	private int level;
	
	public ListOperations(byte[] receivedMessage, int level) {
		listOperations = new ArrayList<Operation>();
		this.level=level;
		extractFirstOperation(Arrays.copyOfRange(receivedMessage,4,receivedMessage.length));
	}
	
	public ListOperations(byte[] receivedMessage) {
		this(receivedMessage,0);
		//System.out.println("conctructeur ListOperations "+receivedMessage);
	}

	public void extractFirstOperation(byte[] operationsAsBytes) {
		if(operationsAsBytes.length<1)
			return;
		
		int tag = Utils.toInt(Arrays.copyOfRange(operationsAsBytes,0,2));
		int nextPosition=0;
		if (tag==1 || tag==3 || tag==4 ) {
			nextPosition=130;
		} else if (tag == 2) {
			nextPosition=106;
		} else if (tag == 5) {
			nextPosition=98;
		} else
			return;
		listOperations.add(new Operation(Arrays.copyOfRange(operationsAsBytes,0,nextPosition)));
		extractFirstOperation(Arrays.copyOfRange(operationsAsBytes,nextPosition,operationsAsBytes.length));
	}
	
	public byte[] getHash() throws IOException {
		return getHash(listOperations);
	}

	public byte[] getHash(ArrayList<Operation> listOperationsLoc) throws IOException {
		if (listOperationsLoc.size() == 0) 
			return Utils.to32bytesArray(0);
		if (listOperationsLoc.size() == 1) 
			return Utils.hash(listOperationsLoc.get(0).getAsBytes(),32);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] hashLastOperation = Utils.hash(listOperationsLoc.get(listOperationsLoc.size()-1).getAsBytes(),32);
		byte[] hashAllOperationExceptTheLast = getHash(new ArrayList<Operation>(listOperationsLoc.subList(0,listOperationsLoc.size()-1))); 
		outputStream.write(hashAllOperationExceptTheLast);
		outputStream.write(hashLastOperation);
		return Utils.hash(outputStream.toByteArray(),32);
	}

	public String toString() {
		String result = "LISTE OPERATIONS "+(level==0?"":"of the level "+level)+", "+listOperations.size()+" operation(s) : \n";
		for(Operation operation : listOperations)
			result += "  "+operation.toString()+"\n";
		return result.substring(0,result.length()-1);
	}
}