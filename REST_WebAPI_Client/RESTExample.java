import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Base64;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

public class RESTExample {

	private static String _rsaKey;
	private static String _rsaModulus;
	private static String _rsaExponent;

    public static void main(String[] args) {
        
		try {
		
			GetRSAKey();
			
			if(_rsaModulus.length() > 0 && _rsaExponent.length() > 0)
				JSONService();
				
		}
		catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	private static void GetRSAKey() {
		System.out.print("Getting RSA Key...");
		try {
			URL url = new URL("http://localhost:48882/RESTService.svc/32179AAE-701F-43AC-8E76-68B1553584D4/rsakey");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/xml;charset=UTF-8");
			conn.setRequestProperty("Accept", "application/xml");
			
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			while ((output = br.readLine()) != null) {
				_rsaKey = output;
			}

			conn.disconnect();		
			
			//_rsaKey = "<RSAKeyValue><Exponent>AQAB</Exponent><Modulus>3nAtE8P3qmYFQSpAMth8dTewX8g5b17RiLifYqEMKeh51SthXeIA2uGhGnwFldk7ukvoUzyNfPy8TyiJwRfjgQ==</Modulus></RSAKeyValue>";
			System.out.println(_rsaKey);
			ParseRSAKey(_rsaKey);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void ParseRSAKey(String rsaKeyXML) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			
			InputSource is = new InputSource(new StringReader(rsaKeyXML));
			
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("RSAKeyValue");
			Element eElement = (Element) nList.item(0);
			
			_rsaExponent = eElement.getElementsByTagName("Exponent").item(0).getTextContent();
			_rsaModulus = eElement.getElementsByTagName("Modulus").item(0).getTextContent();
			
		} 
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (SAXException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	private static void JSONService() {
		System.out.print("Calling JSON service...");
		try {
			URL url = new URL("http://localhost:48882/RESTService.svc/32179AAE-701F-43AC-8E76-68B1553584D4/json");
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888));
			HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
			conn.setDoOutput(true);
			conn.setRequestMethod("PUT");
			conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");			
			
			//Encrypt data
			AES aes = new AES();
			aes._password = "AESPassword20160925BrianSpencer!";
			aes._salt = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			aes._passwordIterations = 1;
			aes.IV = "1234567812345678";
			
			RSA rsa = new RSA();
			rsa.rsaModulus = _rsaModulus;
			rsa.rsaExponent = _rsaExponent;
			
			Request req = new Request();
			req.Action = "Referral";
			req.Data = aes.Encrypt("My test data from Java");
			req.IV = rsa.Encrypt(aes.IV);
			req.Key = rsa.Encrypt(Base64.getEncoder().encodeToString(aes.GetKey()));
					
			String input = req.GetJSON();
			
			System.out.println(input);
			
			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

			String json = "";
			String output;
			System.out.print("Server response .... ");
			while ((output = br.readLine()) != null) {
				json += output.replace("\\/","/");;
				System.out.println(output);
			}
			conn.disconnect();
			
			//Decrypt data
			String encryptedData = json.substring(9, json.indexOf("\"",9));
			System.out.println(encryptedData);
			
			String decryptedData = aes.Decrypt(encryptedData);
			System.out.println(decryptedData);
			
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static class Request {
		public String Action;
		public String Data;
		public String IV;
		public String Key;
		
		public String GetJSON() {
			return "{\"Action\":\"" + Action + "\",\"Data\":\"" + Data + "\",\"IV\":\"" + IV + "\",\"Key\":\"" + Key + "\"}";
		}
	}
}
