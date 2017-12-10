package org.avaje.k8s.discovery;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class K8sMemberDiscoveryTest {


	private K8sServiceMembers members() {
		K8sServiceMembers members = new K8sServiceMembers();
		members.add(true, new K8sServiceMember("1.2.3.4", "foo", "pod1", true));
		members.add(true, new K8sServiceMember("1.2.3.5", "bar", "pod2", true));
		members.add(true, new K8sServiceMember("1.2.3.6", "baz", "pod3", true));
		return members;
	}

	@Test
	public void readSystemProperty() {

		System.setProperty("POD_NAMESPACE", "dev42");
		System.setProperty("POD_NAME", "pod42");

		K8sMemberDiscovery discovery = new K8sMemberDiscovery("service42");
		assertEquals("pod42", discovery.podName);
		assertEquals("dev42", discovery.namespace);
		assertEquals("service42", discovery.serviceName);
	}

	@Test
	public void findOtherIps() {

		K8sMemberDiscovery discovery = new TdDiscovery("").setPodName("pod2");

		List<String> ips = discovery.getOtherIps();
		String memberIp = discovery.getMemberIp();

		assertEquals(2 , ips.size());
		assertEquals("1.2.3.4" , ips.get(0));
		assertEquals("1.2.3.6" , ips.get(1));
		assertEquals("1.2.3.5" , memberIp);
	}


	@Test
	public void findOtherIps_viaSystemProperty() {

		System.setProperty("POD_NAME","pod2");

		K8sMemberDiscovery discovery = new TdDiscovery("");

		List<String> ips = discovery.getOtherIps();
		String memberIp = discovery.getMemberIp();

		assertEquals(2 , ips.size());
		assertEquals("1.2.3.4" , ips.get(0));
		assertEquals("1.2.3.6" , ips.get(1));
		assertEquals("1.2.3.5" , memberIp);

		discovery.setPodName("Junk");
		String notFoundMemberIp = discovery.getMemberIp();
		assertNull(notFoundMemberIp);
	}

	@Test
	public void findAllIps() {

		List<String> ips = new TdDiscovery("")
				.setPodName("pod2")
				.getAllIps();

		assertEquals(3 , ips.size());
		assertEquals("1.2.3.4" , ips.get(0));
		assertEquals("1.2.3.5" , ips.get(1));
		assertEquals("1.2.3.6" , ips.get(2));
	}

	@Test
	public void findOtherMembers() {

		K8sMemberDiscovery discovery = new TdDiscovery("").setPodName("pod2");

		List<K8sServiceMember> members = discovery.getOtherMembers();
		K8sServiceMember member = discovery.getMember();

		assertEquals(2 , members.size());
		assertEquals("ip:1.2.3.4 pod:pod1 node:foo" , members.get(0).toString());
		assertEquals("ip:1.2.3.6 pod:pod3 node:baz" , members.get(1).toString());

		assertEquals("ip:1.2.3.5 pod:pod2 node:bar" ,member.toString());

		discovery.setPodName("junk");
		K8sServiceMember notFound = discovery.getMember();
		assertNull(notFound);
	}


	@Test
	public void findAllMembers() {

		List<K8sServiceMember> members = new TdDiscovery("")
				.setPodName("pod2")
				.getAllMembers();

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
		protected K8sServiceMembers loadAllMembers() {
			return members();
		}
	}
}