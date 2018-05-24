@SuppressWarnings("serial")
public class ErrorThrower extends Exception {
	public ErrorThrower(String arg){
		super("Exception generated:" + arg + ".");
	}
}
