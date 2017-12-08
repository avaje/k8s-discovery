package org.avaje.k8s.discovery;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper that parses the JSON content to List of K8sServiceMember.
 *
 * Ugly but means no dependencies on JSON parsing tools (like Jackson) and
 * we prefer not to require any extra dependencies.
 */
class MemberParser {

	private static final String SUBSETS = "\"subsets\":[";
	private static final String ADDRESSES = "\"addresses\":[";
	private static final String END_QUOTE = "\"";

	private final String rawJson;

	private int pos;

	private List<K8sServiceMember> members = new ArrayList<>();

	/**
	 * Create with raw JSON content.
	 */
	MemberParser(String rawJson) {
		this.rawJson = rawJson;
	}

	/**
	 * Parse returning the list of members.
	 */
	public List<K8sServiceMember> parseJson() {

		pos = rawJson.indexOf(SUBSETS);
		if (pos > -1) {
			pos = rawJson.indexOf(ADDRESSES, pos);
			if (pos > -1) {
				// loop find each member
				String ip;
				while((ip = readProperty("ip")) != null) {
					readEntry(ip);
				}
			}
		}

		return members;
	}

	/**
	 * Read a JSON property given we know it is a json string and no other type and has no escaping of quotes.
	 */
	private String readProperty(String propertyName) {
		String key = jsonKey(propertyName);
		// find "ip":"
		pos = rawJson.indexOf(key, pos);
		if (pos > -1) {
			int startPos = pos + key.length();
			pos = rawJson.indexOf(END_QUOTE, startPos + 1);
			if (pos == -1) {
				throw new IllegalStateException("Can't find end quote for property " + propertyName);
			} else {
				return rawJson.substring(startPos, pos);
			}
		}
		return null;
	}

	/**
	 * Read and build the K8sServiceMember.
	 */
	private void readEntry(String ipAddress) {
		String nodeName = readProperty("nodeName");
		String podName = readProperty("name");
		members.add(new K8sServiceMember(ipAddress, nodeName, podName));
	}

	private String jsonKey(String property) {
		return "\"" + property + "\":\"";
	}
}
