package org.avaje.k8s.discovery;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class K8sMemberDiscoveryTest {


	private List<K8sServiceMember> members() {
		List<K8sServiceMember> list = new ArrayList<>();
		list.add(new K8sServiceMember("1.2.3.4", "foo", "pod1"));
		list.add(new K8sServiceMember("1.2.3.5", "bar", "pod2"));
		list.add(new K8sServiceMember("1.2.3.6", "baz", "pod3"));
		return list;
	}

	@Test
	public void findOtherIps() {

		List<String> ips = new TdDiscovery("")
				.setPodName("pod2")
				.findOtherIps();

		assertEquals(2 , ips.size());
		assertEquals("1.2.3.4" , ips.get(0));
		assertEquals("1.2.3.6" , ips.get(1));
	}

	@Test
	public void findAllIps() {

		List<String> ips = new TdDiscovery("")
				.setPodName("pod2")
				.findAllIps();

		assertEquals(3 , ips.size());
		assertEquals("1.2.3.4" , ips.get(0));
		assertEquals("1.2.3.5" , ips.get(1));
		assertEquals("1.2.3.6" , ips.get(2));
	}

	@Test
	public void findOtherMembers() {

		List<K8sServiceMember> members = new TdDiscovery("")
				.setPodName("pod2")
				.findOtherMembers();

		assertEquals(2 , members.size());
		assertEquals("ip:1.2.3.4 pod:pod1 node:foo" , members.get(0).toString());
		assertEquals("ip:1.2.3.6 pod:pod3 node:baz" , members.get(1).toString());
	}


	@Test
	public void findAllMembers() {

		List<K8sServiceMember> members = new TdDiscovery("")
				.setPodName("pod2")
				.findAllMembers();

		assertEquals(3 , members.size());
		assertEquals("ip:1.2.3.4 pod:pod1 node:foo" , members.get(0).toString());
		assertEquals("ip:1.2.3.5 pod:pod2 node:bar" , members.get(1).toString());
		assertEquals("ip:1.2.3.6 pod:pod3 node:baz" , members.get(2).toString());
	}

	class TdDiscovery extends K8sMemberDiscovery {

		public TdDiscovery(String serviceName) {
			super(serviceName);
		}

		@Override
		protected List<K8sServiceMember> fetchAllMembers() {
			return members();
		}
	}
}