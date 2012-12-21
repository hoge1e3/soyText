package jp.tonyu.soytext2.auth;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jp.tonyu.util.SFile;
import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
// https://developers.google.com/accounts/docs/OAuth2WebServer
public class GoogleOAuth  {
    public static String encode(String u) {
        try {
            return URLEncoder.encode(u, "utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
            return u;
        }
    }
    String redirect_uri;
    String token_uri;
    String auth_uri;
    String client_id;
    String client_secret;
    public GoogleOAuth(SFile conf) throws JSONException, FileNotFoundException, IOException {
    	Map confo=(Map)JSON.decode(conf.inputStream());
    	Map web=(Map)confo.get("web");
    	this.redirect_uri=""+((List)web.get("redirect_uris")).get(0);
    	token_uri=""+web.get("token_uri");
    	auth_uri=""+web.get("auth_uri");
    	client_id=""+web.get("client_id");
    	client_secret=""+web.get("client_secret");
    }

    public String getAccessToken(String code) throws ClientProtocolException, IOException {

        HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(token_uri);
        final List <NameValuePair> params = new Vector <NameValuePair>();
        params.add(new BasicNameValuePair("code", code   ));
        params.add(new BasicNameValuePair("client_id",  client_id   ));
        params.add(new BasicNameValuePair("client_secret",  client_secret   ));
        params.add(new BasicNameValuePair("redirect_uri",  redirect_uri   ));
        params.add(new BasicNameValuePair("grant_type",  "authorization_code"   ));
        httppost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
        HttpResponse response=httpclient.execute(httppost);

        HttpEntity resEntity = response.getEntity();
        String resStr=EntityUtils.toString(resEntity);
        EntityUtils.consume(resEntity);

        Map res=(Map)JSON.decode(resStr);

        //System.out.println(resStr);
        return res.get("access_token").toString();
    }
    public Map getUserInfo(String accessToken) throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("https://www.googleapis.com/oauth2/v1/userinfo?access_token="+encode(accessToken));
        HttpResponse response=httpclient.execute(httpget);

        HttpEntity resEntity = response.getEntity();
        String resStr=EntityUtils.toString(resEntity);
        EntityUtils.consume(resEntity);

        return (Map)JSON.decode(resStr);
    }
    public String authURIWithParams(){
    	return auth_uri+"?"+
                "scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile&"+
                "state=%2Fprofile&"+
                "redirect_uri="+encode(redirect_uri)+"&"+
                "response_type=code&"+
                "client_id="+encode(client_id)+"&"+
                "approval_prompt=force";
    }
}
