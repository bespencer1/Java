import java.security.KeyFactory;
import java.security.PublicKey;
import java.math.BigInteger;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

public class RSA {

	
	public String rsaModulus;
	public String rsaExponent;

	public RSA() {
	}
	
	public String Encrypt(String text) {

		String encryptedString = null;
		try {
	
			byte[] modBytes = Base64.getDecoder().decode(rsaModulus);
			byte[] expBytes = Base64.getDecoder().decode(rsaExponent);
			
			BigInteger modulus = new BigInteger(1,modBytes);
			BigInteger pubExp = new BigInteger(1,expBytes);
			
			RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(modulus, pubExp);			
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey key = keyFactory.generatePublic(pubKeySpec);
		
			//If .Net RSACryptoServiceProvider.Decrypt OEAP set to true use "RSA/ECB/OAEPWithSHA1AndMGF1Padding".
			//If OEAP set to false use "RSA/ECB/OAEPWithSHA1AndMGF1Padding"
			Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			
			byte[] encrypted = cipher.doFinal(text.getBytes("UTF-8"));
			encryptedString = Base64.getEncoder().encodeToString(encrypted);
			
		} catch (Exception e) {
		  e.printStackTrace();
		}
		
		return encryptedString;
	}

}