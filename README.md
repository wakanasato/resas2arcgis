##resas2arcgis
 
 このサービスは [RESAS API](https://opendata.resas-portal.go.jp/) から取得できるデータを ArcGIS へ簡単に入力できるようにした開発したサービスです。<br>
  <img src="http://apps.esrij.com/arcgis-dev/github/img/resas2arcgis.png" >
 
 * RESAS API とは？<br>
 内閣官房（まち・ひと・しごと創生本部事務局）が地方創生の実現に向けて、地域に紐付いた官民ビッグデータ（産業、人口、観光、農業等）を分かりやすく「見える化」したシステムです。

##サービス使用時に必要なURL

 1.	[ArcGIS Developers](https://developers.arcgis.com/)    ： 開発者アカウントの作成
 1.	[ArcGIS Open Data 全国市区町村界データ](http://arcg.is/2iTeKD9) ： RESAS API データを追加するためのデータ取得
 1.	[ArcGIS Online](https://www.arcgis.com/home/)    ： Web マップを作成します
 1.	[RESAS API 仕様書](https://opendata.resas-portal.go.jp/docs/api/v1/index.html)    ： データの取得方法など定義しています
 1.	[resas2arcgis](https://resas2arcgis.herokuapp.com)   ： データ追加します☆
 
##resas2arcgis を使ってみようハンズオン

 1.	ArcGIS for Developers アカウント作成(事前)※未作成者は受付時対応

  以下のサイトから [Get a Free Account] をクリックします(登録にはメールアドレスが必要です)。<br>
  <https://developers.arcgis.com/>

 1.	データを可視化してみたい都道府県を選ぶ（受付時）
  
 1.	シェープファイルをダウンロード(ArcGIS Open Data フィルタリング)
 
  オープンデータ カタログ サイトの [ArcGIS Open Data](http://opendata.arcgis.com/)から市区町村の境界データをダウンロードします。<br>
  [全国市区町村界データ](http://arcg.is/2iTeKD9)のページにアクセス。<br>
  選んだ都道府県でフィルタリングをしてシェープファイルでダウンロードします。

 1.	シェープファイルを ArcGIS Online にホスト(フィーチャ レイヤー作成)
 
  [ArcGIS のページ](https://www.arcgis.com/home/)にアクセスして、ステップ１で作成したアカウントでサイン インします。<br>
  ページ上部の [マイ コンテンツ] を選択後、[アイテムの追加] でローカルのシェープファイルをアップロードします。<br>
  ※「このファイルをホストレイヤーとして公開します」にチェックが入っていることを確認してください！

 1.	公開設定

  アップロードするとアイテム ページが開きます。ページ右の [共有] をクリックして、アクセス権を [すべての人に公開 (パブリック)] に変更します。

 1.	編集可能設定

  そのままアイテム ページの [設定] タブを開きます。[Feature Layer (ホスト) 設定] の「編集の有効化」にチェックを入れます。<br>
  これで API 経由でのデータ入力が可能になります。

 1.	フィールド追加(RESAS データの入力先)

  フィーチャ レイヤーの属性テーブルに RESAS データを入力するためのフィールド(カラム)を新規追加します。<br>
  [概要] タブに戻り、[マップ ビューアーで開く] をクリックして、マップ ビューアーを開きます。<br>
  画面左の [コンテンツ] 上のレイヤー名にカーソルを合わせて、テーブル アイコンをクリックして属性テーブルを開きます。<br>
  テーブル上部の [オプション] > [フィールドの追加] を選択して、RESAS データを入力するフィールドを追加します。

 1.	フィーチャ レイヤーの REST エンドポイント URL の取得(コピー)

  [概要] タブに戻り、「レイヤー」セクションにある [サービスの URL] をクリックします。<br>
  このリンク先の URL が ArcGIS の REST エンドポイントになるので、メモしておきます。

 1.	resas2arcgis(Heroku)アクセス先

  resas2arcgis のサービスへ接続するために、以下のURLへアクセスします。<br>
  <https://resas2arcgis.herokuapp.com>

 1.	resas2arcgis(Heroku)パラメーター入力

 RESAS API のデータをArcGIS の指定のフィーチャ レイヤーに入力するために、以下のパラメータを入力します。

  * RESAS API URL：RESAS API のURLを指定します。
  *	RESAS API Key：RESAS API を使用するためのKey (個人で取得)を入力します。
  *	RESAS Mapping Field：ArcGIS と RESAS API に共通値がある RESAS API データのフィールド名を入力します。
  *	RESAS API Data Hierarchy：RESAS API のデータ取得結果の階層を入力します。
  *	ArcGIS Feature Layer：ArcGIS フィーチャ レイヤーの URL を入力します。
  *	ArcGIS Unique Field：ArcGIS と RESAS API に共通値がある ArcGIS フィーチャ レイヤーのフィールド名を入力します。
  *	ArcGIS New Data Field：ArcGIS フィーチャ レイヤーで新たに更新したいフィールドを指定します。


 1.	実行

  画面下部の[転送]ボタンを押下します。

 1.	データの追加の確認(テーブルを見る)

  アイテム ページに戻り、[データ] タブを開きます。<br>
  属性テーブルを閲覧し、入力したデータが追加されているか確認してみてください。

 1.	(ビジュアライズ)※デモのみです。もくもくタイムにトライしてみてください。

  参考ページ：<br>
  [ArcGIS Online 上のデータを可視化するための方法](http://bit.ly/2jnqSZi)<br>
  [データ可視化のワークフロー](http://bit.ly/2k6EI2Y)

