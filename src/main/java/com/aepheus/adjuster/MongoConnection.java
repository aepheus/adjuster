package com.aepheus.adjuster;

import java.util.*;
import com.google.gson.Gson;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVFormat;
import java.net.UnknownHostException;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

class MongoConnection {
	private Gson gson = new Gson();
	private Mongo mongo = new Mongo("localhost", 27017);
	private DB db = mongo.getDB("adjuster");
	private DBCollection campaign = db.getCollection("campaign");
	private DBCollection creative = db.getCollection("creative");

	void removeCreativesAndCampaigns() {
		DBObject query = (DBObject)JSON.parse("{}");
		creative.remove(query);
		campaign.remove(query);
	}

	long countCreatives() {
		return creative.count();
	}

	long countCampaigns() {
		return campaign.count();
	}

	Creative[] getCreativesByParentId(int parentId) {
		DBObject query = (DBObject)JSON.parse("{\"parentId\":"+parentId+"}");
		DBCursor cursorDoc = creative.find(query);
		Creative[] creativeArray = new Creative[cursorDoc.count()];
		//HashSet offers O(1) complexity
		HashSet creativeSet = new HashSet();
		int index = 0;
		while (cursorDoc.hasNext()) {
			Creative current = gson.fromJson(cursorDoc.next().toString(), Creative.class);
			if(!creativeSet.contains(current.id)){
				creativeSet.add(current.id);
				creativeArray[index++] = current;
			}
		}
		return creativeArray;
	}

	Campaign[] getCampaignsWithClicksAndViews() {
		/*
			We know the creatives have duplicates, and we've filtered those out above.
			We should also filter them out when summing up the views and clicks.
			Duplicates have different values - they are not the same, just the same id...
			Same id, different parentId.
		*/
		HashMap allCampaigns = new HashMap();
		DBCursor cursorDoc = campaign.find();
		while (cursorDoc.hasNext()) {
			Campaign current = gson.fromJson(cursorDoc.next().toString(), Campaign.class);
			allCampaigns.put(current.id, current);
		}
		//db.creative.aggregate([{"$group":{"_id":"$parentId","views":{"$sum":"$views"},"clicks":{"$sum":"$clicks"}}}])
		DBObject query = (DBObject)JSON.parse("{\"$group\":{\"_id\":\"$parentId\",\"views\":{\"$sum\":\"$views\"},\"clicks\":{\"$sum\":\"$clicks\"}}}");
		Iterable<DBObject> output = creative.aggregate(Arrays.asList(query)).results();
		for (DBObject dbObject : output)
		{
			Campaign c = (Campaign)allCampaigns.get(dbObject.get("_id"));
			c.views = Integer.parseInt(dbObject.get("views").toString());
			c.clicks = Integer.parseInt(dbObject.get("clicks").toString());
		}
		Campaign[] retArray = new Campaign[allCampaigns.size()];
		allCampaigns.values().toArray(retArray);
		return retArray;
	}

	void saveCampaigns(Campaign[] campaignList) {
		DBObject[] insert = new DBObject[campaignList.length];
		for(int i = 0; i < campaignList.length; i++){
			insert[i] = (DBObject) JSON.parse(gson.toJson(campaignList[i], Campaign.class));
		}
		campaign.insert(insert);
	}

	void saveCreatives(Creative[] creativesList) {
		DBObject[] insert = new DBObject[creativesList.length];
		for(int i = 0; i < creativesList.length; i++){
			insert[i] = (DBObject) JSON.parse(gson.toJson(creativesList[i], Creative.class));
		}
		creative.insert(insert);
	}

}
