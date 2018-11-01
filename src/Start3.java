import com.hongdeyan.utils.RsaUtil;

public class Start3 {
    public static void main(String[] args) {
        String[] keyPairs = RsaUtil.createKeyPairs();
         System.out.println("公钥Base64编码:" + keyPairs[0]);
         System.out.println("私钥Base64编码:" + keyPairs[1]);
    }
}
