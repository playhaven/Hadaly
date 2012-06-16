package com.jayway.android.robotium.solo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which represents a simple jquery-like selector for traversing the view hierarchy.
 * As expected, class selectors map to Java classes hierarchical selection is not
 * "direct-descendent" restricted. IDs can be either an integer ID or a string which we
 * match first against the native int ID, then against the accessibility 'contentDescription'
 * and then against the view "tag" object. You can also use attribute selectors but the 
 * view must have the property accessible via a 'getPropName' style accessor.
 * 
 * Of course, you can specify multiple classes/interfaces which indicate inheritance, etc.
 * 
 * Note: The more specific a selector the more efficient the search.
 * 
 * Note: Tag selectors ('a li m') are not useful.
 * 
 * Eventually, we'll support the full feature set of jquery.
 * 
 * Note: if you need spaces in your selector (say in a content description) you must use underscores
 * for the spaces. These will be replaced internally....
 * @author samstewart
 *
 */
public class Selector {
	
	private String curSelector 				= "";
	
	private Selector child 					= null; // direct descendant
	
	private int integerID 					= -1;
	
	private String stringID				    = "";
	
	private HashMap<String, String> attributes = new HashMap<String, String>();
	
	private ArrayList<String> classes 		= new ArrayList<String>();
	
	private final String CHILD_SEPARATOR 	= " ";
	
	// tool for testing regex: http://regexpal.com/
	
	private final String CLASS_REGEX 		= "((?:\\.\\w+)+)";
	
	private final String ID_REGEX 			= "#(\\w+)";
	
	private final String ATTRIBUTE_REGEX 	= "\\[(\\w+)='([^\\]]+)'\\]";
	
	public Selector(String selectorStr) {
		parseSelectorHierarchy(selectorStr);
	}
	
	/**
	 * Parses a single node selector instead of an entire
	 * hierarchy string (ex: '#more_games')
	 * 
	 * Note: The groupCount method returns all the groups
	 * in the /original/ regex. We use the "find" method to iterate
	 * over the matches.
	 * @param selector The single node selector
	 */
	private void parseSelector(String selector) {
		
		// parse the class selectors
		Pattern pattern = Pattern.compile(CLASS_REGEX);
		Matcher matcher = pattern.matcher(selector);
		
		if (matcher.find()) {
			String allClasses = matcher.group(1);
			
			
			System.out.println("all classes: "+allClasses);
			classes = new ArrayList<String>(Arrays.asList(allClasses.split("\\.")));
			
			// take out blank first element
			classes.remove(0);
			
			// strip off the class section
			selector = matcher.replaceAll("");
		}
		
		// parse the iD
		pattern = Pattern.compile(ID_REGEX);
		matcher = pattern.matcher(selector);
		
		if (matcher.find()) {
			String idStr = matcher.group(1);
			
			
			
			try {
				integerID = Integer.parseInt(idStr);
			} catch (NumberFormatException e) {
				integerID = -1;
				stringID = idStr;
				System.out.println("ID String: "+ stringID);
			}
			
			// strip all of the IDs out
			selector = matcher.replaceAll("");
		}
		
		// parse the attributes
		pattern = Pattern.compile(ATTRIBUTE_REGEX);
		matcher = pattern.matcher(selector);
					
		// build keys 
		while (matcher.find()) {
			String key = matcher.group(1);
			
			String value = matcher.group(2).replace("_", " ");
			
			System.out.println("Key: " + key + " Value: " + value);
			attributes.put(matcher.group(1), matcher.group(2).replace("_", " "));
		}
		
		// strip all the attributes out
		selector = matcher.replaceAll("");
	
	}
	
	/** 
	 * Parses an entire selector string ('a li.testcass #more_games') as opposed to a single 'node' ('#more_games')
	 * @param fullSelector The entire hierarchy selector
	 */
	private void parseSelectorHierarchy(String fullSelector) {
		
		// pop front selector off, parse, and then recursively parse remainder
		curSelector = (fullSelector.indexOf(" ") != -1 				 ? 
					   fullSelector.substring(0, fullSelector.indexOf(" ")) : 
					   fullSelector);
		
		System.out.println("Cur Selector: "+curSelector);
		
		parseSelector(curSelector);
		
		// parse remainder of the string
		if (fullSelector.indexOf(" ") != -1) child = new Selector(fullSelector.substring(fullSelector.indexOf(" ") + 1)); 
	}
	
	///////////////////////////////////////////////
	//////////  Accessors /////////////////////////
	
	public int getIntegerID() {
		return integerID;
	}
	
	public ArrayList<String> getClasses() {
		return classes;
	}
	
	public String getStringID() {
		return stringID;
	}
	
	public HashMap<String, String> getAttributes() {
		return attributes;
	}
	
	public Selector getChild() {
		return child;
	}
	
	public boolean hasChild() {
		return (getChild() != null);
	}
}
