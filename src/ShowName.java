

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;
//
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSON;

@WebServlet(name="ShowName",value="/")
public class ShowName extends HttpServlet {
       
	protected void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
		String number = servletRequest.getParameter("number");
		String password = servletRequest.getParameter("password");
		String headerAgent = "User-Agent";
		String headAgentArg = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.25 Safari/537.36 Core/1.70.3858.400 QQBrowser/10.7.4309.400";
		String login = "https://jwxt.scau.edu.cn/secService/login";
		String userInfoUrl = "https://jwxt.scau.edu.cn/secService/assert.json?resourceCode=resourceCode&apiCode=framework.sign.controller.SignController.asserts&t=1605428730270";
		Map<String, String> param = new HashMap<>();
		Map<String, String> last = new HashMap<>();
		param.put("kaptcha", "testa");
		param.put("userCode", number);
		param.put("password", password);
		param.put("userCodeType", "account");
		String jsonString = JSON.toJSONString(param);
		
		BasicCookieStore cookieStore = new BasicCookieStore();			//cookieStore用来放第一次POST请求的cookie消息	
		HttpClientContext context = HttpClientContext.create();
		context.setCookieStore(cookieStore);
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
																				
		HttpPost httpPost = new HttpPost(login);
		httpPost.setHeader("Accept", "application/json, text/plain, */*");	
		httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
		httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
		httpPost.setHeader("app", "PCWEB");
		httpPost.setHeader("Connection", "keep-alive");
		httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
		httpPost.setHeader("Host", "jwxt.scau.edu.cn");
		httpPost.setHeader("Origin", "https://jwxt.scau.edu.cn");
		httpPost.setHeader("Referer", "https://jwxt.scau.edu.cn/");
		httpPost.setHeader(headerAgent, headAgentArg);
		httpPost.setHeader("KAPTCHA-KEY-GENERATOR-REDIS", "securityKaptchaRedisServiceAdapter");
			
		try {
			// json方式
			StringEntity entity = new StringEntity(jsonString.toString(),"utf-8");//解决中文乱码问题
			entity.setChunked(true);
			entity.setContentEncoding("UTF-8");
			entity.setContentType("application/json;charset=UTF-8");
			httpPost.setEntity(entity);
			
			HttpResponse response = httpClient.execute(httpPost,context);
			
			if(response.getStatusLine().getStatusCode() == 200) {
				HttpEntity httpEntity = response.getEntity();
				Header []headers1 = response.getAllHeaders();
				String headContent, cookie = "", token = "";
				for(Header header:headers1) {
					headContent = header.toString();
					if(headContent.startsWith("Set-Cookie: token")) {
						token = headContent.substring(headContent.indexOf("token")+6, headContent.indexOf(";"));
					}
					if(headContent.startsWith("Set-Cookie: SESSION")) {
						cookie = headContent.substring(headContent.indexOf("SESSION")+8, headContent.indexOf(";"));
					}
				}														
				String content = EntityUtils.toString(httpEntity, "utf-8");
				Document document = Jsoup.parse(content);
				System.out.println(document);   	
				
				HttpGet httpGet = new HttpGet(userInfoUrl);
				httpGet.setHeader(headerAgent, headAgentArg);
				httpGet.setHeader("Accept", "application/json, text/plain, */*");	
				httpGet.setHeader("Accept-Encoding", "gzip, deflate, br");
				httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
				httpGet.setHeader("app", "PCWEB");
				httpGet.setHeader("Connection", "keep-alive");
				httpGet.setHeader("Content-Type", "application/json");
				httpGet.setHeader("Cookie", "SESSION="+cookie+"; casLogin=1; token="+token);
				httpGet.setHeader("TOKEN", token);
				httpGet.setHeader("Request Method", "GET");
				httpGet.setHeader("Referer", "https://jwxt.scau.edu.cn/Njw2017/index.html");
				httpGet.setHeader("Connection", "keep-alive");
				httpGet.setHeader("Host", "jwxt.scau.edu.cn");
				
				HttpResponse infoResponse = httpClient.execute(httpGet,context);
				HttpEntity user_entity = infoResponse.getEntity();
				String information = EntityUtils.toString(user_entity, "utf-8");
				Document infoDocument = Jsoup.parse(information);
				System.out.println(infoDocument);	
				String name = information.substring(information.indexOf("userAlias")+12, information.indexOf("userAlias")+15);
				last.put("number", number);
				last.put("password", password);
				last.put("name", name);
				String json = JSON.toJSONString(last);
				servletResponse.setContentType("text/html;charset=UTF-8");
				PrintWriter printWriter = servletResponse.getWriter();
				printWriter.write(json);
			}
			else {
				System.out.println("GG");
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
