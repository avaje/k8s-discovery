package org.avaje.k8s.discovery;

import java.util.ArrayList;
import java.util.List;

/**
 * Pod members of a specific service (or label)
 */
public class K8sServiceMembers {

	private final List<K8sServiceMember> members = new ArrayList<>();
	private final List<K8sServiceMember> notReady = new ArrayList<>();

	public K8sServiceMembers() {
	}

	public String toString() {
		return "members:" + members + " notReady:" + notReady;
	}

	public void add(boolean ready, K8sServiceMember member) {
		if (ready) {
			members.add(member);
		} else {
			notReady.add(member);
		}
	}

	public boolean isEmpty() {
		return members.isEmpty();
	}

	/**
	 * Return the ready members.
	 */
	public List<K8sServiceMember> getMembers() {
		return members;
	}

	/**
	 * Return the not ready members.
	 */
	public List<K8sServiceMember> getNotReady() {
		return notReady;
	}

	/**
	 * Find the member searching the notReady members as well.
	 */
	public K8sServiceMember findPod(String podName) {
		K8sServiceMember member = findPod(podName, members);
		if (member == null) {
			member = findPod(podName, notReady);
		}
		return member;
	}

	/**
	 * Find the member given the podName in the given list
	 * which could be the members or notReady list etc.
	 */
	public K8sServiceMember findPod(String podName, List<K8sServiceMember> list) {
		return list.stream()
				.filter(it -> podName.equals(it.getPodName()))
				.findFirst().orElse(null);
	}

}
