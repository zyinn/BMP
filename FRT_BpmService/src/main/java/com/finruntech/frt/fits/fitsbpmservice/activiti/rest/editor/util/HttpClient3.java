package com.finruntech.frt.fits.fitsbpmservice.activiti.rest.editor.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * @ClassName HttpClient3 
 * @Description  璇锋眰鎺ュ彛鍏叡绫�
 * @author erle
 * @date 2014-4-17  
 * @version  1.0
 */
public class HttpClient3 {
	public static final Log as4j = LogFactory.getLog(HttpClient3.class);
    
    private static final String DEFAULT_CHARSET ="UTF-8"; 
    
    private static final int DEFAULT_CONNECTION_TIMEOUT = 120 * 1000;
    
    private static final int DEFAULT_SOCKET_READ_TIMEOUT = 120 * 1000;
    
    private String charset;
    
    private int timeout;
    
    public HttpClient3() {
        this(DEFAULT_CHARSET, DEFAULT_SOCKET_READ_TIMEOUT);
    }
    
    public HttpClient3(int timeout) {
        this(DEFAULT_CHARSET, timeout);
    }
    
    public HttpClient3(String charset, int timeout) {
        this.charset = StringUtils.isBlank(charset) ? DEFAULT_CHARSET : charset;
        this.timeout = timeout < 0 ? DEFAULT_SOCKET_READ_TIMEOUT : timeout;
    }
    
    public String doPost(String url, Map<String, String> params) throws Exception {
        return doPost(url, concatParameters(params));
    }
    
    /**
     * 鍙戦�丳OST璇锋眰
     * @param url 璇锋眰
     * @param params url缂栫爜鐨勮姹傚弬鏁�
     * @return
     * @throws Exception
     */
    public String doPost(String url, String params) throws Exception {
        as4j.info(" Http Post Url : " + url + "   param : "+ params);
        PostMethod post = null;
        try {
        	System.out.println("params:"+params);
            HttpClient httpClient = new HttpClient();
            post = new PostMethod(url);
            RequestEntity requestEntity = new StringRequestEntity(params, "application/x-www-form-urlencoded",this.charset);
            post.setRequestEntity(requestEntity);
            post.setRequestHeader("Connection", "close");  
            httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);
            httpClient.getHttpConnectionManager().getParams().setSoTimeout(this.timeout);
            post.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
            httpClient.executeMethod(post);
            String resp = getResonseString(post);
            return resp;
        } catch (Exception e) {
            throw e;
        } finally {
            post.releaseConnection();
        }
    }
    
    /**
     * 鍙戦�丳OST璇锋眰
     * @param url 璇锋眰
     * @param params url缂栫爜鐨勮姹傚弬鏁�
     * @return
     * @throws Exception
     */
    public String doGet(String url, String params) throws Exception {
        as4j.info(" Http Post Url : " + url + "   param : "+ params);
        GetMethod get=null;
        try {
            HttpClient httpClient = new HttpClient();
            get = new GetMethod(url);
            get.setRequestHeader("Connection", "close");  
            httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(DEFAULT_CONNECTION_TIMEOUT);
            httpClient.getHttpConnectionManager().getParams().setSoTimeout(this.timeout);
            get.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
            httpClient.executeMethod(get);
            String resp = getResonseString(get);
            return resp;
        } catch (Exception e) {
            throw e;
        } finally {
        	get.releaseConnection();
        }
    }
    
    private String getResonseString(PostMethod post) throws Exception{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = post.getResponseBodyAsStream();
        byte[] bys = new byte[1024];
        for(int n = -1; (n = in.read(bys)) != -1; ) {
            out.write(bys, 0, n);
        }
        return new String(out.toByteArray(), this.charset);
    }
    
    private String getResonseString(GetMethod get) throws Exception{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = get.getResponseBodyAsStream();
        byte[] bys = new byte[1024];
        for(int n = -1; (n = in.read(bys)) != -1; ) {
            out.write(bys, 0, n);
        }
        return new String(out.toByteArray(), this.charset);
    }
    
    public String concatParameters(Map<String, String> params) throws Exception  {
        StringBuilder sb = new StringBuilder();
        int p = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (p++ > 0) {
                sb.append('&');
            }
            sb.append(entry.getKey()).append('=').append(entry.getValue());
            //sb.append(entry.getKey()).append('=').append(encodeURI(entry.getValue()));
        }
        return sb.toString();
    }
    
    public String encodeURI(String param) {
        if(param == null) {
            return "";
        }
        try {
            return URLEncoder.encode(param, this.charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}