package co.yodo.pos.fare.serverconnection;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLHandler extends DefaultHandler {
	Boolean currentElement = false;
	String currentValue = null;
	
	public static HashMap<String, String> responseValues = null;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		String attr = "";
		currentElement = true;
		if (localName.equalsIgnoreCase(ServerResponse.SUB_ROOT_ELEMENT))
		{
			/** Start */
			responseValues = new HashMap<String, String>();
		} 
		else if (localName.equalsIgnoreCase(ServerResponse.CODE_ELEM)) {
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.CODE_ELEM);
			responseValues.put(ServerResponse.CODE_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.AUTH_NUM_ELEM)){
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.AUTH_NUM_ELEM);
			responseValues.put(ServerResponse.AUTH_NUM_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.MESSAGE_ELEM)){
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.MESSAGE_ELEM);
			responseValues.put(ServerResponse.MESSAGE_ELEM, attr);
		}
		// response parameters
		else if (localName.equalsIgnoreCase(ServerResponse.BALANCE_ELEM)){
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.BALANCE_ELEM);
			responseValues.put(ServerResponse.BALANCE_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.DEBIT_ELEM)) {
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.DEBIT_ELEM);
			responseValues.put(ServerResponse.DEBIT_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.CREDIT_ELEM)) {
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.CREDIT_ELEM);
			responseValues.put(ServerResponse.CREDIT_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.SETTLEMENT_ELEM)) {
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.SETTLEMENT_ELEM);
			responseValues.put(ServerResponse.SETTLEMENT_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.EQUIPMENTS_ELEM)) {
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.EQUIPMENTS_ELEM);
			responseValues.put(ServerResponse.EQUIPMENTS_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.LEASE_ELEM)) {
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.LEASE_ELEM);
			responseValues.put(ServerResponse.LEASE_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.TOTAL_LEASE_ELEM)) {
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.TOTAL_LEASE_ELEM);
			responseValues.put(ServerResponse.TOTAL_LEASE_ELEM, attr);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.PARAMS_ELEM)){
			/** Get attribute value */
			attr = attributes.getValue(ServerResponse.PARAMS_ELEM);
			responseValues.put(ServerResponse.PARAMS_ELEM, attr);
		}
	}

	/** Called when tag closing ( ex:- <name>AndroidPeople</name>
	* -- </name> )*/
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		currentElement = false;
		
		/** set value */
		if (localName.equalsIgnoreCase(ServerResponse.CODE_ELEM)) {
			/** Get attribute value */
			responseValues.put(ServerResponse.CODE_ELEM, currentValue);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.AUTH_NUM_ELEM)){
			/** Get attribute value */
			responseValues.put(ServerResponse.AUTH_NUM_ELEM, currentValue);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.MESSAGE_ELEM)){
			/** Get attribute value */
			responseValues.put(ServerResponse.MESSAGE_ELEM, currentValue);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.DEBIT_ELEM)) {
			/** Get attribute value */
			String params = responseValues.get(ServerResponse.PARAMS_ELEM);
			responseValues.put(ServerResponse.DEBIT_ELEM, currentValue);
			
			if(params != null)
				responseValues.put(ServerResponse.PARAMS_ELEM, params + ServerResponse.ENTRY_SEPARATOR + ServerResponse.DEBIT_ELEM + ServerResponse.VALUE_SEPARATOR + " " + currentValue);
			else
				responseValues.put(ServerResponse.PARAMS_ELEM, ServerResponse.DEBIT_ELEM + ServerResponse.VALUE_SEPARATOR + " " + currentValue);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.CREDIT_ELEM)) {
			/** Get attribute value */
			String params = responseValues.get(ServerResponse.PARAMS_ELEM);
			responseValues.put(ServerResponse.CREDIT_ELEM, currentValue);
			
			if(params != null)
				responseValues.put(ServerResponse.PARAMS_ELEM, params + ServerResponse.ENTRY_SEPARATOR + ServerResponse.CREDIT_ELEM + ServerResponse.VALUE_SEPARATOR + " " + currentValue);
			else
				responseValues.put(ServerResponse.PARAMS_ELEM, ServerResponse.CREDIT_ELEM + ServerResponse.VALUE_SEPARATOR + " " + currentValue);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.SETTLEMENT_ELEM)) {
			/** Get attribute value */
			String params = responseValues.get(ServerResponse.PARAMS_ELEM);
			responseValues.put(ServerResponse.SETTLEMENT_ELEM, currentValue);
			
			if(params != null)
				responseValues.put(ServerResponse.PARAMS_ELEM, params + ServerResponse.ENTRY_SEPARATOR + ServerResponse.SETTLEMENT_ELEM + ServerResponse.VALUE_SEPARATOR + " " + currentValue);
			else
				responseValues.put(ServerResponse.PARAMS_ELEM, ServerResponse.SETTLEMENT_ELEM + ServerResponse.VALUE_SEPARATOR + " " + currentValue);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.EQUIPMENTS_ELEM)) {
			/** Get attribute value */
			String params = responseValues.get(ServerResponse.PARAMS_ELEM);
			responseValues.put(ServerResponse.EQUIPMENTS_ELEM, currentValue);
			
			if(params != null)
				responseValues.put(ServerResponse.PARAMS_ELEM, params + ServerResponse.ENTRY_SEPARATOR + ServerResponse.EQUIPMENTS_ELEM + ServerResponse.VALUE_SEPARATOR + " " + currentValue);
			else
				responseValues.put(ServerResponse.PARAMS_ELEM, ServerResponse.EQUIPMENTS_ELEM + ServerResponse.VALUE_SEPARATOR + " " + currentValue);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.LEASE_ELEM)) {
			/** Get attribute value */
			String params = responseValues.get(ServerResponse.PARAMS_ELEM);
			responseValues.put(ServerResponse.LEASE_ELEM, currentValue);
			
			if(params != null)
				responseValues.put(ServerResponse.PARAMS_ELEM, params + ServerResponse.ENTRY_SEPARATOR + ServerResponse.LEASE_ELEM + ServerResponse.VALUE_SEPARATOR + " " + currentValue);
			else
				responseValues.put(ServerResponse.PARAMS_ELEM, ServerResponse.LEASE_ELEM + ServerResponse.VALUE_SEPARATOR + " " + currentValue);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.TOTAL_LEASE_ELEM)) {
			/** Get attribute value */
			String params = responseValues.get(ServerResponse.PARAMS_ELEM);
			responseValues.put(ServerResponse.TOTAL_LEASE_ELEM, currentValue);
			
			if(params != null)
				responseValues.put(ServerResponse.PARAMS_ELEM, params + ServerResponse.ENTRY_SEPARATOR + ServerResponse.TOTAL_LEASE_ELEM + ServerResponse.VALUE_SEPARATOR + " " + currentValue);
			else
				responseValues.put(ServerResponse.PARAMS_ELEM, ServerResponse.TOTAL_LEASE_ELEM + ServerResponse.VALUE_SEPARATOR + " " + currentValue);
		}
		// response parameters
		else if (localName.equalsIgnoreCase(ServerResponse.BALANCE_ELEM)){
			/** Get attribute value */
			
			String params = responseValues.get(ServerResponse.PARAMS_ELEM);
			responseValues.put(ServerResponse.BALANCE_ELEM, currentValue);
			
			if(params != null)
				responseValues.put(ServerResponse.PARAMS_ELEM, params + ServerResponse.ENTRY_SEPARATOR + ServerResponse.BALANCE_ELEM + ServerResponse.VALUE_SEPARATOR + currentValue);
			else
				responseValues.put(ServerResponse.PARAMS_ELEM, ServerResponse.BALANCE_ELEM + ServerResponse.VALUE_SEPARATOR+currentValue);
		}
		else if (localName.equalsIgnoreCase(ServerResponse.PARAMS_ELEM)){
			/** Get attribute value */
			if(currentValue != null && currentValue != ""){
				//check if I have this value already inside any other tag
				String paramsValue = responseValues.get(ServerResponse.PARAMS_ELEM);

				if(paramsValue != null & paramsValue != ""){
					//if we don't have it yet, then we add it
					if(!paramsValue.contains(currentValue))
						responseValues.put(ServerResponse.PARAMS_ELEM, currentValue + ServerResponse.ENTRY_SEPARATOR + paramsValue);
				}
				else 
					responseValues.put(ServerResponse.PARAMS_ELEM, currentValue);
			}
		} 
	}

	/** Called to get tag characters ( ex:- <name>AndroidPeople</name>
	* -- to get AndroidPeople Character ) */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if(currentElement) {
			currentValue = new String(ch, start, length);
			currentElement = false;
		}
	}
}
