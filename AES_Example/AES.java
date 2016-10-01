import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import javax.crypto.spec.*;
import java.util.Base64;

public class AES {

	private static String _password;
	private static String _salt;
	private static byte[] _iv;
	private static int _passwordIterations = 10;
	private static int _keySize = 256;
	
	public static void main(String[] args) {
	
		_password = "AESPassword20160925BrianSpencer!";
		_salt = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		
		String text = "This is my big long text.  how long will it take to decrypt me.";
		
		System.out.print("\nData to encrypt:");
		System.out.println(text);
		
		try {
		
			String encryptedText = Encrypt(text);
			System.out.print("Encrypted data:");
			System.out.println(encryptedText);
			
			String decryptedText = Decrypt(encryptedText);
			System.out.print("Decrypted data:");
			System.out.println(decryptedText);	
			
		} catch (Exception e) {
		  e.printStackTrace();
		}
	
	}
	
	private static SecretKey GetKey() throws Exception {
	
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		KeySpec spec = new PBEKeySpec(_password.toCharArray(), _salt.getBytes(), _passwordIterations, _keySize);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey theKey = new SecretKeySpec(tmp.getEncoded(), "AES");
		return theKey;
	}
	
	private static String Encrypt(String text) throws Exception {
		
		Key key = GetKey();
		
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, key);
		
		AlgorithmParameters params = c.getParameters();
		_iv = params.getParameterSpec(IvParameterSpec.class).getIV();
		
        byte[] encValue = c.doFinal(text.getBytes());
		
        String encryptedValue = Base64.getEncoder().encodeToString(encValue);
        return encryptedValue;

	}
	
	private static String Decrypt(String text) throws Exception {
	
		Key key = GetKey();
		
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(_iv));
		
        byte[] decordedValue = Base64.getDecoder().decode(text);
        byte[] decValue = c.doFinal(decordedValue);
		
        String decryptedValue = new String(decValue);
        return decryptedValue;
	}

}