package org.avaje.k8s.discovery;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Find the members of service in a Kubernetes cluster and namespace.
 * <p>
 * <pre>{@code
 *
 *   // use defaults which reads namespace from
 *   // POD_NAMESPACE environment variable
 *
 *   List<K8sServiceMember> members =
 *       new K8sMemberDiscovery("my-service")
 *       .getAllMembers();
 *
 * }</pre>
 * <p>
 * <pre>{@code
 *
 *   // explicit namespace
 *   // filter out the current pod
 *
 *   List<K8sServiceMember> members =
 *       new K8sMemberDiscovery("my-service", "dev")
 *       .setPodName(currentPod)
 *       .getOtherMembers();
 *
 * }</pre>
 */
public class K8sMemberDiscovery {

	private static final Logger log = LoggerFactory.getLogger(K8sMemberDiscovery.class);

	protected String masterUrl = "https://kubernetes.default.svc.cluster.local:443";

	protected String accountToken = "/var/run/secrets/kubernetes.io/serviceaccount/token";

	protected String serviceName;

	protected String namespace;

	protected String podName;

	protected K8sServiceMembers members;

	/**
	 * Create with a given service name.
	 */
	public K8sMemberDiscovery(String serviceName) {
		this.serviceName = serviceName;
		this.namespace = read("POD_NAMESPACE");
		this.podName = read("POD_NAME");
	}

	/**
	 * Create with a given service name and namespace.
	 */
	public K8sMemberDiscovery(String serviceName, String namespace) {
		this.serviceName = serviceName;
		this.namespace = namespace;
		this.podName = read("POD_NAME");
	}

	private String read(String key) {
		return System.getProperty(key, System.getenv(key));
	}

	/**
	 * Set the service name to find members for.
	 */
	public K8sMemberDiscovery setServiceName(String serviceName) {
		if (serviceName != null) {
			this.serviceName = serviceName;
		}
		return this;
	}

	/**
	 * Set the namespace to search. Default to read env POD_NAMESPACE.
	 */
	public K8sMemberDiscovery setNamespace(String namespace) {
		if (namespace != null) {
			this.namespace = namespace;
		}
		return this;
	}

	/**
	 * Set the name of the current pod (to filter out). Default to read env POD_NAME.
	 */
	public K8sMemberDiscovery setPodName(String podName) {
		if (podName != null) {
			this.podName = podName;
		}
		return this;
	}

	/**
	 * Sets Kubernetes master API url.
	 */
	public K8sMemberDiscovery setMasterUrl(String masterUrl) {
		if (masterUrl != null) {
			this.masterUrl = masterUrl;
		}
		return this;
	}

	/**
	 * Set the account token.
	 * <p>
	 * By default account token is read from '/var/run/secrets/kubernetes.io/serviceaccount/token'.
	 */
	public K8sMemberDiscovery setAccountToken(String accountToken) {
		if (accountToken != null) {
			this.accountToken = accountToken;
		}
		return this;
	}

	/**
	 * Return the masterUrl used.
	 */
	public String getMasterUrl() {
		return masterUrl;
	}

	/**
	 * Return the service name used.
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * Return the namespace used.
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Return the podName used (Often set as environment variable POD_NAME).
	 */
	public String getPodName() {
		return podName;
	}

	/**
	 * Extract out the Ip Address from the members.
	 */
	public List<String> mapIps(List<K8sServiceMember> members) {
		return members.stream()
				.map(K8sServiceMember::getIpAddress)
				.collect(Collectors.toList());
	}

	/**
	 * Return the Ip address of the other service members.
	 */
	public List<String> getOtherIps() {
		return mapIps(getOtherMembers());
	}

	/**
	 * Return the Ip address of all service members.
	 */
	public List<String> getAllIps() {
		return mapIps(getAllMembers());
	}

	/**
	 * Return the Ip address of this pod (matched by pod name).
	 */
	public String getMemberIp() {
		K8sServiceMember member = getMember();
		return member != null ? member.getIpAddress() : null;
	}

	/**
	 * Return this pod (matched by pod name) or null searching both the members and 'notReady' members.
	 */
	public K8sServiceMember getMember() {
		loadIfRequired();
		if (podName == null) {
			return null;

		} else {
			return members.findPod(podName);
		}
	}

	/**
	 * Return the members filtering out the current pod if it is set.
	 */
	public List<K8sServiceMember> getOtherMembers() {

		loadIfRequired();
		if (podName == null) {
			return members.getMembers();
		}
		// filter out the current pod
		return members.getMembers()
				.stream().filter(it -> !podName.equals(it.getPodName()))
				.collect(Collectors.toList());
	}

	/**
	 * Return all the members of this service including the current pod.
	 */
	public List<K8sServiceMember> getAllMembers() {
		loadIfRequired();
		return members.getMembers();
	}

	/**
	 * Return all the not ready members of this service.
	 * This can include the current pod.
	 */
	public List<K8sServiceMember> getNotReadyMembers() {
		loadIfRequired();
		return members.getNotReady();
	}

	/**
	 * Return the members.
	 */
	public K8sServiceMembers getMembers() {
		loadIfRequired();
		return members;
	}

	/**
	 * Force a reload of the members from the kubernetes master.
	 */
	public void reload() {
		members = loadAllMembers();
	}

	protected void loadIfRequired() {
		if (members == null || members.isEmpty()) {
			members = loadAllMembers();
			log.debug("loaded all members for service:{} namespace:{} members:{}", serviceName, namespace, members);
		}
	}

	protected K8sServiceMembers loadAllMembers() {

		String path = "/api/v1/namespaces/" + namespace + "/endpoints/" + serviceName;

		try {
			if (log.isTraceEnabled()) {
				log.trace("loading member content from:{}", masterUrl + path);
			}
			URL url = new URL(masterUrl + path);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setHostnameVerifier(trustAllHosts);

			SSLContext ctx = SSLContext.getInstance("SSL");
			ctx.init(null, trustAll, new SecureRandom());
			conn.setSSLSocketFactory(ctx.getSocketFactory());
			conn.addRequestProperty("Authorization", "Bearer " + serviceAccountToken(accountToken));

			String jsonContent = readContent(conn);
			if (log.isTraceEnabled()) {
				log.trace("K8s endpoints json: " + jsonContent);
			}
			return new MemberParser(jsonContent).parseJson();

		} catch (Exception e) {
			throw new IllegalStateException("Error getting members", e);
		}
	}

	/**
	 * Read the content as a String.
	 */
	private String readContent(HttpsURLConnection conn) throws IOException {

		InputStream inputStream = conn.getInputStream();
		InputStreamReader reader = new InputStreamReader(inputStream);

		LineNumberReader lineNumberReader = new LineNumberReader(reader);
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = lineNumberReader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	/**
	 * Reads content of the service account token file.
	 *
	 * @param file The path to the service account token.
	 * @return Service account token.
	 */
	private String serviceAccountToken(String file) {
		try {
			return new String(Files.readAllBytes(Paths.get(file)));
		} catch (IOException e) {
			throw new RuntimeException("Failed to load services account token [setAccountToken= " + file + "]", e);
		}
	}

	private TrustManager[] trustAll = new TrustManager[]{
			new X509TrustManager() {
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			}
	};

	private HostnameVerifier trustAllHosts = (hostname, session) -> true;
}
