import javax.xml.xpath.XPathExpressionException;

public class driver
{
	public static void main(String[] args) throws XPathExpressionException
	{		
		Dialogue recept = new Dialogue("./res/recept.xml");
		recept.start();
	}
}