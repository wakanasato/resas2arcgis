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
	static String PramKey_mappingFact = "PramKey_mappingFact";
	static String PramKey_resasKey = "PramKey_resasKey";
	static String PramKey_agolURL = "PramKey_agolURL";
	static String PramKey_importField = "PramKey_importField";
	static String PramKey_uniqField = "PramKey_uniqField";
	static String PramKey_newField = "PramKey_newField";
	static String paramSplit = ";";

	/**
	 * AGOL params
	 * */
	public static final String mQuery = "/query";
	public static final String mAgolField = "where=(1=1)&outFields=JCODE,FID,";
	public static final String mAgolQuery = "&f=json&returnGeometry=false";
	public static final String mApplyEdits = "/applyEdits";
	public static final String mApplyEditsReq = "f=json&updates=";

    /**
     * perce for new json
     * */
    public static final String mSeparator = "/";
    public static final String mResult = "result";
    public static final String mSuffix = "n";
    static ArrayList<ResasfieldStr> mJsonKeyList;
    static int nIndex = 0;

    @POST
	@Path("/putMessage")
	@Produces(MediaType.APPLICATION_JSON)
	public MyData put(@FormParam("resasurl") String resasurl,
			@FormParam("mappingfact") String mappingfact,
			@FormParam("resaskey") String resaskey,
			@FormParam("agollayer") String agollayer,
			@FormParam("hierarchy") String hierarchy,
			@FormParam("ufield") String ufield,
			@FormParam("nfield") String nfield) {

		System.out.println("input Params ------------------------");
		System.out.println("resasurl = " + resasurl);
		System.out.println("mappingfact = " + mappingfact);
		System.out.println("resaskey = " + resaskey);
		System.out.println("agollayer = " + agollayer);
		System.out.println("hierarchy = " + hierarchy);
		System.out.println("ufield = " + ufield);
		System.out.println("nfield = " + nfield);
		System.out.println("input Params ------------------------");

		// perse message
		mPramMap = new HashMap<String, String>();
		mPramMap.put(PramKey_resasURL, resasurl);
		mPramMap.put(PramKey_mappingFact, mappingfact);
		mPramMap.put(PramKey_resasKey, resaskey);
		mPramMap.put(PramKey_agolURL, agollayer);
		mPramMap.put(PramKey_importField, hierarchy);
		mPramMap.put(PramKey_uniqField, ufield);
		mPramMap.put(PramKey_newField, nfield);

		return updateData();
	}

	/**
	 * perse data
	 * call update task
	 * */
	public static MyData updateData(){

		boolean dataFlg = false;
		MyData myData = new MyData();
		// agolのレイヤーを取得する
		dataFlg = collectionGeometry(mPramMap.get(PramKey_agolURL));
		if(!dataFlg){

			myData.setId(1);
			myData.setMessage("ArcGIS フィーチャ レイヤーへアクセスできませんでした");
			return myData;
		}
		// resasのデータをcallする
		dataFlg = getResasData(mPramMap.get(PramKey_resasURL), mPramMap.get(PramKey_resasKey));
		// パラメータに沿ってデータを作成する::指定フィールドの検索処理
		// agolを更新する
		if(dataFlg){
			dataFlg = updateAgolLayer(mPramMap.get(PramKey_agolURL));
			if(dataFlg){
				myData.setId(0);
				myData.setMessage("更新に成功しました");
				return myData;
			}else{
				myData.setId(3);
				myData.setMessage("更新に失敗しました");
				return myData;
			}
		}else{
			myData.setId(2);
			myData.setMessage("RESAS API へアクセスできませんでした");
			return myData;
		}

	}

    /**
     * get update layer geometry list
     * */
    public static boolean collectionGeometry(String pAgolLayer){

    	// String想定 : http://***/0 まで
    	// Agolで指定された引数のfieldもoutfieldsに追加する
        JSONObject result = getArcGISRequest(pAgolLayer + mQuery, mAgolField + mPramMap.get(PramKey_uniqField) + mAgolQuery);
        try {
			createRecestGeometryList(result);
		} catch (JSONException e) {
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

        JSONArray features = pJsonObject.getJSONArray(mFeatures);
        System.out.println("Response: create geometry list by agol" );
        mPolygonList = new HashMap<String,JSONObject>();
        for(int i = 0 ; i < features.length();i++){
            mPolygonList.put(features.getJSONObject(i).getJSONObject(mAttributes)
                    .getString(mPramMap.get(PramKey_uniqField)), features.getJSONObject(i));
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
         System.out.println("Response: get Resas Data");
         System.out.println("Response:" + pResasURL);
		 try {
			 if(resasResut.get(mResult).equals(null)){
				 return false;
			 }
	         // "n"を含むものか精査する。
			 if(chkSuffix()){
				 // pattern1
				 makeUpdateListLoop(resasResut);
			 }else{
				 // nomaldata:pattern1
				 // updateリストの作成
				 // pattern2
				 // mappingfieldの値を取得する
				 makeUpdateList(dispatchResasData(resasResut),getMappingFieldfact(resasResut));
			 }
		} catch (org.json.JSONException e) {
			e.printStackTrace();
			return false;
		}
    	return true;
    }

    /**
     * 指定されたコードの内容を取得する
     * resas data or url
     * @throws JSONException
     *
     * */
    public static String getMappingFieldfact(JSONObject pResasResut) throws JSONException{

        // citycodeの取得
        String resasUrl = mPramMap.get(PramKey_resasURL);
        // ? までを切って&で配列にして=から最後までの文字列を取得する:cityCode=-&
        String[] request = resasUrl.substring(resasUrl.indexOf("?")+1, resasUrl.length()).split("&");
        for(String param : request){
        	if(param.startsWith(mPramMap.get(PramKey_mappingFact))){
        		return  param.substring(mPramMap.get(PramKey_mappingFact).length()+1, param.length());
        	}
        }
        // resultのすぐ下にあると決めつける。
        return pResasResut.getJSONObject(mResult).getJSONObject(mPramMap.get(PramKey_mappingFact)).toString();

    }

    /**
     * suffix check
     * */
    static boolean chkSuffix(){
		boolean loopFlg = false;
		nIndex = 0;

    	// 配列の添え字チェック(int or n)
    	// json Key の格納
        String[] getKey = mPramMap.get(PramKey_importField).split(mSeparator);
        mJsonKeyList = new ArrayList<ResasfieldStr>();

        for(int i = 0; i < getKey.length ;i++){

        	// keyが配列
        	if(getKey[i].endsWith("]")){
        		// 配列のとき
        		String Jsonkey = getKey[i].substring(0, getKey[i].indexOf("["));
        		String suffix = getKey[i].substring(getKey[i].indexOf("[")+1, getKey[i].length()-1);
        		mJsonKeyList.add(setStr(Jsonkey, suffix));
        		if(suffix.equals(mSuffix)){
        			// ループできるデータの場合
        			loopFlg = true;
        			nIndex = i;
        		}
        	}else{
        		// keyのとき
        		mJsonKeyList.add(setStr(getKey[i],null));
        	}
        }
        return loopFlg;
    }

    /**
     * 構造体用の内部クラス.
     *
     */
    private static class ResasfieldStr {
        String jsonKey;
        String suffix;
    }

    /**
     * 構造体に値をセット.
     * @param pJsonKey
     * @param pSuffix
     * @return
     */
    public static ResasfieldStr setStr(String pJsonKey, String pSuffix) {
    	ResasfieldStr str = new ResasfieldStr();
        str.jsonKey = pJsonKey;
        str.suffix = pSuffix;
        return str;
    }


    /**
     * resas data dispatcher
     * @throws JSONException
     * 指定の値を入力したいとき
     *
     * */
    static Object dispatchResasData(JSONObject pResasResut) throws JSONException{

        // citycode = JCODE
        mUpdateList = new ArrayList<JSONObject>();
        JSONObject perseObj = null;
        Object lastObj = null;

        // resasFeildStructure から作成したリストで回す。
        for(int i = 0; i < mJsonKeyList.size() ;i++ ){

        	if(mJsonKeyList.get(i).suffix == null){
        		// Keyのみのとき
        		if(perseObj == null){
            		perseObj = pResasResut.getJSONObject(mJsonKeyList.get(i).jsonKey);
        		}else{
        			if(i == mJsonKeyList.size() -1){
        				// 最後のオブジェクトのとき
        				lastObj = perseObj.get(mJsonKeyList.get(i).jsonKey);
        			}else{
        				perseObj = perseObj.getJSONObject(mJsonKeyList.get(i).jsonKey);
        			}
        		}
        	}else{
        		// 配列のとき
        		int indexNum = Integer.parseInt(mJsonKeyList.get(i).suffix);
        		perseObj = perseObj.getJSONArray(mJsonKeyList.get(i).jsonKey).getJSONObject(indexNum);
        	}
        }
        return lastObj;
    }

    static Pattern mPattern = Pattern.compile("[0-9]");

    /**
     * create update list
     * pattern1data
     * */
    static boolean makeUpdateList(Object pObject,String pMappingValue) throws org.json.JSONException{


    	// 指定コードの値が必要！
        if(mPolygonList.containsKey(pMappingValue)){
            JSONObject jsonobj = mPolygonList.get(pMappingValue);
            JSONObject updatejson = new JSONObject();

            // 更新するarcgisのfield名の取得
            if(mPattern.matcher(pObject.toString()).find()){
            	// int
                updatejson.accumulate(mAttributes, jsonobj.getJSONObject(mAttributes).accumulate(mPramMap.get(PramKey_newField), Integer.parseInt(pObject.toString())));
            }else{
            	// String
                updatejson.accumulate(mAttributes, jsonobj.getJSONObject(mAttributes).accumulate(mPramMap.get(PramKey_newField), pObject.toString()));
            }
            mUpdateList.add(updatejson);
        }
        System.out.println("Response: update list is ok [makeUpdateList]");

        return true;

    }

    /**
     * resas data dispatcher
     * @throws JSONException
     * 指定の値を入力したいとき
     *
     * */
    static void makeUpdateListLoop(JSONObject pResasResut) throws JSONException{

        mUpdateList = new ArrayList<JSONObject>();
        JSONObject perseObj = null;
        JSONObject jsonTmpObj;

        // resasFeildStructure から作成したリストで回す。
        for(int i = 0; i < mJsonKeyList.size() ;i++ ){

        	if(mJsonKeyList.get(i).suffix == null){
        		// Keyのみのとき
        		perseObj = pResasResut.getJSONObject(mJsonKeyList.get(i).jsonKey);
        	}else{
        		// 配列のとき=n;必ず
        		// 要素の数でリストを回す
        		// nのしたには必ず最後の要素があるとするnのパターンは考えない
        		// mJsonKeyList + 1で下の要素を指すことにする
        		JSONArray arrayResult = perseObj.getJSONArray(mJsonKeyList.get(i).jsonKey);
                for(int j =0 ;j < arrayResult.length();j++){

                	// もし、mappingFieldがPrefCodeならばRESAS から取得できるデータをString2桁に変更する
                	String mapvalue  = arrayResult.getJSONObject(j).get(mPramMap.get(PramKey_mappingFact)).toString();
                	if(mPramMap.get(PramKey_mappingFact).equals("prefCode")){
                		mapvalue = null;
                		mapvalue = String.format("%02d",Integer.valueOf(arrayResult.getJSONObject(j).get(mPramMap.get(PramKey_mappingFact)).toString()));
                	}

                    if(mPolygonList.containsKey(mapvalue.toString())){
                    	// citycodeが取得できるはず
//                    	String contedKey = arrayResult.getJSONObject(j).get(mPramMap.get(PramKey_mappingFact)).toString();
                    	// ポリゴンリストから該当したarcの要素を出す。
                        JSONObject jsonobj = mPolygonList.get(mapvalue);
//                        JSONObject jsonobj = mPolygonList.get(contedKey);
                        jsonTmpObj = new JSONObject();
                        jsonTmpObj.accumulate(mAttributes, jsonobj.getJSONObject(mAttributes).accumulate(mPramMap.get(PramKey_newField), arrayResult.getJSONObject(j).get(mJsonKeyList.get(i+1).jsonKey)));
                        mUpdateList.add(jsonTmpObj);
                    }
                }
        		// listへの格納が終わったらloopは抜ける
                return;
        	}
        }
    }

    /**
     * update agol
     * */
    static boolean  updateAgolLayer(String pAgolLayer){

        // REST CALL
        JSONObject result = getArcGISRequest(pAgolLayer + mApplyEdits,mApplyEditsReq + mUpdateList.toString() );
        System.out.println("Response: update Done");
        System.out.println("Response:" + result.toString());
        return true;
    }
    /**
     * For RESAS
     * REST POST CALL
     * POST
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
                  System.out.println("Response: from Resas");
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
     * GET
     * */
    public static JSONObject getArcGISRequest(String pStringUrl, String pRequestJson){

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
                  System.out.println("Response: from ArcGIS Online");
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
