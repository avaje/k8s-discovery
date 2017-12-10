package org.avaje.k8s.discovery;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MemberParserTest {


	@Test
	public void parse() {

		String basic = "{\"kind\":\"Endpoints\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"foo-service\",\"namespace\":\"dev\",\"selfLink\":\"/api/v1/namespaces/dev/endpoints/foo-service\",\"uid\":\"f2fa8d37-c2d9-11e7-b9c2-02424c321446\",\"resourceVersion\":\"101316205\",\"creationTimestamp\":\"2017-11-06T10:04:59Z\",\"labels\":{\"kubernetes.io/cluster-service\":\"true\",\"run\":\"foo-service\",\"toggles\":\"true\"}},\"subsets\":[{\"addresses\":[{\"ip\":\"10.254.18.216\",\"nodeName\":\"ip-172-19-72-214.ap-southeast-2.compute.internal\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"dev\",\"name\":\"foo-service-6788ccd559-fv7tx\",\"uid\":\"5741bf67-dba7-11e7-a4ff-02b5ed2f9dca\",\"resourceVersion\":\"101315368\"}},{\"ip\":\"10.254.5.121\",\"nodeName\":\"ip-172-19-73-199.ap-southeast-2.compute.internal\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"dev\",\"name\":\"foo-service-6788ccd559-khh5m\",\"uid\":\"9e5d8460-dba7-11e7-a4ff-02b5ed2f9dca\",\"resourceVersion\":\"101316008\"}}],\"ports\":[{\"port\":8370,\"protocol\":\"TCP\"}]}]}";

		List<K8sServiceMember> members = new MemberParser(basic).parseJson().getMembers();

		assertEquals(members.size(), 2);
		assertEquals(members.get(0).toString(), "ip:10.254.18.216 pod:foo-service-6788ccd559-fv7tx node:ip-172-19-72-214.ap-southeast-2.compute.internal");
		assertEquals(members.get(1).toString(), "ip:10.254.5.121 pod:foo-service-6788ccd559-khh5m node:ip-172-19-73-199.ap-southeast-2.compute.internal");
	}

	@Test
	public void parse_one() {

		String basic = "{\"kind\":\"Endpoints\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"foo-service\",\"namespace\":\"dev\",\"selfLink\":\"/api/v1/namespaces/dev/endpoints/foo-service\",\"uid\":\"f2fa8d37-c2d9-11e7-b9c2-02424c321446\",\"resourceVersion\":\"101316205\",\"creationTimestamp\":\"2017-11-06T10:04:59Z\",\"labels\":{\"kubernetes.io/cluster-service\":\"true\",\"run\":\"foo-service\",\"toggles\":\"true\"}},\"subsets\":[{\"addresses\":[{\"ip\":\"10.254.5.121\",\"nodeName\":\"ip-172-19-73-199.ap-southeast-2.compute.internal\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"dev\",\"name\":\"foo-service-6788ccd559-khh5m\",\"uid\":\"9e5d8460-dba7-11e7-a4ff-02b5ed2f9dca\",\"resourceVersion\":\"101316008\"}}],\"ports\":[{\"port\":8370,\"protocol\":\"TCP\"}]}]}";

		List<K8sServiceMember> members = new MemberParser(basic).parseJson().getMembers();

		assertEquals(members.size(), 1);
		assertEquals(members.get(0).toString(), "ip:10.254.5.121 pod:foo-service-6788ccd559-khh5m node:ip-172-19-73-199.ap-southeast-2.compute.internal");
	}


	@Test
	public void empty() {

		String empty = "{\"kind\":\"Endpoints\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"foo-service\",\"namespace\":\"dev\",\"selfLink\":\"/api/v1/namespaces/dev/endpoints/foo-service\",\"uid\":\"f2fa8d37-c2d9-11e7-b9c2-02424c321446\",\"resourceVersion\":\"101316205\",\"creationTimestamp\":\"2017-11-06T10:04:59Z\",\"labels\":{\"kubernetes.io/cluster-service\":\"true\",\"run\":\"foo-service\",\"toggles\":\"true\"}},\"subsets\":[{\"addresses\":[],\"ports\":[{\"port\":8370,\"protocol\":\"TCP\"}]}]}";

		List<K8sServiceMember> members = new MemberParser(empty).parseJson().getMembers();
		assertEquals(members.size(), 0);
	}

	@Test
	public void nothing() {

		String empty = "";

		K8sServiceMembers members = new MemberParser(empty).parseJson();
		assertTrue(members.isEmpty());
	}

	@Test
	public void asd() {

		String content = "{\"kind\":\"Endpoints\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"foo-service\",\"namespace\":\"dev\",\"selfLink\":\"/api/v1/namespaces/dev/endpoints/foo-service\",\"uid\":\"f2fa8d37-c2d9-11e7-b9c2-02424c321446\",\"resourceVersion\":\"102270133\",\"creationTimestamp\":\"2017-11-06T10:04:59Z\",\"labels\":{\"kubernetes.io/cluster-service\":\"true\",\"run\":\"foo-service\",\"toggles\":\"true\"}},\"subsets\":[{\"notReadyAddresses\":[{\"ip\":\"10.254.105.109\",\"nodeName\":\"ip-172-19-72-216.ap-southeast-2.compute.internal\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"dev\",\"name\":\"foo-service-88c4844c-kwpzg\",\"uid\":\"a8d2da1d-dd4a-11e7-a4ff-02b5ed2f9dca\",\"resourceVersion\":\"102268785\"}},{\"ip\":\"10.254.107.61\",\"nodeName\":\"ip-172-19-72-208.ap-southeast-2.compute.internal\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"dev\",\"name\":\"foo-service-84bb5c876b-wtkzr\",\"uid\":\"91ebd71c-dd55-11e7-a4ff-02b5ed2f9dca\",\"resourceVersion\":\"102270124\"}},{\"ip\":\"10.254.12.125\",\"nodeName\":\"ip-172-19-73-214.ap-southeast-2.compute.internal\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"dev\",\"name\":\"foo-service-88c4844c-7pwv6\",\"uid\":\"a481e62c-dd4a-11e7-a4ff-02b5ed2f9dca\",\"resourceVersion\":\"102268707\"}},{\"ip\":\"10.254.42.249\",\"nodeName\":\"ip-172-19-73-215.ap-southeast-2.compute.internal\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"dev\",\"name\":\"foo-service-84bb5c876b-fgx4w\",\"uid\":\"91ed9486-dd55-11e7-a4ff-02b5ed2f9dca\",\"resourceVersion\":\"102270131\"}}],\"ports\":[{\"name\":\"app\",\"port\":8370,\"protocol\":\"TCP\"},{\"name\":\"hazelcast\",\"port\":9911,\"protocol\":\"TCP\"}]}]}";
		K8sServiceMembers members = new MemberParser(content).parseJson();
		assertEquals(members.getMembers().size(), 0);
		assertEquals(members.getNotReady().size(), 4);

		K8sServiceMember found = members.findPod("foo-service-84bb5c876b-wtkzr");
		assertNotNull(found);
	}

}