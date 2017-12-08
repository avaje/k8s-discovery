package org.avaje.k8s.discovery;


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
 * Find the members of service in a K8s cluster and namespace.
 *
 * <pre>{@code
 *
 *   // use defaults which reads namespace from
 *   // POD_NAMESPACE environment variable
 *
 *   List<K8sServiceMember> members =
 *       new K8sMemberDiscovery("my-service")
 *       .findAllMembers();
 *
 * }</pre>
 *
 * <pre>{@code
 *
 *   // explicit namespace
 *   // filter out the current pod
 *
 *   List<K8sServiceMember> members =
 *       new K8sMemberDiscovery("my-service", "dev")
 *       .setPodName(currentPod)
 *       .findMembers();
 *
 * }</pre>
 */
public class K8sMemberDiscovery {

	private String master = "https://kubernetes.default.svc.cluster.local:443";

	private String accountToken = "/var/run/secrets/kubernetes.io/serviceaccount/token";

	private String serviceName;

	private String namespace;

	private String podName;

	/**
	 * Create with a given service name.
	 */
	public K8sMemberDiscovery(String serviceName) {
		this.serviceName = serviceName;
		this.namespace = System.getenv("POD_NAMESPACE");
		this.podName = System.getenv("POD_NAME");
	}

	/**
	 * Create with a given service name and namespace.
	 */
	public K8sMemberDiscovery(String serviceName, String namespace) {
		this.serviceName = serviceName;
		this.namespace = namespace;
	}

	/**
	 * Set the master URL to use.
	 */
	public K8sMemberDiscovery setMaster(String master) {
		this.master = master;
		return this;
	}

	/**
	 * Set the service name to find members for.
	 */
	public K8sMemberDiscovery setServiceName(String serviceName) {
		this.serviceName = serviceName;
		return this;

	}

	/**
	 * Set the namespace to search. Default to read env POD_NAMESPACE.
	 */
	public K8sMemberDiscovery setNamespace(String namespace) {
		this.namespace = namespace;
		return this;
	}

	/**
	 * Set the name of the current pod (to filter out). Default to read env POD_NAME.
	 */
	public K8sMemberDiscovery setPodName(String podName) {
		this.podName = podName;
		return this;
	}

	/**
	 * Find and return the Ip address of the other service members.
	 */
	public List<String> findOtherIps() {
		return mapIps(findOtherMembers());
	}

	/**
	 * Find and return the Ip address of all service members.
	 */
	public List<String> findAllIps() {
		return mapIps(findAllMembers());
	}

	/**
	 * Extract out the Ip Address only from the members.
	 */
	public List<String> mapIps(List<K8sServiceMember> members) {
		return members.stream()
				.map(K8sServiceMember::getIpAddress)
				.collect(Collectors.toList());
	}

	/**
	 * Return the members filtering out the current pod if it is set.
	 */
	public List<K8sServiceMember> findOtherMembers() {

		List<K8sServiceMember> members = fetchAllMembers();
		if (podName == null) {
			return members;
		}
		// filter out the current pod
		return members
				.stream().filter(it -> !podName.equals(it.getPodName()) )
				.collect(Collectors.toList());
	}

	/**
	 * Return all the members of this service including the current pod.
	 */
	public List<K8sServiceMember> findAllMembers() {
		return fetchAllMembers();
	}

	protected List<K8sServiceMember> fetchAllMembers() {

		String path = "/api/v1/namespaces/" + namespace + "/endpoints/" + serviceName;

		try {

			URL url = new URL(master + path);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setHostnameVerifier(trustAllHosts);

			SSLContext ctx = SSLContext.getInstance("SSL");
			ctx.init(null, trustAll, new SecureRandom());
			conn.setSSLSocketFactory(ctx.getSocketFactory());
			conn.addRequestProperty("Authorization", "Bearer " + serviceAccountToken(accountToken));


			String jsonContent = readContent(conn);
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
	 * Sets Kubernetes master API url.
	 */
	public void setMasterUrl(String master) {
		this.master = master;
	}

	/**
	 * Set the account token.
	 *
	 * By default account token is read from '/var/run/secrets/kubernetes.io/serviceaccount/token'.
	 */
	public void setAccountToken(String accountToken) {
		this.accountToken = accountToken;
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
