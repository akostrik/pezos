package pezos;

@SuppressWarnings("serial")
public class BroadcastInsteadOfAnswerException extends Exception{
    private byte[] broadcast;
    
    public BroadcastInsteadOfAnswerException (String message, byte[] unplannedMessage) {
        super(message);
        this.broadcast=unplannedMessage;
    }

    public byte[] getUnexpectedBroadcast(){
    	return broadcast;
    }
}