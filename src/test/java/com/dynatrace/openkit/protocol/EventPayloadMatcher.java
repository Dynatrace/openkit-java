package com.dynatrace.openkit.protocol;

import java.util.Arrays;

import org.mockito.ArgumentMatcher;

/**
 * @author matthias.hochrieser
 */
class EventPayloadMatcher extends ArgumentMatcher<String> {

	private final String expectedEventPayload;

	public EventPayloadMatcher(String expectedEventPayload){
		this.expectedEventPayload = expectedEventPayload;
	}

	@Override
	public boolean matches(Object argument) {
		String actualEventPayload = (String) argument;

		if(expectedEventPayload.contentEquals(actualEventPayload)){
			return true;
		}

		// Check if both of them are not null
		if(expectedEventPayload == null ^ actualEventPayload == null){
			return false;
		}

		// Check for beginning of event payload
		if(!(expectedEventPayload.startsWith("et=98&pl=") && actualEventPayload.startsWith("et=98&pl="))){
			return false;
		}

		String expectedPayload = expectedEventPayload.substring(expectedEventPayload.indexOf("pl=") + "pl=".length());
		String actualPayload = actualEventPayload.substring(actualEventPayload.indexOf("pl=") + "pl=".length());

		String[] expectedPayloadValues = expectedPayload.split("%2C");
		Arrays.sort(expectedPayloadValues);

		String[] actualPayloadValues = actualPayload.split("%2C");
		Arrays.sort(actualPayloadValues);

		return Arrays.equals(expectedPayloadValues, actualPayloadValues);
	}
}
