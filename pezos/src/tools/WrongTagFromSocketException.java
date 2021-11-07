package tools;
@SuppressWarnings("serial")

public class WrongTagFromSocketException extends Exception {
	
    public WrongTagFromSocketException(String message){
        super(message);
    }
}