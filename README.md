﻿##resas2arcgis
 
 このサービスは [RESAS API](https://opendata.resas-portal.go.jp/) から取得できるデータを ArcGIS へ簡単に入力できるようにした開発したサービスです。<br>
  <img src="http://apps.esrij.com/arcgis-dev/github/img/resas2arcgis.png" >
 
 * RESAS API とは？
 
 > 内閣官房（まち・ひと・しごと創生本部事務局）が地方創生の実現に向けて、地域に紐付いた官民ビッグデータ（産業、人口、観光、農業等）を分かりやすく「見える化」したシステムです。
 
 * デモ
 
 [resas2arcgis on Heroku](https://resas2arcgis.herokuapp.com/)
 
## API 仕様

* Request
   ```java
   POST https://resas2arcgis.herokuapp.com/api/uploadarc
   ```

* Parameters
 
 |パラメーター|内容|
 |:--:|:--:|
 |resasurl|RESAS API の URL|
 |resaskey|RESAS API キー|
 |mappingfact|結合に利用する値を持つプロパティ名（都道府県/市区町村コード等）|
 |hierarchy|転送したい値を持つプロパティのパス|
 |agollayer|ArcGIS フィーチャ レイヤーの REST エンドポイント URL|
 |ufield|ArcGIS Unique Field|
 |nfield|ArcGIS New Data Field|
 
* `hierarchy` の記述方法

 * 指定した都道府県コードに対して市区町村のデータセットを配列で返す場合（[人口/将来人口推計](https://opendata.resas-portal.go.jp/docs/api/v1/population/future/cities.html)など）
 
   ```javascript
         {
           "message": null,
           "result": {
               "cities": [{
                   "cityCode": "01101",
                   "cityName": "札幌市中央区",
                   "value": 262557,
                   "ratio": 0.22
               }, {
                   "cityCode": "01102",
                   "cityName": "札幌市北区",
                   "value": 257847,
                   "ratio": 0.38
               }, {
                   "cityCode": "01103",
                   "cityName": "札幌市東区",
                   "value": 225135,
                   "ratio": 0.39
               }, {
   ```
   `hierarchy` に `result/cities[n]/value` を指定すると、上記のレスポンスに対して北海道の各市区町村の人口推計の値を取得します。
    
 * 指定した都道府県/市区町村コードのデータのみを返す場合（[自治体比較/企業数](https://opendata.resas-portal.go.jp/docs/api/v1/municipality/company/perYear.html)など）
        
   ```javascript
    {
        "message": null,
        "result": {
            "prefCode": 11,
            "prefName": "埼玉県",
            "cityCode": "11362",
            "cityName": "皆野町",
            "sicName": "製造業",
            "sicCode": "E",
            "simcName": "なめし革・同製品・毛皮製造業",
            "simcCode": "20",
            "data": [{
                "year": 2009,
                "value": 0
            }, {
                "year": 2012,
                "value": 0
            }, {
                "year": 2014,
                "value": 0
            }]
        }
    }
   ```
   `hierarchy` に `result/data[2]/value` を指定すると、上記のレスポンスに対して埼玉県皆野町の 2014 年時点のなめし革・同製品・毛皮製造業企業数の値を取得します。
 
<!-- 
* Example (curl)

   ```
     $ curl -F resasurl="https://opendata.resas-portal.go.jp/api/v1/municipality/company/perYear?cityCode=11362&simcCode=20&prefCode=11&sicCode=E" -F mappingfact="cityCode" -F resaskey={REPLACE WITH YOUR API KEY} -F agollayer="https://services1.arcgis.com/RVzd6I1g6h9fqyZM/arcgis/rest/services/saitama-ken/FeatureServer/0" -F hierarchy="result/data[2]/value" -F ufield="JCODE" -F nfield="num_company20" "https://resas2arcgis.herokuapp.com/api/putMessage"
   ```
-->

## resas2arcgis によるデータ入力処理前に必要なワークフロー

 * ワークフロー
 
  1. 境界データ（都道府県/市区町村）の取得
  1. 自身の ArcGIS for Developers サイトに取得したデータをホスト
  1. ホストしたデータの設定（RESAS データ入力用フィールドの作成と編集有効化）
  1. resas2arcgis のパラメーター入力項目の取得（RESAS API/ArcGIS REST API）
 
 * ページ リンク
 
  *	[ArcGIS Developers](https://developers.arcgis.com/)  ： 開発者アカウントの作成
  *	[ArcGIS Open Data 全国市区町村界データ](http://arcg.is/2iTeKD9) ： RESAS API データを追加するためのデータ取得
  *	[ArcGIS Online](https://www.arcgis.com/home/)    ： Web マップを作成します
  *	[RESAS API 仕様書](https://opendata.resas-portal.go.jp/docs/api/v1/index.html)    ： データの取得方法など定義しています
  *	[resas2arcgis](https://resas2arcgis.herokuapp.com)   ： データ追加します☆
 
 * 手順

  1.	ArcGIS for Developers アカウント作成
   
    以下のサイトから [Get a Free Account] をクリックします(登録にはメールアドレスが必要です)。<br>
    <https://developers.arcgis.com/>
   
  1.	データを可視化してみたい都道府県を選ぶ
   
  1.	シェープファイルをダウンロード(ArcGIS Open Data フィルタリング)

    オープンデータ カタログ サイトの [ArcGIS Open Data](http://opendata.arcgis.com/)から市区町村の境界データをダウンロードします。<br>
    [全国市区町村界データ](http://arcg.is/2iTeKD9)のページにアクセス。<br>
    選んだ都道府県でフィルタリングをしてシェープファイルでダウンロードします。

  1.	シェープファイルを ArcGIS Online にホスト(フィーチャ レイヤー作成)

    [ArcGIS のページ](https://www.arcgis.com/home/)にアクセスして、ステップ１で作成したアカウントでサイン インします。<br>
    ページ上部の [マイ コンテンツ] を選択後、[アイテムの追加] でローカルのシェープファイルをアップロードします。<br>
    ※「このファイルをホストレイヤーとして公開します」にチェックが入っていることを確認

  1.	公開設定

    アップロードするとアイテム ページが開きます。ページ右の [共有] をクリックして、アクセス権を [すべての人に公開 (パブリック)] に変更します。

  1.	編集可能設定

    そのままアイテム ページの [設定] タブを開きます。[Feature Layer (ホスト) 設定] の「編集の有効化」にチェックを入れます。<br>
    これで API 経由でのデータ入力が可能になります。

  1.	フィールド追加 (RESAS データの入力先)

    フィーチャ レイヤーの属性テーブルに RESAS データを入力するためのフィールド(カラム)を新規追加します。<br>
    [概要] タブに戻り、[マップ ビューアーで開く] をクリックして、マップ ビューアーを開きます。<br>
    画面左の [コンテンツ] 上のレイヤー名にカーソルを合わせて、テーブル アイコンをクリックして属性テーブルを開きます。<br>
    テーブル上部の [オプション] > [フィールドの追加] を選択して、RESAS データを入力するフィールドを追加します。

  1.	フィーチャ レイヤーの REST エンドポイント URL の取得

    [概要] タブに戻り、「レイヤー」セクションにある [サービスの URL] をクリックします。<br>
    このリンク先の URL が ArcGIS の REST エンドポイントになるので、メモしておきます。

  1.	resas2arcgis (Heroku) アクセス先

    resas2arcgis のサービスへ接続するために、以下の URL へアクセスします。<br>
    <https://resas2arcgis.herokuapp.com>

  1.	resas2arcgis (Heroku) パラメーター入力

    RESAS API のデータを ArcGIS の指定のフィーチャ レイヤーに入力するために、パラメータを入力します。<br>
    パラメーターの内容は [API 仕様](#API-仕様)をご覧ください。

  1.	実行

    画面下部の[転送]ボタンを押下します。

  1.	データの追加の確認 (テーブルを見る)

    アイテム ページに戻り、[データ] タブを開きます。<br>
    属性テーブルを閲覧し、入力したデータが追加されているか確認してみてください。

