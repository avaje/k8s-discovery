package org.avaje.k8s.discovery;

public class K8sServiceMember {

	private String ipAddress;
	private String nodeName;
	private String podName;

	private boolean ready;

	public K8sServiceMember(String ipAddress, String nodeName, String podName, boolean ready) {
		this.ipAddress = ipAddress;
		this.nodeName = nodeName;
		this.podName = podName;
		this.ready = ready;
	}

	public String toString() {
		return "ip:" + ipAddress + " pod:" + podName + " node:" + nodeName;
	}

	/**
	 * Change the ready status.
	 */
	public void setReady(boolean ready) {
		this.ready = ready;
	}

	/**
	 * Return true if the member was in the notReady list.
	 */
	public boolean isReady() {
		return ready;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getNodeName() {
		return nodeName;
	}

	public String getPodName() {
		return podName;
	}
}
