import javax.xml.xpath.XPathExpressionException;

public class Driver
{
	public static void main(String[] args) throws XPathExpressionException
	{		
		Dialogue recept = new Dialogue("./res/recept.xml");
		recept.start();
	}
}
