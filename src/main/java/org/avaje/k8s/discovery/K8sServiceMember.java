package org.avaje.k8s.discovery;

public class K8sServiceMember {

	private String ipAddress;
	private String nodeName;
	private String podName;

	public K8sServiceMember(String ipAddress, String nodeName, String podName) {
		this.ipAddress = ipAddress;
		this.nodeName = nodeName;
		this.podName = podName;
	}

	public String toString() {
		return "ip:" + ipAddress + " pod:" + podName + " node:" + nodeName;
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
