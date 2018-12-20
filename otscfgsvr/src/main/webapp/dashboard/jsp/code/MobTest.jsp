<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html >
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=gbk" />
	<title>mob�ֶεĶ�дdemo����--xInsight big-data</title>
	<link rel="shortcut icon" href="../images/icon.ico"/>	
	<script type="text/javascript" src="scripts/shCore.js"></script>
    <!-- ���� pre �� brush �� java�������Ҫ���� shBrushJava �� js �ļ� -->
    <!-- scripts Ŀ¼�»��кܶ�� shBrushXxxx.js �ļ� -->
    <script type="text/javascript" src="scripts/shBrushJava.js"></script>
    <link type="text/css" rel="stylesheet" href="styles/shCore.css"/>
    <!-- ����ʹ�� eclipse ��ۣ�styles Ŀ¼�»��кܶ����� -->
	<link type="text/css" rel="stylesheet" href="styles/shThemeEclipse.css"/>
	<script type="text/javascript">
		SyntaxHighlighter.config.clipboardSwf = 'scripts/clipboard.swf';
		SyntaxHighlighter.all();
	</script>
</head>

<style type="text/css">
/** ��һ���ֺŴ�С�������ʽ����Ĵ����ܴ� */
.syntaxhighlighter div { font-size:12px !important; }
</style>

<body>
<p>mob�ֶεĶ�дdemo����</p>
<div style="width:80%;border:1px solid #EDEDED;padding:10px;background:#F7F7F7;color:#4F4F4F;">
<pre class="brush: java;">
package com.baosight.xinsight.ots.rest.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MobTest {
	public static void main(String[] args) throws Exception {
		MobTest to = new MobTest();

		to.putRecord("168.2.4.54:8080", "oss_13_2", "range",
		        "2015-09-06_104102", "image", "D:\\2015-09-06_104102.jpg");
		to.getRecord("168.2.4.54:8080", "oss_13_2", "range",
				"2015-09-06_104102", "image", "D:\\2015-09-06_104102_g.jpg");
	}

	public String getRecord(String addr, String tableName, String hashkey,
			String rangekey, String columnName, String outputFile) {
		try {
			URL urlObj = new URL("http://" + addr + "/otsrest/api/record/file/"
					+ tableName + "?hash_key=" + hashkey + "&range_key="
					+ rangekey + "&column=" + columnName);
			HttpURLConnection http_conn = (HttpURLConnection) urlObj
					.openConnection();

			//ע������Ҫ�����û��������base64����YmFva2FuZzpieXM=����baokang���ֳ����Ը�������޸�
			http_conn.setRequestProperty("Authorization",
					"Basic YWRtaW5Ab3NzOmFkbWlu");
			http_conn.setDoOutput(true);
			http_conn.setUseCaches(false);

			http_conn.setConnectTimeout(20 * 1000);
			http_conn.setReadTimeout(20 * 1000);

			http_conn.setRequestMethod("GET");
			http_conn.setRequestProperty("Content-Type",
					"application/octet-stream");
			http_conn.connect();

			InputStream in = http_conn.getInputStream();
			FileOutputStream fos = new FileOutputStream(outputFile);
			byte[] b = new byte[1026];
			int count = 0;
			while ((count = in.read(b)) != -1) {
				fos.write(b, 0, count);

				// �����Ҫ��otsҳ��鿴�����ݣ�����תΪ16���Ʊ����д
				// String strContent = new String(b);
				// byte[] bytes = hexStringToBytes(strContent);
				// fos.write(bytes);
			}
			in.close();
			fos.close();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public String putRecord(String addr, String tableName, String hashkey,
			String rangekey, String columnName, String inputFile) {
		try {
			URL urlObj = new URL("http://" + addr + "/otsrest/api/record/file/"
					+ tableName + "?hash_key=" + hashkey + "&range_key="
					+ rangekey + "&column=" + columnName);
			HttpURLConnection http_conn = (HttpURLConnection) urlObj
					.openConnection();
			http_conn.setDoOutput(true);
			http_conn.setDoInput(true);
			http_conn.setUseCaches(false);

			http_conn.setRequestMethod("POST");
			//�������Ϊoctet-stream
			http_conn.setRequestProperty("Content-Type",
					"application/octet-stream");
			//ע������Ҫ�����û��������base64����YmFva2FuZzpieXM=����baokang���ֳ����Ը�������޸�
			http_conn.setRequestProperty("Authorization",
					"Basic YWRtaW5Ab3NzOmFkbWlu");

			OutputStream dos = http_conn.getOutputStream();
			FileInputStream fis = new FileInputStream(inputFile);
			int count = 0;
			byte[] buffer = new byte[1026];
			while ((count = fis.read(buffer)) != -1) {
				dos.write(buffer, 0, count);

				// �����Ҫ��otsҳ��鿴�����ݣ�����תΪ16���Ʊ����д
				// StringBuffer bufferContent = bytesToString(buffer);
				// dos.write(bufferContent.toString().getBytes());
			}
			fis.close();
			dos.flush();
			dos.close();

			// �����Ӧ״̬
			int responseCode = http_conn.getResponseCode();
			if (HttpURLConnection.HTTP_CREATED == responseCode) {// ���ӳɹ�
				BufferedReader in = new BufferedReader(new InputStreamReader(
						(InputStream) http_conn.getInputStream(), "UTF-8"));
				String returnData = in.readLine();
				System.out.println(returnData);
				in.close();
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ���ַ�����ʽ��ʾ��ʮ��������ת��Ϊbyte����
	 */
	public static byte[] hexStringToBytes(String hexString) {
		hexString = hexString.toLowerCase();
		String[] hexStrings = hexString.split(" ");
		byte[] bytes = new byte[hexStrings.length];
		for (int i = 0; i < hexStrings.length; i++) {
			char[] hexChars = hexStrings[i].toCharArray();
			bytes[i] = (byte) (charToByte(hexChars[0]) << 4 
					| charToByte(hexChars[1]));
		}
		return bytes;
	}

	/**
	 * ��byte����ת��Ϊ�ַ�����ʽ��ʾ��ʮ������������鿴
	 */
	public static StringBuffer bytesToString(byte[] bytes) {
		StringBuffer sBuffer = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String s = Integer.toHexString(bytes[i] & 0xff);
			if (s.length() < 2)
				sBuffer.append('0');
			sBuffer.append(s + " ");
		}
		return sBuffer;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789abcdef".indexOf(c);
		// �� return (byte) "0123456789ABCDEF".indexOf(c);
	}
}

</pre>
</div>
</body>
</html>