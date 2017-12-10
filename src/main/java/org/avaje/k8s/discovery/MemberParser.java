package org.avaje.k8s.discovery;

/**
 * Helper that parses the JSON content to List of K8sServiceMember.
 *
 * Ugly but means no dependencies on JSON parsing tools (like Jackson) and
 * we prefer not to require any extra dependencies.
 */
class MemberParser {

	private static final String SUBSETS = "\"subsets\":[";
	private static final String ADDRESSES = "\"addresses\":[";
	private static final String NOT_READY_ADDRESSES = "\"notReadyAddresses\":[";
	private static final String END_QUOTE = "\"";

	private final String rawJson;

	private int pos;

	private K8sServiceMembers members = new K8sServiceMembers();

	/**
	 * Create with raw JSON content.
	 */
	MemberParser(String rawJson) {
		this.rawJson = rawJson;
	}

	/**
	 * Parse returning the list of members.
	 */
	public K8sServiceMembers parseJson() {

		int subsets = rawJson.indexOf(SUBSETS);
		if (subsets > -1) {
			pos = rawJson.indexOf(ADDRESSES, subsets);
			if (pos > -1) {
				readAddresses(true);
			}
			pos = rawJson.indexOf(NOT_READY_ADDRESSES, subsets);
			if (pos > -1) {
				readAddresses(false);
			}
		}

		return members;
	}

	private void readAddresses(boolean ready) {
		// loop find each member
		String ip;
		while((ip = readProperty("ip")) != null) {
			readEntry(ip, ready);
		}
	}

	/**
	 * Read a JSON property given we know it is a json string and no other type and has no escaping of quotes.
	 */
	private String readProperty(String propertyName) {
		String key = jsonKey(propertyName);
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
	private void readEntry(String ipAddress, boolean ready) {
		String nodeName = readProperty("nodeName");
		String podName = readProperty("name");
		members.add(ready, new K8sServiceMember(ipAddress, nodeName, podName, ready));
	}

	private String jsonKey(String property) {
		return "\"" + property + "\":\"";
	}
}
