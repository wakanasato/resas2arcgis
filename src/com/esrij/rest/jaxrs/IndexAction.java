package com.esrij.rest.jaxrs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.esrij.rest.jaxrs.entity.MyData;


@Path("/")
public class IndexAction {
	@GET
	@Path("/loadMessages")
	@Produces(MediaType.APPLICATION_JSON)
	public List<MyData> loadList() {
		List<MyData> datas = new ArrayList<MyData>();
		datas.add(new MyData(1, "test1"));
		datas.add(new MyData(2, "test2"));
		datas.add(new MyData(3, "test3"));
		return datas;
	}

    /**
     * Json keys
     * */
    static String mAttributes = "attributes";
    static String mFeatures = "features";
    static String mValue = "value";

    /**
     * polygonリスト
     *  */
    static Map<String,JSONObject> mPolygonList;
    static ArrayList<String> mPolygonListName;
    static ArrayList<JSONObject> mUpdateList;

    /**
     * 入力パラメータ取得key
     * */
	static Map<String,String> mPramMap;
	static String PramKey_resasURL = "PramKey_resasURL";
	static String PramKey_resasKey = "PramKey_resasKey";
	static String PramKey_agolURL = "PramKey_agolURL";
	static String PramKey_importField = "PramKey_importField";
	static String paramSplit = ";";

	/**
	 * AGOL params
	 * */
	public static final String mQuery = "/query";
	public static final String mAgolQuery = "where=(1=1)&outFields=JCODE,FID&f=json&returnGeometry=false";
	public static final String mApplyEdits = "/applyEdits";
	public static final String mApplyEditsReq = "f=json&updates=";

    /**
     * perce for new json
     * */
    public static final String mSeparator = "/";
    public static final String mCityCode = "cityCode";
    public static final String mResult = "result";


    @POST
	@Path("/putMessage")
	@Produces(MediaType.APPLICATION_JSON)
	public void put(@FormParam("message") String message) {
		MyData myData = new MyData();
		myData.setMessage(message);
		System.out.println("message = " + message);
		// put Message
		updateData(message);
		myData.setMessage("success");
	}

	/**
	 * perse data
	 * call update task
	 * */
	public static boolean updateData(String pParam){

		// perse message
		mPramMap = new HashMap<String, String>();
		// pParam=resas,keyString,agollayer,field
		String[] splitprama = pParam.split(paramSplit);
		mPramMap.put(PramKey_resasURL, splitprama[0]);
		mPramMap.put(PramKey_resasKey, splitprama[1]);
		mPramMap.put(PramKey_agolURL, splitprama[2]);
		mPramMap.put(PramKey_importField, splitprama[3]);

		// agolのレイヤーを取得する
		collectionGeometry(mPramMap.get(PramKey_agolURL));
		// resasのデータをcallする
		getResasData(mPramMap.get(PramKey_resasURL), mPramMap.get(PramKey_resasKey));
		// パラメータに沿ってデータを作成する::指定フィールドの検索処理
		// agolを更新する
		updateSpotCount(mPramMap.get(PramKey_agolURL));

		return true;

	}

    /**
     * get update layer geometry list
     * */
    public static boolean collectionGeometry(String pAgolLayer){

    	// String想定 : http://***/0 まで
        JSONObject result = getArcGISRequest(pAgolLayer + mQuery, mAgolQuery);
        try {
			createRecestGeometryList(result);
		} catch (JSONException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
	        return false;
		}
        return true;
    }

    /**
     * create geometry list
     * attribures:FID,colectionpoint
     * geometry:xy
     * @throws org.json.JSONException
     * */
    public static boolean createRecestGeometryList(JSONObject pJsonObject) throws JSONException, org.json.JSONException{

        // JSONオブジェクトからリストを作成する
        JSONArray features = pJsonObject.getJSONArray(mFeatures);
        System.out.println("Response:" + "hogehoge");
        mPolygonList = new HashMap<String,JSONObject>();
        for(int i = 0 ; i < features.length();i++){
        	// TODO ：JCODEが取得できないとだめな感じになっている
            mPolygonList.put(features.getJSONObject(i).getJSONObject(mAttributes)
                    .getString("JCODE"), features.getJSONObject(i));
        }
        return true;
    }

    /**
     * get resas data.
     * */
    private static boolean getResasData(String pResasURL, String pResasKey) {

    	 // https://opendata.resas-portal.go.jp/api/v1-rc.1/population/future/cities?year=2040&prefCode=13：この形(パラメータまで指定済み)
    	 // resasデータ取得
		 JSONObject resasResut = getResasRequest(pResasURL, pResasKey);
		 // TODO ここで検索をかけるメソッドを呼ぶ
		 try {
			// updateリストの作成
			makeUpdateList(resasResut);
		} catch (org.json.JSONException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			return false;
		}

    	return true;
    }

    /**
     * create update list
     *
     * */
    static boolean makeUpdateList(JSONObject pResasResut) throws org.json.JSONException{
        // citycode = JCODE
        mUpdateList = new ArrayList<JSONObject>();

        // 指定のパラメータを解析してデータを取得する
        String[] getKey = mPramMap.get(PramKey_importField).split(mSeparator);

        JSONObject perseObj = null;
        Object lastObj = null;

        String citycodeFromResult = null;

        for(int i = 0; i < getKey.length ;i++){

        	// keyが配列かそうじゃないか
        	if(getKey[i].endsWith("]")){
        		// 配列のとき
        		String Jsonkey = getKey[i].substring(0, getKey[i].indexOf("["));
        		int indexNum = Integer.parseInt(getKey[i].substring(getKey[i].indexOf("[")+1, getKey[i].length()-1));
        		perseObj = perseObj.getJSONArray(Jsonkey).getJSONObject(indexNum);

        	}else{
        		// keyのとき
        		if(perseObj == null){
            		perseObj = pResasResut.getJSONObject(getKey[i]);
        		}else{
        			if(i == getKey.length-1){
        				// 最後のオブジェクトのとき
        				lastObj = perseObj.get(getKey[i]);
        			}else{
        				perseObj = perseObj.getJSONObject(getKey[i]);
        			}
        		}
        		if(getKey[i].equals(mResult)){
        			citycodeFromResult = perseObj.getString(mCityCode);
        		}
        	}
        }

        String citycodeFromUrl = null;
        // citycodeの取得
        String resasUrl = mPramMap.get(PramKey_resasURL);
        // ? までを切って&で配列にして=から最後までの文字列を取得する:cityCode=-&
        String[] request = resasUrl.substring(resasUrl.indexOf("?")+1, resasUrl.length()).split("&");
        for(String param : request){
        	if(param.startsWith(mCityCode)){
        		citycodeFromUrl = param.substring(mCityCode.length()+1, param.length());
        	}
        }

        String citycode;
        if(!citycodeFromUrl.isEmpty()){
        	citycode = citycodeFromUrl;
        }else if(!citycodeFromResult.isEmpty()){
        	citycode = citycodeFromResult;
        }else{
        	// cityCodeがないと更新できないのreturnする
        	return false;
        }

        if(mPolygonList.containsKey(citycode)){
            JSONObject jsonobj = mPolygonList.get(citycode);

            JSONObject updatejson = new JSONObject();
            // 更新するarcgisのfield名の取得
            String field = getKey[getKey.length-1];
            String regex = "[0-9]";
            Pattern p = Pattern.compile(regex);
            if(p.matcher(lastObj.toString()).find()){
            	// int
                updatejson.accumulate(mAttributes, jsonobj.getJSONObject(mAttributes).accumulate(field, Integer.parseInt(lastObj.toString())));
            }else{
            	// String
                updatejson.accumulate(mAttributes, jsonobj.getJSONObject(mAttributes).accumulate(field, lastObj.toString()));
            }
            mUpdateList.add(updatejson);
        }
        System.out.println("update list is ok");

        return true;

    }

    /**
     * 更新する
     * */
    static boolean  updateSpotCount(String pAgolLayer){

        // REST CALL
        JSONObject result = getArcGISRequest(pAgolLayer + mApplyEdits,mApplyEditsReq + mUpdateList.toString() );
        System.out.println("update Done ☆" + result.toString());
        return true;
    }
    /**
     * For RESAS
     * REST POST CALL
     * */
    public static JSONObject getResasRequest(String pStringUrl, String pResasKey){

      HttpURLConnection con = null;
      String buffer = "";
      OutputStream os = null;
      BufferedReader reader = null;
      JSONObject response = null;

      try {
          URL url = new URL(pStringUrl);
          con = (HttpURLConnection) url.openConnection();
          con.setRequestMethod("GET");
          con.setRequestProperty("Accept-Charset", "UTF-8");
          con.setRequestProperty("Content-type", "application/json");
          con.setRequestProperty("X-API-KEY", pResasKey);
          con.setDoOutput(true);
          con.setDoInput(true);

          int status = con.getResponseCode();
          switch(status) {
              case HttpURLConnection.HTTP_OK:
                  InputStream is = con.getInputStream();
                  reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                  buffer = reader.readLine();
                  is.close();

                  String responseStr = buffer;
                  response = new JSONObject(buffer);
                  System.out.println("Response:" + responseStr);
                  return response;
              case HttpURLConnection.HTTP_UNAUTHORIZED:
                  break;

              default:
                  break;
          }
      } catch (Exception ex) {
          ex.printStackTrace();
      } finally {
          try {
              if (reader != null) {
                  reader.close();
              }
              if (os != null) {
                  os.close();
              }
              if (con != null) {
                  con.disconnect();
              }
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
      // 空を返す
      return response;
    }

    /**
     * For ArcGIS
     * REST POST CALL
     * */
    static private JSONObject getArcGISRequest(String pStringUrl, String pRequestJson){

      HttpURLConnection con = null;
      String buffer = "";
      OutputStream os = null;
      BufferedReader reader = null;
      JSONObject response = null;

      try {
          URL url = new URL(pStringUrl);
          con = (HttpURLConnection) url.openConnection();
          con.setRequestMethod("POST");
          con.setRequestProperty("Accept-Charset", "UTF-8");
          con.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
          con.setRequestProperty("Accept", "application/json");
          con.setDoOutput(true);
          con.setDoInput(true);

          //POST用のOutputStreamを取得
          os = con.getOutputStream();
          //POSTするデータ
          PrintStream ps = new PrintStream(os);
          ps.print(pRequestJson);
          ps.close();

          int status = con.getResponseCode();
          switch(status) {
              case HttpURLConnection.HTTP_OK:
                  InputStream is = con.getInputStream();
                  reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                  buffer = reader.readLine();
                  is.close();

                  String responseStr = buffer;
                  response = new JSONObject(buffer);
                  System.out.println("Response:" + responseStr);
                  // TODO stringからJsonオブジェクトに変換する。
                  return response;
              case HttpURLConnection.HTTP_UNAUTHORIZED:
                  break;

              default:
                  break;
          }
      } catch (Exception ex) {
          ex.printStackTrace();
      } finally {
          try {
              if (reader != null) {
                  reader.close();
              }
              if (os != null) {
                  os.close();
              }
              if (con != null) {
                  con.disconnect();
              }
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
      return response;
    }


}
