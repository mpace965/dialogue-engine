import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class Dialogue
{
	private Document doc; //necessary for the XML document
	private XPath xpath; //The parser
	private XPathExpression expr; //The expression you pass to the parser
	
	private ArrayList<String> curDialog; //Contains the current dialog. 0 is the message displayed, 1-... are the answers
	private ArrayList<Boolean> curIsNextId; //Contains whether the answers go to next dialogs, or subdialogs. 0 corresponds to 1 in curDialog, 2 to 1 and so on
	private String path; //Current path for the Xpath, in a String
	
	public Dialogue(String s)
	{
		try
		{
			File fXmlFile = new File(s);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(fXmlFile); //All this loads the xml file
			
			xpath = XPathFactory.newInstance().newXPath(); //create a new Xpath instance
			
			curDialog = new ArrayList<String>();
			curIsNextId = new ArrayList<Boolean>();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void start() throws XPathExpressionException
	{
		setCurrentDialog("root");
		printCurrentDialog();
		
		input();
	}
	
	private void input() throws XPathExpressionException
	{
		Scanner s = new Scanner(System.in);
		System.out.println("Choice: ");
		int input = s.nextInt();
		

		expr = xpath.compile(path+"/answers/*/@goto"); /*compile the expression to the Xpath. This one means whatever path we are at, answers,
		* = wildcard, so any element held in answers, select their goto attribute (see xml doc). More info: http://www.w3schools.com/xpath/xpath_syntax.asp */
		NodeList nl = getNodeList(expr); //Build the expression to a NodeList. Similar to ArrayList, but has way less features, and is for XML
			
		if (curIsNextId.get(input)) //ArrayList of booleans. Whether the input is type next or type sub
		{
			try
			{
				setCurrentDialog(nl.item(input).getNodeValue()); //nl.item similar to get(), returns a Node. You then have to use an accessor method to retrieve the Node's data
				printCurrentDialog();
				
				input();
			}
			catch (Exception e)
			{
				System.out.println("Exit."); //Occurs when the path is invalid. The only time this occurs is when the goto is "quit"
			}
		}
		else //This one is for subdialogs, the overrided method setCurrentDialog(String, String) needs dialog id and subdialog id
		{
			String subId = nl.item(input).getNodeValue(); 
			
			try //Case: we came from a dialog
			{
				expr = xpath.compile(path+"/@id"); //Gets the id of the dialog we just came from
				nl = getNodeList(expr);
				String nextId = nl.item(0).getNodeValue();
				
				setCurrentDialog(nextId, subId);
				printCurrentDialog();
				
				input();
			}
			catch (Exception e) //Case: we just came from a subdialog. In this case, the path is (roughly) in the form /<dialog id>/<subdialog id>/
			{
				expr = xpath.compile(path+"/../@id"); //So we need to go up one more level to get the dialog id. .. refers to a parent in the path
				nl = getNodeList(expr);
				String nextId = nl.item(0).getNodeValue();
				
				setCurrentDialog(nextId, subId);
				printCurrentDialog();
				
				input();
			}
		}
		
		s.close();
	}
	
	private void printCurrentDialog()
	{
		System.out.println(curDialog.get(0));
		
		for(int i = 1; i < curDialog.size(); i++)
			System.out.println("\t"+curDialog.get(i));
		
		System.out.println();
	}
	
	private void setCurrentDialog(String id) throws XPathExpressionException
	{			
		path = "//dialog[@id=\'"+id+"\']"; //set the path to the dialog with id passed in. // in Xpath means "any in the document." So this is any dialog with id
		proccessDialog();
	}
	
	private void setCurrentDialog(String id, String sid) throws XPathExpressionException
	{		
		path = "//dialog[@id=\'"+id+"\']//subdialog[@id=\'"+sid+"\']"; //basically same as above, but with a modified path for subdialogs
		proccessDialog();
	}
	
	private void proccessDialog() throws XPathExpressionException /*This can be the same for subdialogs and dialogs, because they are laid out the exact same way,
	but the path has to be modified. This is done in setCurrentDialog(String) and setCurrentDialog(String, String) respectively*/
	{
		NodeList nodes;
		
		clearLists(); //clear curDialog and curIsNextId
		
		expr = xpath.compile(path+"/message/text()");
		nodes = getNodeList(expr);
		
		String message = nodes.item(0).getNodeValue();
		curDialog.add(message); //gets the message text and adds it to the curDialog
		
		expr = xpath.compile(path+"/answers/*/text()"); //wild card again. basically anything in the answers element (which are individual answer elements.)
		nodes = getNodeList(expr);
		
		for (int i = 0; i < nodes.getLength(); i++)
			curDialog.add(nodes.item(i).getNodeValue()); //Loop through the NodeList, adding each Node's contents to curDialog
		
		expr = xpath.compile(path+"/answers/*/@type"); //wild card, this time with the attribute type of the answer elements
		nodes = getNodeList(expr);
		
		for (int i = 0; i < nodes.getLength(); i++)
			curIsNextId.add(toBoolean(nodes.item(i).getNodeValue())); //Loop through the NodeList, adding each Node's contents (converted to a  boolean)
	}
	
	private NodeList getNodeList(XPathExpression expr) throws XPathExpressionException
	{
		Object result = expr.evaluate(doc, XPathConstants.NODESET); //evaluate the expression to get an object
		NodeList nodes = (NodeList) result; //This object is in the format of a NodeList, so we cast it and return it
		
		return nodes;
	}
	
	private void clearLists()
	{
		while (curDialog.size() != 0)
			curDialog.remove(0);
		
		while (curIsNextId.size() != 0)
			curIsNextId.remove(0);
	}
	
	private boolean toBoolean(String s)
	{
		if (s.equals("next"))
			return true;
		else
			return false;
	}
}
