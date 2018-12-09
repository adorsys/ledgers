package de.adorsys.ledgers.mockbank.simple;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class HttpURLConnectionHelper {

	public static void setAuthHeader(String accessToken, HttpURLConnection con) {
		if (accessToken != null) {
			con.setRequestProperty("Authorization", "Bearer " + accessToken);
		}
	}
	
	public static HttpURLConnection getContent(URL url, String accessToken) throws IOException, ProtocolException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setDoInput(true);
		con.setRequestProperty("Accept", "application/json,text/plain");
		// Access Token
		setAuthHeader(accessToken, con);
		// give it 15 seconds to respond
		con.setReadTimeout(30 * 1000);
		con.connect();
		return con;
	}
	

	public static HttpURLConnection postContent(URL url, String accessToken, byte[] content, String contentType)
			throws IOException, ProtocolException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Type", contentType);
		con.setRequestProperty("Content-Length", String.valueOf(content.length));
		con.setRequestProperty("Accept", "application/json,text/plain");
		setAuthHeader(accessToken, con);
		
		DataOutputStream out = new DataOutputStream(con.getOutputStream());
		out.write(content);
		out.flush();
//		out.close();		
		return con;
	}	

	public static String readToString(HttpURLConnection con) throws IOException {
		BufferedReader reader = null;
		StringBuilder stringBuilder;

		// read the output from the server
		reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
		stringBuilder = new StringBuilder();

		String line = null;
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line + "\n");
		}
		return stringBuilder.toString();
	}	
}
