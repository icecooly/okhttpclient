package io.itit.itf.okhttp;

import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 
 * @author icecooly
 *
 */
public class PostRequest extends OkHttpRequest {
	//
	public static Log log = LogFactory.getLog(PostRequest.class);
	//
	public PostRequest(String url, Object tag, Map<String, String> params, 
			Map<String, String> headers,List<FileInfo> fileInfos, int id) {
		super(url, tag, params, headers, fileInfos, id);
	}

	@Override
	protected RequestBody buildRequestBody() {
		if (fileInfos == null || fileInfos.isEmpty()) {
			FormBody.Builder builder = new FormBody.Builder();
			addParams(builder);
			FormBody formBody = builder.build();
			return formBody;
		} else {
			MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
			addParams(builder);
			fileInfos.forEach(fileInfo -> {
				RequestBody fileBody = RequestBody.create(MediaType.parse(getMimeType(fileInfo.fileName)),
						fileInfo.fileContent);
				builder.addFormDataPart(fileInfo.partName, fileInfo.fileName, fileBody);
			});
			return builder.build();
		}
	}

	@Override
	protected Request buildRequest(RequestBody requestBody) {
		return builder.post(requestBody).build();
	}

	private void addParams(FormBody.Builder builder) {
		if (params!= null) {
			params.forEach((k,v)->builder.add(k,v));
		}
	}
	//
	private void addParams(MultipartBody.Builder builder) {
		if (params != null && !params.isEmpty()) {
			params.forEach((k,v)->{
				builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + k + "\""),
						RequestBody.create(null,v));
			});
		}
	}

	//
	public static class FileInfo {
		public String partName;
		public String fileName;
		public byte[] fileContent;
	}
	//
	public static String getMimeType(String path) {
		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String contentTypeFor = null;
		try {
			contentTypeFor = fileNameMap.getContentTypeFor(URLEncoder.encode(path, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(),e);
		}
		if (contentTypeFor == null) {
			contentTypeFor = "application/octet-stream";
		}
		return contentTypeFor;
	}
}