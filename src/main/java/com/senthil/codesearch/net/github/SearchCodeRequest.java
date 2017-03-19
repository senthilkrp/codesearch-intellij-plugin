package com.senthil.codesearch.net.github;

import com.senthil.codesearch.net.CodeSearchRequest;
import com.senthil.codesearch.model.AbstractCodeSearchRequest;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;


/**
 * Created by spanneer on 3/18/17.
 */
public class SearchCodeRequest extends AbstractCodeSearchRequest {

  @Override
  public CodeSearchRequest setContext(SearchContext context) {
    return null;
  }

  @Override
  public CodeSearchRequest setFileName(String fileName) {
    setQuery(fileName);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder queryBuilder = new StringBuilder();
    if (query != null) {
      queryBuilder.append(query);
    }
    if (fileType != null) {
      queryBuilder.append(" ext:").append(fileType);
    }
    if(facet != null) {
      queryBuilder.append(" repo:").append(facet);
    }
    return queryBuilder.toString();
  }

  @Override
  public String build() {
    List<NameValuePair> params = new ArrayList<>();
    if (fileType == null) {
      params.add(new BasicNameValuePair("q", query));
    } else {
      params.add(new BasicNameValuePair("q", query + "ext:" + fileType));
    }

    params.add(new BasicNameValuePair("p", String.valueOf(start / 20)));
    if (facet != null) {
      params.add(new BasicNameValuePair("src", facet));
    }
    return URLEncodedUtils.format(params, Consts.UTF_8);
  }
  @Override
  public CodeSearchRequest copy() {
    SearchCodeRequest request = new SearchCodeRequest();
    request.query = query;
    request.numToReturn = numToReturn;
    request.start = start;
    request.requestTimeOut = requestTimeOut;
    request.facet = facet;
    request.fileName = fileName;
    request.filePath = filePath;
    request.fileType = fileType;
    return request;
  }
}
