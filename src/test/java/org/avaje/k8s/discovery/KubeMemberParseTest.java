package org.avaje.k8s.discovery;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class KubeMemberParseTest {


	@Test
	public void parse() {

		String basic = "{\"kind\":\"Endpoints\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"foo-service\",\"namespace\":\"dev\",\"selfLink\":\"/api/v1/namespaces/dev/endpoints/foo-service\",\"uid\":\"f2fa8d37-c2d9-11e7-b9c2-02424c321446\",\"resourceVersion\":\"101316205\",\"creationTimestamp\":\"2017-11-06T10:04:59Z\",\"labels\":{\"kubernetes.io/cluster-service\":\"true\",\"run\":\"foo-service\",\"toggles\":\"true\"}},\"subsets\":[{\"addresses\":[{\"ip\":\"10.254.18.216\",\"nodeName\":\"ip-172-19-72-214.ap-southeast-2.compute.internal\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"dev\",\"name\":\"foo-service-6788ccd559-fv7tx\",\"uid\":\"5741bf67-dba7-11e7-a4ff-02b5ed2f9dca\",\"resourceVersion\":\"101315368\"}},{\"ip\":\"10.254.5.121\",\"nodeName\":\"ip-172-19-73-199.ap-southeast-2.compute.internal\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"dev\",\"name\":\"foo-service-6788ccd559-khh5m\",\"uid\":\"9e5d8460-dba7-11e7-a4ff-02b5ed2f9dca\",\"resourceVersion\":\"101316008\"}}],\"ports\":[{\"port\":8370,\"protocol\":\"TCP\"}]}]}";

		List<K8sServiceMember> members = new MemberParser(basic).parseJson();

		assertEquals(members.size(), 2);
		assertEquals(members.get(0).toString(), "ip:10.254.18.216 pod:foo-service-6788ccd559-fv7tx node:ip-172-19-72-214.ap-southeast-2.compute.internal");
		assertEquals(members.get(1).toString(), "ip:10.254.5.121 pod:foo-service-6788ccd559-khh5m node:ip-172-19-73-199.ap-southeast-2.compute.internal");
	}

	@Test
	public void parse_one() {

		String basic = "{\"kind\":\"Endpoints\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"foo-service\",\"namespace\":\"dev\",\"selfLink\":\"/api/v1/namespaces/dev/endpoints/foo-service\",\"uid\":\"f2fa8d37-c2d9-11e7-b9c2-02424c321446\",\"resourceVersion\":\"101316205\",\"creationTimestamp\":\"2017-11-06T10:04:59Z\",\"labels\":{\"kubernetes.io/cluster-service\":\"true\",\"run\":\"foo-service\",\"toggles\":\"true\"}},\"subsets\":[{\"addresses\":[{\"ip\":\"10.254.5.121\",\"nodeName\":\"ip-172-19-73-199.ap-southeast-2.compute.internal\",\"targetRef\":{\"kind\":\"Pod\",\"namespace\":\"dev\",\"name\":\"foo-service-6788ccd559-khh5m\",\"uid\":\"9e5d8460-dba7-11e7-a4ff-02b5ed2f9dca\",\"resourceVersion\":\"101316008\"}}],\"ports\":[{\"port\":8370,\"protocol\":\"TCP\"}]}]}";

		List<K8sServiceMember> members = new MemberParser(basic).parseJson();

		assertEquals(members.size(), 1);
		assertEquals(members.get(0).toString(), "ip:10.254.5.121 pod:foo-service-6788ccd559-khh5m node:ip-172-19-73-199.ap-southeast-2.compute.internal");
	}


	@Test
	public void empty() {

		String empty = "{\"kind\":\"Endpoints\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"foo-service\",\"namespace\":\"dev\",\"selfLink\":\"/api/v1/namespaces/dev/endpoints/foo-service\",\"uid\":\"f2fa8d37-c2d9-11e7-b9c2-02424c321446\",\"resourceVersion\":\"101316205\",\"creationTimestamp\":\"2017-11-06T10:04:59Z\",\"labels\":{\"kubernetes.io/cluster-service\":\"true\",\"run\":\"foo-service\",\"toggles\":\"true\"}},\"subsets\":[{\"addresses\":[],\"ports\":[{\"port\":8370,\"protocol\":\"TCP\"}]}]}";

		List<K8sServiceMember> members = new MemberParser(empty).parseJson();
		assertEquals(members.size(), 0);
	}

	@Test
	public void nothing() {

		String empty = "";

		List<K8sServiceMember> members = new MemberParser(empty).parseJson();
		assertEquals(members.size(), 0);
	}

}