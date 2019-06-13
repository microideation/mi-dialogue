package com.microideation.app.dialogue.rsocket.utils;

import com.microideation.app.dialogue.support.exception.DialogueException;
import com.microideation.app.dialogue.support.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RSocketGeneralUtils {
	
	
	/**
	 * Method to split the address based on the data provided
	 * First checks whether the data is in correct format
	 *
	 * @param address : address in the format ( hostname:port)
	 * @return : Return the array of split
	 */
	public static String[] splitAddress(String address) {
		
		// Check if the separator is present ( : )
		if (address == null ||
				address.indexOf(":") == -1 ||
					address.split(":").length != 2) {
			
			// Log the error
			log.error("splitAddress -> Invalid format - Expected ( hostname:port ), provided: " + address);
			
			// throw Exception
			throw new DialogueException(ErrorCode.ERR_INVALID_PARAMETERS,
					"Invalid format - Expected ( hostname:port ), provided: " + address
			);
			
		}
		
		// Return the split values
		return address.split(":");
		
	}
	
}
